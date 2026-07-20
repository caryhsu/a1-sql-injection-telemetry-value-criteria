package org.tasa.socs.moc.hdtrend.tlmstats.model;

import java.util.List;
import java.util.Objects;

abstract class AbstractTelemetryStatisticsCalculator implements TelemetryStatisticsComputation {

    protected List<Double> normalizeValues(List<Double> rawValues) {
        return rawValues == null
                ? List.of()
                : rawValues.stream()
                .filter(Objects::nonNull)
                .filter(Double::isFinite)
                .toList();
    }

    protected TelemetryStatistics emptyStatistics(String telemetryName) {
        return TelemetryStatistics.builder()
                .telemetryName(telemetryName)
                .maxValue(null)
                .minValue(null)
                .averageValue(null)
                .medianValue(null)
                .geometricMeanValue(null)
                .varianceValue(null)
                .rootMeanSquareValue(null)
                .stddevValue(null)
                .count(0L)
                .build();
    }

    protected static Double medianFromSorted(List<Double> sorted) {
        int count = sorted.size();
        int mid = count / 2;
        if ((count & 1) == 1) {
            return sorted.get(mid);
        }
        return (sorted.get(mid - 1) + sorted.get(mid)) / 2.0;
    }
}
