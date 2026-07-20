package org.tasa.socs.moc.hdtrend.tlmstats.model;

import java.util.List;

public final class DefaultTelemetryStatisticsCalculator extends AbstractTelemetryStatisticsCalculator {

    @Override
    public TelemetryStatistics compute(String telemetryName, List<Double> rawValues) {
        List<Double> values = normalizeValues(rawValues);
        if (values.isEmpty()) {
            return emptyStatistics(telemetryName);
        }

        int count = values.size();
        Double maxValue = max(values);
        Double minValue = min(values);
        Double averageValue = average(values);
        Double medianValue = median(values);
        Double geometricMeanValue = geometricMean(values);
        Double varianceValue = count <= 1 ? 0.0 : values.stream()
                .mapToDouble(value -> Math.pow(value - averageValue, 2))
                .sum() / (count - 1);
        Double stddevValue = Math.sqrt(varianceValue);
        Double rootMeanSquareValue = rootMeanSquare(values);

        return TelemetryStatistics.builder()
                .telemetryName(telemetryName)
                .maxValue(maxValue)
                .minValue(minValue)
                .averageValue(averageValue)
                .medianValue(medianValue)
                .geometricMeanValue(geometricMeanValue)
                .varianceValue(varianceValue)
                .rootMeanSquareValue(rootMeanSquareValue)
                .stddevValue(stddevValue)
                .count((long) count)
                .build();
    }

    private static Double median(List<Double> values) {
        List<Double> sorted = values.stream().sorted().toList();
        return medianFromSorted(sorted);
    }

    private static Double max(List<Double> values) {
        var result = values.stream().mapToDouble(Double::doubleValue).max();
        return result.isPresent() ? result.getAsDouble() : null;
    }

    private static Double min(List<Double> values) {
        var result = values.stream().mapToDouble(Double::doubleValue).min();
        return result.isPresent() ? result.getAsDouble() : null;
    }

    private static Double average(List<Double> values) {
        var result = values.stream().mapToDouble(Double::doubleValue).average();
        return result.isPresent() ? result.getAsDouble() : null;
    }

    private static Double rootMeanSquare(List<Double> values) {
        Double averageOfSquares = average(values.stream()
                .map(value -> value * value)
                .toList());
        return averageOfSquares == null ? null : Math.sqrt(averageOfSquares);
    }

    private static Double geometricMean(List<Double> values) {
        if (values.stream().anyMatch(value -> value <= 0.0)) {
            return null;
        }
        var logAverageResult = values.stream()
                .mapToDouble(Math::log)
                .average();
        Double logAverage = logAverageResult.isPresent() ? logAverageResult.getAsDouble() : null;
        return logAverage == null ? null : Math.exp(logAverage);
    }
}
