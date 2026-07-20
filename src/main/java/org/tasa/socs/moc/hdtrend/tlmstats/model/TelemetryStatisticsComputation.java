package org.tasa.socs.moc.hdtrend.tlmstats.model;

import java.util.List;

public interface TelemetryStatisticsComputation {
    TelemetryStatistics compute(String telemetryName, List<Double> rawValues);
}
