package org.tasa.socs.moc.hdtrend.tlmstats.model;

import lombok.*;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryStatistics {

    private String telemetryName;
    private Double maxValue;
    private Double minValue;
    private Long count;
    private Double averageValue;
    private Double medianValue;
    private Double geometricMeanValue;
    private Double varianceValue;
    private Double rootMeanSquareValue;
    private Double stddevValue;

}

