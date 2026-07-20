package org.tasa.socs.moc.hdtrend.tlmdata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.tasa.socs.moc.hdtrend.tlmstats.repository.NormalTelemetryStatsRepository;
import org.tasa.socs.moc.hdtrend.tlmstats.repository.ResetTelemetryStatsRepository;

// TRIMMED for reproduction scope: production TelemetryRepositoryConfig additionally
// registers value / time-range / aggregate repositories from other packages
// (tlmdata.repository.jdbc, tlmtimerange, tlmaggregate), which are unrelated to
// this finding. The two stats-repository beans below are identical to production.
@Configuration
class TelemetryRepositoryConfig {

    @Bean
    NormalTelemetryStatsRepository normalTelemetryStatsRepository(
            JdbcTemplate jdbcTemplate
    ) {
        return new NormalTelemetryStatsRepository(jdbcTemplate);
    }

    @Bean
    ResetTelemetryStatsRepository resetTelemetryStatsRepository(
            JdbcTemplate jdbcTemplate
    ) {
        return new ResetTelemetryStatsRepository(jdbcTemplate);
    }
}
