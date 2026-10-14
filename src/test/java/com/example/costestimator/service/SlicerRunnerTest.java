package com.example.costestimator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.junit.jupiter.api.Test;

class SlicerRunnerTest {

  private final SlicerRunner runner = new SlicerRunner("prusa-slicer", "minimal.ini", Duration.ofSeconds(120), 2);

  @Test
  void parsesDefaultLayerGcode() throws Exception {
    String g = Files.readString(Path.of("src/test/resources/cube.gcode"));
    SliceResult r = runner.parse(g);
    assertEquals(834, r.seconds());
    assertEquals(new BigDecimal("3.59"), r.filamentCm3());
    assertEquals("2.9.6", r.slicerVersion());
  }

  @Test
  void parsesHalfLayerGcode() throws Exception {
    String g = Files.readString(Path.of("src/test/resources/cube3.gcode"));
    assertEquals(1411, runner.parse(g).seconds());
  }

  @Test
  void parsesHoursMinutesSeconds() {
    String g = "; estimated printing time (normal mode) = 6h 17m 34s\n"
        + "; filament used [cm3] = 74.20\n";
    assertEquals(22654, runner.parse(g).seconds());
  }

  @Test
  void throwsWhenNoTime() {
    assertThrows(IllegalStateException.class, () -> runner.parse("garbage"));
  }

  @Test
  void slicesRealCube() {
    assumeTrue(slicerInstalled(), "prusa-slicer not on PATH - skipping");
    SliceResult r = runner.slice(Path.of("src/test/resources/cube.stl"));
    assertTrue(r.seconds() > 0, "expected a positive print time");
    assertTrue(r.filamentCm3().doubleValue() > 0, "expected positive filament volume");
  }

  @Test
  void timesOutAndKillsProcess() throws Exception {
    Path script = script("sleep 300");
    SlicerRunner slow = new SlicerRunner(script.toString(), "minimal.ini", Duration.ofSeconds(2), 1);

    long t0 = System.nanoTime();
    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> slow.slice(Path.of("src/test/resources/cube.stl")));
    long ms = (System.nanoTime() - t0) / 1_000_000;

    assertTrue(ex.getMessage().contains("timed out"), ex.getMessage());
    assertTrue(ms < 20_000, "took " + ms + "ms");

    Thread.sleep(300);
    assertTrue(ProcessHandle.allProcesses()
        .filter(p -> p.info().commandLine().orElse("").contains(script.toString()))
        .findAny().isEmpty(),
        "slicer process survived the timeout");
  }

  @Test
  void rejectsWhenAllSlotsBusy() throws Exception {
    Path marker = Files.createTempFile("busy-", ".marker");
    Files.delete(marker);
    Path script = script("touch " + marker + "\nsleep 2");
    SlicerRunner one = new SlicerRunner(script.toString(), "minimal.ini", Duration.ofSeconds(30), 1);
    Path stl = Path.of("src/test/resources/cube.stl");

    Thread first = new Thread(() -> {
      try {
        one.slice(stl);
      } catch (Exception ignored) {
      }
    });
    first.start();
    awaitFile(marker);

    IllegalStateException busy = assertThrows(IllegalStateException.class, () -> one.slice(stl));
    assertTrue(busy.getMessage().contains("busy"), busy.getMessage());

    first.join();
    IllegalStateException after = assertThrows(IllegalStateException.class, () -> one.slice(stl));
    assertFalse(after.getMessage().contains("busy"), "permit leaked: " + after.getMessage());
  }

  private static Path script(String body) throws IOException {
    Path s = Files.createTempFile("fake-slicer-", ".sh");
    Files.writeString(s, "#!/bin/sh\n" + body + "\n");
    s.toFile().setExecutable(true);
    s.toFile().deleteOnExit();
    return s;
  }

  private static void awaitFile(Path p) throws InterruptedException {
    for (int i = 0; i < 100 && !Files.exists(p); i++) {
      Thread.sleep(100);
    }
    assertTrue(Files.exists(p), "script never started");
  }

  private static boolean slicerInstalled() {
    try {
      Process p = new ProcessBuilder("sh", "-c", "command -v prusa-slicer").start();
      return p.waitFor() == 0;
    } catch (Exception e) {
      return false;
    }
  }
}
