package org.tasa.socs.moc.hdtrend.tlmdata.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.tasa.socs.moc.hdtrend.model.SohType;
import org.tasa.socs.moc.hdtrend.model.tlmfilter.TelemetryValueCriterion;
import org.tasa.socs.moc.hdtrend.tlmstats.model.TelemetryStatistics;
import org.tasa.socs.moc.hdtrend.tlmstats.repository.NormalTelemetryStatsRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// TRIMMED for reproduction scope: only batchQuery (in the flagged taint flow) is kept;
// the production class also implements query(...) and batchQueryReset(...) backed by a
// second repository. The batchQuery method body is identical to production.
@Service
@Profile("!cary-laptop-tlm-mock")
@RequiredArgsConstructor
public class TelemetryStatsQueryServiceImpl implements TelemetryStatsQueryService {

    private final NormalTelemetryStatsRepository normalTelemetryStatsRepository;

    @Override
    public List<TelemetryStatistics> batchQuery(
            List<String> sohTypes,
            Long startMs,
            Long endMs,
            List<String> series,
            List<TelemetryValueCriterion> telemetryValueCriteria) {
        if (series == null || series.isEmpty()) {
            return List.of();
        }
        List<String> categories = (sohTypes != null ? sohTypes : List.<String>of()).stream()
                .map(s -> {
                    SohType st = SohType.of(s);
                    if (st == null) throw new IllegalArgumentException("Invalid sohType: " + s);
                    return st.name();
                })
                .collect(Collectors.toList());
        Instant start = Instant.ofEpochMilli(startMs);
        Instant end = Instant.ofEpochMilli(endMs);
        return normalTelemetryStatsRepository.getTelemetryStats(
                series,
                categories,
                start,
                end,
                telemetryValueCriteria == null ? new ArrayList<>() : telemetryValueCriteria);
    }
}
