package org.tasa.socs.moc.hdtrend.tlmstats.repository;

import org.springframework.jdbc.core.JdbcTemplate;

final class DbMedianCalculator_POSTGRES extends AbstractDbMedianCalculator {

    DbMedianCalculator_POSTGRES(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public String medianExpression() {
        return "percentile_cont(0.5) within group (order by d.TLM_VALUE)";
    }
}
