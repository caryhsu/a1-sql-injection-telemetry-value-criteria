package org.tasa.socs.moc.hdtrend.tlmdata.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.tasa.socs.moc.hdtrend.model.SohType;
import org.tasa.socs.moc.hdtrend.model.tlmfilter.TelemetryValueCriterion;
import org.tasa.socs.moc.hdtrend.tlmstats.model.TelemetryStatistics;
import org.tasa.socs.moc.hdtrend.tlmstats.repository.NormalTelemetryStatsRepository;
import org.tasa.socs.moc.hdtrend.tlmstats.repository.ResetTelemetryStatsRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("!cary-laptop-tlm-mock")
@RequiredArgsConstructor
public class TelemetryStatsQueryServiceImpl implements TelemetryStatsQueryService {

    private final NormalTelemetryStatsRepository normalTelemetryStatsRepository;
    private final ResetTelemetryStatsRepository resetTelemetryStatsRepository;

    @Override
    public TelemetryStatistics query(String tlmName, SohType sohType, Instant start, Instant end) {
        List<TelemetryValueCriterion> telemetryValueCriteria = new ArrayList<>();
        return normalTelemetryStatsRepository.getTelemetryStats(
                tlmName,
                sohType.name(),
                start,
                end,
                telemetryValueCriteria);
    }

    @Override
    public List<TelemetryStatistics> batchQueryReset(List<String> sohTypes, String importFileName,
            Integer segmentIndex, List<String> series,
            List<TelemetryValueCriterion> telemetryValueCriteria) {
        if (series == null || series.isEmpty()) return List.of();
        List<String> categories = (sohTypes != null ? sohTypes : List.<String>of()).stream()
                .map(s -> {
                    SohType st = SohType.of(s);
                    if (st == null) throw new IllegalArgumentException("Invalid sohType: " + s);
                    return st.name();
                }).toList();
        return resetTelemetryStatsRepository.getTelemetryStats(series, categories, importFileName,
                segmentIndex, telemetryValueCriteria == null ? List.of() : telemetryValueCriteria);
    }

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
