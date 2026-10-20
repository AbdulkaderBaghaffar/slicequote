package com.example.costestimator.pricing.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
@Import({ PricingRateService.class, PricingRateSeeder.class })
class PricingRateTest {

  @Autowired
  private PricingRateRepository repo;
  @Autowired
  private PricingRateService service;
  @Autowired
  private PricingRateSeeder seeder;
  @Autowired
  private JdbcTemplate jdbc;

  @BeforeEach
  void resetDb() {
    repo.deleteAll();
  }

  // --- seeder ------------------------------------------------------------

  @Test
  void seederCreatesExactlyOneRowOnFreshDb() throws Exception {
    assertThat(repo.count()).isZero();

    seeder.run();

    assertThat(repo.count()).isEqualTo(1);
  }

  @Test
  void seederIsIdempotent() throws Exception {
    seeder.run();
    seeder.run();

    assertThat(repo.count()).isEqualTo(1);
  }

  @Test
  void seederWritesTheDefaultValues() throws Exception {
    seeder.run();

    PricingRate row = repo.findAll().get(0);
    assertThat(row.getHourlyRate()).isEqualByComparingTo(new BigDecimal("15.00"));
    assertThat(row.getMode()).isEqualTo(PricingMode.MACHINE_TIME);
    assertThat(row.getBaseFee()).isEqualByComparingTo(new BigDecimal("2.00"));
    assertThat(row.getMinFloor()).isEqualByComparingTo(new BigDecimal("7.50"));
  }

  // --- mode round-trip ---------------------------------------------------

  @Test
  void modeRoundTripsFromDbToEntity() {
    save(rate("15.00", PricingMode.MACHINE_TIME));

    PricingRate loaded = repo.findAll().get(0);
    assertThat(loaded.getMode()).isEqualTo(PricingMode.MACHINE_TIME);
  }

  @Test
  void modeIsStoredAsTextInTheColumn() {
    save(rate("15.00", PricingMode.MACHINE_TIME));

    String raw = jdbc.queryForObject("select mode from pricing_rate", String.class);

    assertThat(raw).isEqualTo("MACHINE_TIME");
  }

  // --- service reads from the DB, not a constant -------------------------

  @Test
  void serviceReturnsDbValueNotConstant() {
    save(rate("99.00", PricingMode.MACHINE_TIME)); // deliberately != seed value

    assertThat(service.getActiveRate().getHourlyRate())
        .isEqualByComparingTo(new BigDecimal("99.00"));
  }

  @Test
  void afterSeedingServiceReturnsSeedValue() throws Exception {
    seeder.run();

    assertThat(service.getActiveRate().getHourlyRate())
        .isEqualByComparingTo(new BigDecimal("15.00"));
  }

  // --- "active rate" = exactly one row -----------------------------------

  @Test
  void serviceThrowsWhenNoRowExists() {
    assertThatThrownBy(() -> service.getActiveRate())
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void serviceThrowsWhenMoreThanOneRowExists() {
    save(rate("15.00", PricingMode.MACHINE_TIME));
    save(rate("20.00", PricingMode.MACHINE_TIME));

    assertThatThrownBy(() -> service.getActiveRate())
        .isInstanceOf(IllegalStateException.class);
  }

  // --- helpers -----------------------------------------------------------

  private PricingRate rate(String hourly, PricingMode mode) {
    PricingRate r = new PricingRate();
    r.setHourlyRate(new BigDecimal(hourly));
    r.setMode(mode);
    r.setBaseFee(new BigDecimal("2.00"));
    r.setMinFloor(new BigDecimal("7.50"));
    return r;
  }

  private void save(PricingRate r) {
    repo.saveAndFlush(r);
  }
}
