package org.tasa.socs.moc.hdtrend.tlmdata.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.tasa.socs.moc.hdtrend.tlmstats.repository.NormalTelemetryStatsRepository;

// TRIMMED for reproduction scope: production TelemetryRepositoryConfig additionally
// registers ResetTelemetryStatsRepository, which is unrelated to this finding.
@Configuration
public class TelemetryRepositoryConfig {

    @Bean
    NormalTelemetryStatsRepository normalTelemetryStatsRepository(
            JdbcTemplate jdbcTemplate
    ) {
        return new NormalTelemetryStatsRepository(jdbcTemplate);
    }
}
