package org.tasa.socs.moc.hdtrend.tlmdata.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tasa.socs.moc.hdtrend.model.tlmfilter.TelemetryValueCriterion;
import org.tasa.socs.moc.hdtrend.tlmstats.model.TelemetryStatistics;

import org.tasa.socs.moc.hdtrend.tlmdata.service.TelemetryStatsQueryService;

import java.util.List;

// TRIMMED for reproduction scope: only the flagged endpoint (POST /api/tlm-stats/batch,
// Checkmarx source method batchQuery) is kept. The production class additionally has
// GET /api/tlm-stats/{tlmName} and POST /api/tlm-stats/reset/batch, which are unrelated
// to this finding. Class name, annotations, field and the batchQuery method body are
// identical to production. See README.md "File mapping".
@Slf4j
@CrossOrigin(origins = "*")
@AllArgsConstructor
@RestController
@RequestMapping("/api/tlm-stats")
public class TelemetryStatsQueryController {

    private final TelemetryStatsQueryService telemetryStatsQueryService;

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

}
