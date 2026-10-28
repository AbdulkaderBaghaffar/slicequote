package com.example.costestimator.service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class SlicerHealthCheck {

  private static final Logger log = LoggerFactory.getLogger(SlicerHealthCheck.class);

  private final String slicerPath;

  public SlicerHealthCheck(@Value("${slicer.path:prusa-slicer}") String slicerPath) {
    this.slicerPath = slicerPath;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void check() {
    try {
      Process p = new ProcessBuilder(List.of(slicerPath, "--help"))
          .redirectErrorStream(true)
          .start();

      boolean done = p.waitFor(Duration.ofSeconds(10).toSeconds(), TimeUnit.SECONDS);

      if (!done) {
        p.destroyForcibly();
        log.error("slicer did not respond within 10s: {} — uploads will FAIL", slicerPath);
        return;
      }

      log.info("slicer ok: {}", slicerPath);

    } catch (IOException e) {
      log.error("slicer NOT available at '{}' — uploads will FAIL until it is installed", slicerPath);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("interrupted while checking slicer: {}", slicerPath);
    }
  }
}
