package org.tasa.socs.moc.hdtrend.tlmstats.model;

public final class TelemetryStatisticsAccumulator {

    private long count;
    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;
    private double sum;
    private double sumSquares;
    private double sumLogs;
    private boolean hasNonPositive;

    public TelemetryStatisticsAccumulator() {
    }

    public void add(double value) {
        count++;
        if (value < min) {
            min = value;
        }
        if (value > max) {
            max = value;
        }
        sum += value;
        sumSquares += value * value;
        if (value <= 0.0) {
            hasNonPositive = true;
        } else {
            sumLogs += Math.log(value);
        }
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public TelemetryStatistics toStatistics(String telemetryName) {
        return toStatistics(telemetryName, null);
    }

    public TelemetryStatistics toStatistics(String telemetryName, Double medianValue) {
        if (isEmpty()) {
            return TelemetryStatistics.builder()
                    .telemetryName(telemetryName)
                    .maxValue(null)
                    .minValue(null)
                    .averageValue(null)
                    .medianValue(medianValue)
                    .geometricMeanValue(null)
                    .varianceValue(null)
                    .rootMeanSquareValue(null)
                    .stddevValue(null)
                    .count(0L)
                    .build();
        }

        double averageValue = sum / count;
        Double varianceValue = count <= 1 ? 0.0 : variance();
        Double stddevValue = Math.sqrt(varianceValue);
        Double geometricMeanValue = hasNonPositive ? null : Math.exp(sumLogs / count);
        Double rootMeanSquareValue = Math.sqrt(sumSquares / count);

        return TelemetryStatistics.builder()
                .telemetryName(telemetryName)
                .maxValue(max)
                .minValue(min)
                .averageValue(averageValue)
                .medianValue(medianValue)
                .geometricMeanValue(geometricMeanValue)
                .varianceValue(varianceValue)
                .rootMeanSquareValue(rootMeanSquareValue)
                .stddevValue(stddevValue)
                .count(count)
                .build();
    }

    private Double variance() {
        return Math.max(0.0, sumSquaresCentered() / (count - 1));
    }

    private double sumSquaresCentered() {
        // Reconstruct the centered sum of squares from sufficient summary statistics.
        return sumSquares - (sum * sum / count);
    }
}
