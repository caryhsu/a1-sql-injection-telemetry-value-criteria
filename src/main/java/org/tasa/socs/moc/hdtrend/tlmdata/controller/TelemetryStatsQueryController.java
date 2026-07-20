package org.tasa.socs.moc.hdtrend.tlmdata.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;
import org.tasa.socs.moc.hdtrend.model.tlmfilter.TelemetryValueCriterion;
import org.tasa.socs.moc.hdtrend.tlmstats.model.TelemetryStatistics;

import org.tasa.socs.moc.hdtrend.model.SohType;
import org.tasa.socs.moc.hdtrend.time.TelemetryTimeParser;
import org.tasa.socs.moc.hdtrend.tlmdata.service.TelemetryStatsQueryService;

import java.time.Instant;
import java.util.List;

@Slf4j
@CrossOrigin(origins = "*")
@AllArgsConstructor
@RestController
@RequestMapping("/api/tlm-stats")
public class TelemetryStatsQueryController {

    private final TelemetryStatsQueryService telemetryStatsQueryService;

    @GetMapping("/{tlmName}")
    public ResponseEntity<TelemetryStatistics> query(
            @PathVariable("tlmName") String tlmName,
            @RequestParam("sohType") String sohTypeStr,
            @RequestParam("start") String startStr,
            @RequestParam("end") String endStr
    ) {
        // XSS Prevention: Sanitize input that will be reflected in output or used in processing
        String sanitizedTlmName = HtmlUtils.htmlEscape(tlmName);
        String sanitizedSohTypeStr = HtmlUtils.htmlEscape(sohTypeStr);

        Instant start = TelemetryTimeParser.parse(startStr);
        Instant end = TelemetryTimeParser.parse(endStr);
        SohType sohType = SohType.of(sanitizedSohTypeStr);
        if (sohType == null) {
            throw new IllegalArgumentException("Invalid sohType: " + sanitizedSohTypeStr);
        }
        log.info("tlmName = {}, sohType = {}, start = {}, end = {}", sanitizedTlmName, sohType, start, end);
        TelemetryStatistics result = telemetryStatsQueryService.query(sanitizedTlmName, sohType, start, end);

        // Security fix: explicitly set Content-Type to application/json and add X-Content-Type-Options
        // to prevent browsers from interpreting the response as HTML (XSS prevention)
        return ResponseEntity.ok()
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("X-Content-Type-Options", "nosniff")
                .body(result);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<TelemetryStatistics>> batchQuery(@RequestBody TlmStatsBatchRequest req) {
        List<TelemetryValueCriterion> criteria = req.telemetryValueCriteria() == null
                ? List.of()
                : req.telemetryValueCriteria();
        int seriesCount = req.series() == null ? 0 : req.series().size();
        log.info(
                "batch stats: sohTypes={} seriesCount={} startMs={} endMs={} telemetryValueCriteriaCount={} telemetryValueCriteria={}",
                req.sohTypes(),
                seriesCount,
                req.startMs(),
                req.endMs(),
                criteria.size(),
                criteria);
        List<TelemetryStatistics> result = telemetryStatsQueryService.batchQuery(
                req.sohTypes(), req.startMs(), req.endMs(), req.series(), criteria);
        log.info("batch stats success: resultCount={}", result.size());
        return ResponseEntity.ok()
                .header("Content-Type", "application/json;charset=UTF-8")
                .header("X-Content-Type-Options", "nosniff")
                .body(result);
    }

    @PostMapping("/reset/batch")
    public ResponseEntity<List<TelemetryStatistics>> batchQueryReset(@RequestBody TlmResetStatsBatchRequest req) {
        List<TelemetryStatistics> result = telemetryStatsQueryService.batchQueryReset(
                req.sohTypes(), req.importFileName(), req.segmentIndex(), req.series(),
                req.telemetryValueCriteria() == null ? List.of() : req.telemetryValueCriteria());
        return ResponseEntity.ok(result);
    }

}
