package org.tasa.socs.moc.hdtrend.tlmdata.service;

import org.tasa.socs.moc.hdtrend.model.tlmfilter.TelemetryValueCriterion;
import org.tasa.socs.moc.hdtrend.tlmstats.model.TelemetryStatistics;

import java.util.List;

// TRIMMED for reproduction scope: only batchQuery (in the flagged taint flow) is kept.
// The production interface additionally declares query(...) and batchQueryReset(...).
public interface TelemetryStatsQueryService {

    List<TelemetryStatistics> batchQuery(
            List<String> sohTypes,
            Long startMs,
            Long endMs,
            List<String> series,
            List<TelemetryValueCriterion> telemetryValueCriteria);
}
