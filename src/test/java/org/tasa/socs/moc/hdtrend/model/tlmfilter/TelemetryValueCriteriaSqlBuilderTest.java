package org.tasa.socs.moc.hdtrend.model.tlmfilter;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.tasa.socs.moc.hdtrend.sql.SqlWithParams;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class TelemetryValueCriteriaSqlBuilderTest {

    @Test
    void testEmptyListReturnsTrueCondition() {
        TelemetryValueCriteriaSqlBuilder builder = new TelemetryValueCriteriaSqlBuilder("name", "value");
        var sqlWithParams = builder.build(List.of());
        String sql = sqlWithParams.sql();
        List<Object> params = sqlWithParams.params();
        assertEquals("1 = 1", sql);
        assertTrue(params.isEmpty(), "Params should be empty for an empty filter list");
    }

    @Test
    void testSingleFilterWithMinMax() {
        TelemetryValueCriteriaSqlBuilder builder = new TelemetryValueCriteriaSqlBuilder("name", "value");
        List<TelemetryValueCriterion> criteria = List.of(
            new TelemetryValueCriterion("TEMP1", 10.0, 100.0)
        );
        SqlWithParams sqlWithParams = builder.build(criteria);
        String sql = sqlWithParams.sql();
        List<Object> params = sqlWithParams.params();
        assertEquals("(name = ? AND value >= ? AND value <= ?)", sql);
        assertEquals(List.of("TEMP1", 10.0, 100.0), params);
    }

    @Test
    void testSingleFilterWithOnlyMin() {
        TelemetryValueCriteriaSqlBuilder builder = new TelemetryValueCriteriaSqlBuilder("name", "value");
        List<TelemetryValueCriterion> criteria = List.of(
            new TelemetryValueCriterion("TEMP2", 5.0, null)
        );
        SqlWithParams sqlWithParams = builder.build(criteria);
        String sql = sqlWithParams.sql();
        List<Object> params = sqlWithParams.params();
        assertEquals("(name = ? AND value >= ?)", sql);
        assertEquals(List.of("TEMP2", 5.0), params);
    }

    @Test
    void testSingleFilterWithOnlyMax() {
        TelemetryValueCriteriaSqlBuilder builder = new TelemetryValueCriteriaSqlBuilder("name", "value");
        List<TelemetryValueCriterion> criteria = List.of(
            new TelemetryValueCriterion("PRESSURE1", null, 300.0)
        );
        SqlWithParams sqlWithParams = builder.build(criteria);
        String sql = sqlWithParams.sql();
        List<Object> params = sqlWithParams.params();
        assertEquals("(name = ? AND value <= ?)", sql);
        assertEquals(List.of("PRESSURE1",  300.0), params);
    }

    @Test
    void testSingleFilterWithNoMinMax() {
        TelemetryValueCriteriaSqlBuilder builder = new TelemetryValueCriteriaSqlBuilder("name", "value");
        List<TelemetryValueCriterion> criteria = List.of(
            new TelemetryValueCriterion("PRESSURE2", null, null)
        );
        SqlWithParams sqlWithParams = builder.build(criteria);
        String sql = sqlWithParams.sql();
        List<Object> params = sqlWithParams.params();
        assertEquals("1 = 1", sql);
        assertTrue(params.isEmpty());
    }

    @Test
    void testMultipleFiltersCombined() {
        TelemetryValueCriteriaSqlBuilder builder = new TelemetryValueCriteriaSqlBuilder("name", "value");
        List<TelemetryValueCriterion> criteria = List.of(
            new TelemetryValueCriterion("TEMP1", 10.0, 100.0),
            new TelemetryValueCriterion("TEMP2", 5.0, null),
            new TelemetryValueCriterion("PRESSURE1", null, 300.0),
            new TelemetryValueCriterion("PRESSURE2", null, null)
        );
        SqlWithParams sqlWithParams = builder.build(criteria);
        String sql = sqlWithParams.sql();
        List<Object> param = sqlWithParams.params();
        String expectedSQL = "(" +
            "(name = ? AND value >= ? AND value <= ?) OR " +
            "(name = ? AND value >= ?) OR " +
            "(name = ? AND value <= ?)" +
            ")";
        List<Object> expectedParam = List.of("TEMP1", 10.0, 100.0,
            "TEMP2", 5.0,
            "PRESSURE1", 300.0
        );
        assertEquals(expectedSQL, sql);
        assertEquals(expectedParam, param);
    }
}
