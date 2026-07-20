package org.tasa.socs.moc.hdtrend.tlmstats.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.tasa.socs.moc.hdtrend.model.tlmfilter.TelemetryValueCriteriaSqlBuilder;
import org.tasa.socs.moc.hdtrend.model.tlmfilter.TelemetryValueCriterion;
import org.tasa.socs.moc.hdtrend.sql.SqlWithParams;
import org.tasa.socs.moc.hdtrend.tlmstats.model.TelemetryStatisticsAccumulator;
import org.tasa.socs.moc.hdtrend.tlmstats.model.TelemetryStatistics;
import org.tasa.socs.moc.hdtrend.tlmstats.model.TelemetryStatisticsCalculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ResetTelemetryStatsRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DbMedianCalculator dbMedianCalculator;

    private final TelemetryValueCriteriaSqlBuilder criteriaSqlBuilder =
            new TelemetryValueCriteriaSqlBuilder("n.TLM_NAME", "d.TLM_VALUE");

    public ResetTelemetryStatsRepository(
            JdbcTemplate jdbcTemplate
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.dbMedianCalculator = DbMedianCalculatorFactory.create(jdbcTemplate);
    }

    public List<TelemetryStatistics> getTelemetryStats(
            Collection<String> telemetryNames,
            Collection<String> categories,
            String inputFileName,
            int segmentIndex,
            Collection<TelemetryValueCriterion> telemetryValueCriteria
    ) {
        if (telemetryNames == null || telemetryNames.isEmpty() || categories == null || categories.isEmpty()) {
            return List.of();
        }

        String telemetryPlaceholders = String.join(",", Collections.nCopies(telemetryNames.size(), "?"));
        String categoryPlaceholders = String.join(",", Collections.nCopies(categories.size(), "?"));
        SqlWithParams criteria = criteriaSqlBuilder.build(telemetryValueCriteria);
        String sql = String.format("""
              SELECT n.TLM_NAME,
                     d.TLM_VALUE
                FROM RESET_DATA d, TLM_NAME_DIM n
               WHERE d.TLM_NAME_ID = n.ID
                 AND n.TLM_NAME IN (%s)
                 AND d.CATEGORY IN (%s)
                 AND d.INPUT_FILE_NAME = ?
                 AND d.SEGMENT_INDEX = ?
                 AND (%s)
          """,
                telemetryPlaceholders,
                categoryPlaceholders,
                criteria.sql()
        );

        List<Object> params = new ArrayList<>(telemetryNames);
        params.addAll(categories);
        params.add(inputFileName);
        params.add(segmentIndex);
        params.addAll(criteria.params());

        log.info("sql:\n{}", sql);
        log.info("parameters:\n" +
                        "telemetryNames={}, " +
                        "categories={}, " +
                        "inputFileName={}, " +
                        "segmentIndex={}",
                telemetryNames, categories, inputFileName, segmentIndex);

        long startTime = System.currentTimeMillis();
        String medianSql = String.format("""
              SELECT n.TLM_NAME,
                     %s AS TLM_VALUE
                FROM RESET_DATA d, TLM_NAME_DIM n
               WHERE d.TLM_NAME_ID = n.ID
                 AND n.TLM_NAME IN (%s)
                 AND d.CATEGORY IN (%s)
                 AND d.INPUT_FILE_NAME = ?
                 AND d.SEGMENT_INDEX = ?
                 AND (%s)
            GROUP BY n.TLM_NAME
          """,
                dbMedianCalculator.medianExpression(),
                telemetryPlaceholders,
                categoryPlaceholders,
                criteria.sql()
        );
        var result = queryTelemetryStats(sql, params, medianSql, params, telemetryNames);
        long endTime = System.currentTimeMillis();
        log.info("jdbcTemplate.query took {} ms", (endTime - startTime));
        return result;
    }

    public TelemetryStatistics getTelemetryStats(
            Collection<String> telemetryNames,
            String inputFileName,
            int segmentIndex,
            Collection<TelemetryValueCriterion> telemetryValueCriteria
    ) {
        if (telemetryNames == null || telemetryNames.isEmpty()) {
            throw new EmptyResultDataAccessException(1);
        }

        String telemetryPlaceholders = String.join(",", Collections.nCopies(telemetryNames.size(), "?"));
        SqlWithParams criteria = criteriaSqlBuilder.build(telemetryValueCriteria);
        String sql = String.format("""
              SELECT n.TLM_NAME,
                     d.TLM_VALUE
                FROM RESET_DATA d, TLM_NAME_DIM n
               WHERE d.TLM_NAME_ID = n.ID
                 AND n.TLM_NAME IN (%s)
                 AND d.INPUT_FILE_NAME = ?
                 AND d.SEGMENT_INDEX = ?
                 AND (%s)
          """,
                telemetryPlaceholders,
                criteria.sql()
        );

        List<Object> params = new ArrayList<>(telemetryNames);
        params.add(inputFileName);
        params.add(segmentIndex);
        params.addAll(criteria.params());

        log.info("sql:\n{}", sql);
        log.info("parameters:\n" +
                        "telemetryNames={}, " +
                        "inputFileName={}, " +
                        "segmentIndex={}",
                telemetryNames, inputFileName, segmentIndex);

        long startTime = System.currentTimeMillis();
        String medianSql = String.format("""
              SELECT n.TLM_NAME,
                     %s AS TLM_VALUE
                FROM RESET_DATA d, TLM_NAME_DIM n
               WHERE d.TLM_NAME_ID = n.ID
                 AND n.TLM_NAME IN (%s)
                 AND d.INPUT_FILE_NAME = ?
                 AND d.SEGMENT_INDEX = ?
                 AND (%s)
            GROUP BY n.TLM_NAME
          """,
                dbMedianCalculator.medianExpression(),
                telemetryPlaceholders,
                criteria.sql()
        );
        var result = querySingleTelemetryStats(sql, params, medianSql, params, telemetryNames.iterator().next());
        long endTime = System.currentTimeMillis();
        log.info("jdbcTemplate.query took {} ms", (endTime - startTime));
        return result;
    }

    public TelemetryStatistics getTelemetryStats(
            String telemetryName,
            String category,
            String inputFileName,
            int segmentIndex,
            Collection<TelemetryValueCriterion> telemetryValueCriteria
    ) {
        SqlWithParams criteria = criteriaSqlBuilder.build(telemetryValueCriteria);
        String sql = String.format("""
              SELECT n.TLM_NAME,
                     d.TLM_VALUE
                FROM RESET_DATA d, TLM_NAME_DIM n
               WHERE d.TLM_NAME_ID = n.ID
                 AND n.TLM_NAME = ?
                 AND d.CATEGORY = ?
                 AND d.INPUT_FILE_NAME = ?
                 AND d.SEGMENT_INDEX = ?
                 AND (%s)
          """, criteria.sql());

        List<Object> params = new ArrayList<>();
        params.add(telemetryName);
        params.add(category);
        params.add(inputFileName);
        params.add(segmentIndex);
        params.addAll(criteria.params());

        log.info("sql:\n{}", sql);
        log.info("parameters:\n" +
                        "telemetryName={}, " +
                        "category={}, " +
                        "inputFileName={}, " +
                        "segmentIndex={}",
                telemetryName, category, inputFileName, segmentIndex);

        long startTime = System.currentTimeMillis();
        String medianSql = String.format("""
              SELECT n.TLM_NAME,
                     %s AS TLM_VALUE
                FROM RESET_DATA d, TLM_NAME_DIM n
               WHERE d.TLM_NAME_ID = n.ID
                 AND n.TLM_NAME = ?
                 AND d.CATEGORY = ?
                 AND d.INPUT_FILE_NAME = ?
                 AND d.SEGMENT_INDEX = ?
                 AND (%s)
            GROUP BY n.TLM_NAME
          """,
                dbMedianCalculator.medianExpression(),
                criteria.sql()
        );
        var result = querySingleTelemetryStats(sql, params, medianSql, params, telemetryName);
        long endTime = System.currentTimeMillis();
        log.info("jdbcTemplate.query took {} ms", (endTime - startTime));
        return result;
    }

    public TelemetryStatistics getTelemetryStats(
            String telemetryName,
            String inputFileName,
            int segmentIndex,
            Collection<TelemetryValueCriterion> telemetryValueCriteria
    ) {
        SqlWithParams criteria = criteriaSqlBuilder.build(telemetryValueCriteria);
        String sql = String.format("""
              SELECT n.TLM_NAME,
                     d.TLM_VALUE
                FROM RESET_DATA d, TLM_NAME_DIM n
               WHERE d.TLM_NAME_ID = n.ID
                 AND n.TLM_NAME = ?
                 AND d.INPUT_FILE_NAME = ?
                 AND d.SEGMENT_INDEX = ?
                 AND (%s)
          """, criteria.sql());

        List<Object> params = new ArrayList<>();
        params.add(telemetryName);
        params.add(inputFileName);
        params.add(segmentIndex);
        params.addAll(criteria.params());

        log.info("sql:\n{}", sql);
        log.info("parameters:\n" +
                        "telemetryName={}, " +
                        "inputFileName={}, " +
                        "segmentIndex={}",
                telemetryName, inputFileName, segmentIndex);

        long startTime = System.currentTimeMillis();
        String medianSql = String.format("""
              SELECT n.TLM_NAME,
                     %s AS TLM_VALUE
                FROM RESET_DATA d, TLM_NAME_DIM n
               WHERE d.TLM_NAME_ID = n.ID
                 AND n.TLM_NAME = ?
                 AND d.INPUT_FILE_NAME = ?
                 AND d.SEGMENT_INDEX = ?
                 AND (%s)
            GROUP BY n.TLM_NAME
          """,
                dbMedianCalculator.medianExpression(),
                criteria.sql()
        );
        var result = querySingleTelemetryStats(sql, params, medianSql, params, telemetryName);
        long endTime = System.currentTimeMillis();
        log.info("jdbcTemplate.query took {} ms", (endTime - startTime));
        return result;
    }

    public List<TelemetryStatistics> getTelemetryStats(
            Collection<String> telemetryNames,
            Collection<String> categories,
            long executionId,
            Collection<TelemetryValueCriterion> telemetryValueCriteria
    ) {
        if (telemetryNames == null || telemetryNames.isEmpty() || categories == null || categories.isEmpty()) {
            return List.of();
        }

        String telemetryPlaceholders = String.join(",", Collections.nCopies(telemetryNames.size(), "?"));
        String categoryPlaceholders = String.join(",", Collections.nCopies(categories.size(), "?"));
        SqlWithParams criteria = criteriaSqlBuilder.build(telemetryValueCriteria);
        String sql = String.format("""
              SELECT n.TLM_NAME,
                     d.TLM_VALUE
                FROM RESET_DATA d, TLM_NAME_DIM n
               WHERE d.TLM_NAME_ID = n.ID
                 AND n.TLM_NAME IN (%s)
                 AND d.CATEGORY IN (%s)
                 AND d.EXECUTION_ID = ?
                 AND (%s)
          """,
                telemetryPlaceholders,
                categoryPlaceholders,
                criteria.sql());

        List<Object> params = new ArrayList<>(telemetryNames);
        params.addAll(categories);
        params.add(executionId);
        params.addAll(criteria.params());

        log.info("sql:\n{}", sql);
        log.info("parameters:\n" +
                        "telemetryNames={}, " +
                        "categories={}, " +
                        "executionId={}",
                telemetryNames, categories, executionId);

        long startTime = System.currentTimeMillis();
        String medianSql = String.format("""
              SELECT n.TLM_NAME,
                     %s AS TLM_VALUE
                FROM RESET_DATA d, TLM_NAME_DIM n
               WHERE d.TLM_NAME_ID = n.ID
                 AND n.TLM_NAME IN (%s)
                 AND d.CATEGORY IN (%s)
                 AND d.EXECUTION_ID = ?
                 AND (%s)
            GROUP BY n.TLM_NAME
          """,
                dbMedianCalculator.medianExpression(),
                telemetryPlaceholders,
                categoryPlaceholders,
                criteria.sql()
        );
        var result = queryTelemetryStats(sql, params, medianSql, params, telemetryNames);
        long endTime = System.currentTimeMillis();
        log.info("jdbcTemplate.query took {} ms", (endTime - startTime));
        return result;
    }

    public TelemetryStatistics getTelemetryStats(
            String telemetryName,
            String category,
            long executionId,
            Collection<TelemetryValueCriterion> telemetryValueCriteria
    ) {
        SqlWithParams criteria = criteriaSqlBuilder.build(telemetryValueCriteria);
        String sql = String.format("""
            SELECT n.TLM_NAME,
                   d.TLM_VALUE
              FROM RESET_DATA d, TLM_NAME_DIM n
             WHERE d.TLM_NAME_ID = n.ID
               AND n.TLM_NAME = ?
               AND d.CATEGORY = ?
               AND d.EXECUTION_ID = ?
               AND (%s)
          """, criteria.sql());

        List<Object> params = new ArrayList<>();
        params.add(telemetryName);
        params.add(category);
        params.add(executionId);
        params.addAll(criteria.params());

        log.info("sql:\n{}", sql);
        log.info("parameters:\n" +
                        "telemetryName={}, " +
                        "category={}, " +
                        "executionId={}",
                telemetryName, category, executionId);

        long startTime = System.currentTimeMillis();
        String medianSql = String.format("""
            SELECT n.TLM_NAME,
                   %s AS TLM_VALUE
              FROM RESET_DATA d, TLM_NAME_DIM n
             WHERE d.TLM_NAME_ID = n.ID
               AND n.TLM_NAME = ?
               AND d.CATEGORY = ?
               AND d.EXECUTION_ID = ?
               AND (%s)
            GROUP BY n.TLM_NAME
          """,
                dbMedianCalculator.medianExpression(),
                criteria.sql()
        );
        var result = querySingleTelemetryStats(sql, params, medianSql, params, telemetryName);
        long endTime = System.currentTimeMillis();
        log.info("jdbcTemplate.query took {} ms", (endTime - startTime));
        return result;
    }


    private List<TelemetryStatistics> queryTelemetryStats(
            String sql,
            List<Object> params,
            String medianSql,
            List<Object> medianParams,
            Collection<String> preferredOrder
    ) {
        Map<String, TelemetryStatisticsAccumulator> accumulators = jdbcTemplate.query(sql, rs -> {
            Map<String, TelemetryStatisticsAccumulator> map = new LinkedHashMap<>();
            while (rs.next()) {
                String telemetryName = rs.getString("TLM_NAME");
                Double value = rs.getObject("TLM_VALUE") == null ? null : rs.getDouble("TLM_VALUE");
                if (telemetryName == null || value == null || !Double.isFinite(value)) {
                    continue;
                }
                map.computeIfAbsent(telemetryName, key -> new TelemetryStatisticsAccumulator()).add(value);
            }
            return map;
        }, params.toArray());
        Map<String, Double> medianByTelemetry = dbMedianCalculator.queryMedian(medianSql, medianParams);
        return TelemetryStatisticsCalculator.aggregateStreaming(accumulators, medianByTelemetry, preferredOrder);
    }

    private TelemetryStatistics querySingleTelemetryStats(
            String sql,
            List<Object> params,
            String medianSql,
            List<Object> medianParams,
            String telemetryName
    ) {
        var stats = queryTelemetryStats(sql, params, medianSql, medianParams, List.of(telemetryName));
        return stats.stream()
                .findFirst()
                .orElseThrow(() -> new EmptyResultDataAccessException(1));
    }

}
