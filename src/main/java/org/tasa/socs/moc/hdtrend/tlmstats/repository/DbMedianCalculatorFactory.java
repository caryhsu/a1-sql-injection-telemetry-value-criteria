package org.tasa.socs.moc.hdtrend.tlmstats.repository;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

final class DbMedianCalculatorFactory {

    private DbMedianCalculatorFactory() {
    }

    static DbMedianCalculator create(JdbcTemplate jdbcTemplate) {
        if (jdbcTemplate == null || jdbcTemplate.getDataSource() == null) {
            throw new IllegalStateException("JdbcTemplate dataSource is required for median calculator selection");
        }
        return jdbcTemplate.execute((ConnectionCallback<DbMedianCalculator>) connection -> {
            String productName = databaseProductName(connection);
            if (productName == null) {
                throw new IllegalStateException("Unable to determine database product name");
            }
            String normalized = productName.toLowerCase();
            if (normalized.contains("h2")) {
                return new DbMedianCalculator_H2(jdbcTemplate);
            }
            if (normalized.contains("postgresql")) {
                return new DbMedianCalculator_POSTGRES(jdbcTemplate);
            }
            throw new IllegalStateException("Unsupported database for median calculator: " + productName);
        });
    }

    private static String databaseProductName(Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        return metaData == null ? null : metaData.getDatabaseProductName();
    }
}
