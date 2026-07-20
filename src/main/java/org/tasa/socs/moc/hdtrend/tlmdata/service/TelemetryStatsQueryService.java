package org.tasa.socs.moc.hdtrend.tlmdata.service;

import org.tasa.socs.moc.hdtrend.model.SohType;
import org.tasa.socs.moc.hdtrend.model.tlmfilter.TelemetryValueCriterion;
import org.tasa.socs.moc.hdtrend.tlmstats.model.TelemetryStatistics;

import java.time.Instant;
import java.util.List;

public interface TelemetryStatsQueryService {

    TelemetryStatistics query(String tlmName, SohType sohType, Instant start, Instant end);

    List<TelemetryStatistics> batchQuery(
            List<String> sohTypes,
            Long startMs,
            Long endMs,
            List<String> series,
            List<TelemetryValueCriterion> telemetryValueCriteria);

    default List<TelemetryStatistics> batchQueryReset(List<String> sohTypes, String importFileName,
            Integer segmentIndex, List<String> series,
            List<TelemetryValueCriterion> telemetryValueCriteria) { return List.of(); }
}
