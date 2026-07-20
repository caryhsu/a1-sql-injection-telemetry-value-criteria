package org.tasa.socs.moc.hdtrend.tlmstats.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.tasa.socs.moc.hdtrend.model.tlmfilter.TelemetryValueCriterion;
import org.tasa.socs.moc.hdtrend.tlmstats.model.TelemetryStatistics;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runtime proof for Checkmarx finding A1 (SQL_Injection, CWE-89):
 * the exact production taint chain is executed against a real database and
 * user-controlled criterion values demonstrably act as bound parameters,
 * not as SQL text.
 */
@Tag("unit")
class NormalTelemetryStatsRepositoryH2Test {

    private static final Instant START = Instant.parse("2025-12-31T00:00:00Z");
    private static final Instant END = Instant.parse("2026-01-02T00:00:00Z");

    private EmbeddedDatabase db;
    private NormalTelemetryStatsRepository repository;

    @BeforeEach
    void setUp() {
        db = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("repro" + System.nanoTime())
                .addScript("schema.sql")
                .addScript("data.sql")
                .build();
        repository = new NormalTelemetryStatsRepository(new JdbcTemplate(db));
    }

    @Test
    void legitCriteriaReturnStatisticsViaBoundParameters() {
        List<TelemetryStatistics> result = repository.getTelemetryStats(
                List.of("TEMP1"),
                List.of("VC0"),
                START,
                END,
                List.of(new TelemetryValueCriterion("TEMP1", 15.0, 100.0)));

        assertEquals(1, result.size());
        TelemetryStatistics stats = result.get(0);
        assertEquals("TEMP1", stats.getTelemetryName());
        assertEquals(2L, stats.getCount(), "only values in [15, 100] should be aggregated (20.0, 30.0)");
        assertEquals(20.0, stats.getMinValue());
        assertEquals(30.0, stats.getMaxValue());
    }

    @Test
    void injectionShapedCriterionNameIsTreatedAsLiteralValue() {
        // If criterion.getName() were concatenated into SQL text, this payload would
        // match every row; as a bound parameter it matches nothing.
        String payload = "TEMP1' OR '1'='1";
        List<TelemetryStatistics> result = repository.getTelemetryStats(
                List.of("TEMP1"),
                List.of("VC0"),
                START,
                END,
                List.of(new TelemetryValueCriterion(payload, 0.0, 999999.0)));

        assertTrue(result.isEmpty(),
                "injection-shaped name must be bound as a literal value and therefore match no rows");
    }

    @Test
    void injectionShapedTelemetryNameIsTreatedAsLiteralValue() {
        String payload = "TEMP1' OR '1'='1' --";
        List<TelemetryStatistics> result = repository.getTelemetryStats(
                List.of(payload),
                List.of("VC0"),
                START,
                END,
                List.of());

        assertTrue(result.isEmpty(),
                "telemetry names go through the IN (?) placeholder list and must match literally");
    }
}
