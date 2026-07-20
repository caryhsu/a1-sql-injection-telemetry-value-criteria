package org.tasa.socs.moc.hdtrend.tlmstats.repository;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractDbMedianCalculator implements DbMedianCalculator {

    protected final JdbcTemplate jdbcTemplate;

    AbstractDbMedianCalculator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<String, Double> queryMedian(String sql, List<Object> params) {
        return jdbcTemplate.query(sql, rs -> {
            Map<String, Double> result = new LinkedHashMap<>();
            while (rs.next()) {
                String telemetryName = rs.getString("TLM_NAME");
                Double value = rs.getObject("TLM_VALUE") == null ? null : rs.getDouble("TLM_VALUE");
                if (telemetryName == null || value == null || !Double.isFinite(value)) {
                    continue;
                }
                result.put(telemetryName, value);
            }
            return result;
        }, params.toArray());
    }
}
