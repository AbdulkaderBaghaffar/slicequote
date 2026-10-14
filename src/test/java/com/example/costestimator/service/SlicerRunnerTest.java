package com.example.costestimator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.junit.jupiter.api.Test;

class SlicerRunnerTest {

  private final SlicerRunner runner = new SlicerRunner("prusa-slicer", "minimal.ini", Duration.ofSeconds(120));

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

  private static boolean slicerInstalled() {
    try {
      Process p = new ProcessBuilder("sh", "-c", "command -v prusa-slicer").start();
      return p.waitFor() == 0;
    } catch (Exception e) {
      return false;
    }
  }
}
