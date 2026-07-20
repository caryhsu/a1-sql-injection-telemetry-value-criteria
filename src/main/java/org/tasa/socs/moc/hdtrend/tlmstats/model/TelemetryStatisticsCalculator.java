package org.tasa.socs.moc.hdtrend.tlmstats.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TelemetryStatisticsCalculator {

    private static final TelemetryStatisticsComputation DEFAULT = new DefaultTelemetryStatisticsCalculator();

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

    public static List<TelemetryStatistics> aggregate(Map<String, List<Double>> valuesByTelemetry, Collection<String> preferredOrder) {
        Set<String> orderedNames = new LinkedHashSet<>();
        if (preferredOrder != null) {
            for (String telemetryName : preferredOrder) {
                if (valuesByTelemetry != null && valuesByTelemetry.containsKey(telemetryName)) {
                    orderedNames.add(telemetryName);
                }
            }
        }
        if (valuesByTelemetry != null) {
            orderedNames.addAll(valuesByTelemetry.keySet());
        }

        List<TelemetryStatistics> result = new ArrayList<>();
        for (String telemetryName : orderedNames) {
            result.add(DEFAULT.compute(telemetryName, valuesByTelemetry == null ? null : valuesByTelemetry.get(telemetryName)));
        }
        return result;
    }
}
