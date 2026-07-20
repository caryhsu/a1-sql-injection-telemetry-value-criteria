package org.tasa.socs.moc.hdtrend.tlmdata.controller;

import org.tasa.socs.moc.hdtrend.model.tlmfilter.TelemetryValueCriterion;

import java.util.List;

public record TlmStatsBatchRequest(
        List<String> sohTypes,
        Long startMs,
        Long endMs,
        List<String> series,
        List<TelemetryValueCriterion> telemetryValueCriteria
) {}
