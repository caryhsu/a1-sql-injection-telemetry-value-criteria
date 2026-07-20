package org.tasa.socs.moc.hdtrend.tlmstats.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// TRIMMED for reproduction scope: only aggregateStreaming (used by the flagged taint flow) is kept.
// The production class additionally has aggregate(...) backed by TelemetryStatisticsComputation,
// which is unrelated to this finding. See README.md "File mapping".
public final class TelemetryStatisticsCalculator {

    private TelemetryStatisticsCalculator() {
    }

    public static List<TelemetryStatistics> aggregateStreaming(
            Map<String, TelemetryStatisticsAccumulator> accumulators,
            Map<String, Double> medianByTelemetry,
            Collection<String> preferredOrder
    ) {
        Set<String> orderedNames = new LinkedHashSet<>();
        if (preferredOrder != null) {
            for (String telemetryName : preferredOrder) {
                if (accumulators != null && accumulators.containsKey(telemetryName)) {
                    orderedNames.add(telemetryName);
                }
            }
        }
        if (accumulators != null) {
            orderedNames.addAll(accumulators.keySet());
        }

        List<TelemetryStatistics> result = new ArrayList<>();
        for (String telemetryName : orderedNames) {
            TelemetryStatisticsAccumulator accumulator = accumulators == null ? null : accumulators.get(telemetryName);
            if (accumulator == null) {
                continue;
            }
            Double medianValue = medianByTelemetry == null ? null : medianByTelemetry.get(telemetryName);
            result.add(accumulator.toStatistics(telemetryName, medianValue));
        }
        return result;
    }
}
