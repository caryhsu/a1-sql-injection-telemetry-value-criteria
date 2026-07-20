package org.tasa.socs.moc.hdtrend.tlmdata.controller;

import org.tasa.socs.moc.hdtrend.model.tlmfilter.TelemetryValueCriterion;
import java.util.List;

public record TlmResetStatsBatchRequest(
        List<String> sohTypes,
        String importFileName,
        Integer segmentIndex,
        List<String> series,
        List<TelemetryValueCriterion> telemetryValueCriteria) {}
