package org.tasa.socs.moc.hdtrend.model.tlmfilter;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TelemetryValueCriterion {
    private final String name;
    private final Double min;
    private final Double max;

    public TelemetryValueCriterion(String name) {
        this(name, null, null);
    }
}
