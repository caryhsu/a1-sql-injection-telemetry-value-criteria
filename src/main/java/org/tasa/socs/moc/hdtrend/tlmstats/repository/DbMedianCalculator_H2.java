package org.tasa.socs.moc.hdtrend.tlmstats.repository;

import org.springframework.jdbc.core.JdbcTemplate;

final class DbMedianCalculator_H2 extends AbstractDbMedianCalculator {

    DbMedianCalculator_H2(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public String medianExpression() {
        return "MEDIAN(d.TLM_VALUE)";
    }
}
