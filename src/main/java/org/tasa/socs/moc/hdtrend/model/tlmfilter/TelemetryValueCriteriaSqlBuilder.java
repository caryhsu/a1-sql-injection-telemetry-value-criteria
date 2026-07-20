package org.tasa.socs.moc.hdtrend.model.tlmfilter;

import org.tasa.socs.moc.hdtrend.sql.SqlWithParams;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TelemetryValueCriteriaSqlBuilder {

    private final String nameColumn;
    private final String valueColumn;

    public TelemetryValueCriteriaSqlBuilder(String nameColumn, String valueColumn) {
        this.nameColumn = nameColumn;
        this.valueColumn = valueColumn;
    }

    public SqlWithParams build(Collection<TelemetryValueCriterion> telemetryValueCriteria) {
        if (telemetryValueCriteria == null || telemetryValueCriteria.isEmpty()) {
            return new SqlWithParams("1 = 1", List.of());
        }

        List<TelemetryValueCriterion> effectiveCriteria = telemetryValueCriteria.stream()
            .filter(f -> f.getMin() != null || f.getMax() != null)
            .toList();

        if (effectiveCriteria.isEmpty()) {
            return new SqlWithParams("1 = 1", List.of());
        }

        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        for (TelemetryValueCriterion criterion : effectiveCriteria) {
            StringBuilder condition = new StringBuilder();
            condition.append("(").append(nameColumn).append(" = ?");
            params.add(criterion.getName());

            if (criterion.getMin() != null) {
                condition.append(" AND ").append(valueColumn).append(" >= ?");
                params.add(criterion.getMin());
            }
            if (criterion.getMax() != null) {
                condition.append(" AND ").append(valueColumn).append(" <= ?");
                params.add(criterion.getMax());
            }

            condition.append(")");
            conditions.add(condition.toString());
        }

        String joined = String.join(" OR ", conditions);
        String sql = (conditions.size() > 1) ? "(" + joined + ")" : joined;

        return new SqlWithParams(sql, params);
    }
}
