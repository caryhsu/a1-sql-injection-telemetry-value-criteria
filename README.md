# Checkmarx Finding Reproduction — SQL_Injection (CWE-89)

**Standalone, self-contained reproduction of a single Checkmarx SAST finding, provided for vendor analysis.**
This project contains only the code involved in the flagged data flow. It builds, runs, and can be scanned on its own — no other part of the production system is required.

---

## 1. Finding under analysis

| Item | Value |
|---|---|
| Product | Checkmarx One / CxSAST 9.7.6.1003 HF10 |
| Query | `SQL_Injection` (Java Critical Risk, query version 56152908) |
| CWE | CWE-89 |
| Severity | Critical |
| Preset | OWASP TOP 10 - 2021 |
| Flagged location | `TelemetryStatsQueryController.java:56` (`batchQuery` method) |
| SimilarityId | `-2029866169` |
| Source / sink | `batchQuery` → `batchQuery` (per report) |
| Detection history | First flagged in incremental scan 2026-07-16 (ScanId 1000300); still flagged in scan 2026-07-20 (ScanId 1000301) |

The flagged taint flow (as reconstructed from the report path):

```
TelemetryStatsQueryController.batchQuery(@RequestBody TlmStatsBatchRequest req)      [taint source]
  └─ TelemetryStatsQueryServiceImpl.batchQuery(..., criteria)
       └─ NormalTelemetryStatsRepository.getTelemetryStats(..., telemetryValueCriteria)
            ├─ TelemetryValueCriteriaSqlBuilder.build(criteria)  →  SqlWithParams(sql, params)
            ├─ String.format(SQL_TEMPLATE, ..., criteria.sql())                      [flagged concatenation]
            └─ jdbcTemplate.query(sql, extractor, params.toArray())                  [sink]
```

## 2. Our assessment: we believe this is a false positive

**Every user-controlled value is passed to the JDBC driver as a bound `?` parameter. The SQL text is assembled exclusively from compile-time constants.**

Evidence (all code in this project is byte-identical to production, see §4):

1. `TelemetryValueCriteriaSqlBuilder` (`src/main/java/org/tasa/socs/moc/hdtrend/model/tlmfilter/TelemetryValueCriteriaSqlBuilder.java:35-47`):
   - `criterion.getName()` / `getMin()` / `getMax()` — the only user-controlled values — are appended to `params`, never to the SQL string.
   - The SQL fragment is built from `nameColumn` / `valueColumn`, which are **constructor-injected constants**: the only instantiation in the codebase is `new TelemetryValueCriteriaSqlBuilder("n.TLM_NAME", "d.TLM_VALUE")` (`NormalTelemetryStatsRepository.java:28-29`).
2. `NormalTelemetryStatsRepository` (`src/main/java/org/tasa/socs/moc/hdtrend/tlmstats/repository/NormalTelemetryStatsRepository.java:49-66`):
   - `telemetryPlaceholders` / `categoryPlaceholders` are strings of `?` generated from collection sizes (`Collections.nCopies(size, "?")`) — no values.
   - `dbMedianCalculator.medianExpression()` returns a per-database constant (`MEDIAN(d.TLM_VALUE)` / `percentile_cont(0.5) within group (order by d.TLM_VALUE)`).
   - `criteria.sql()` therefore contains only constant column names and `?` placeholders; `criteria.params()` carries the user input, and is passed via `params.toArray()` to `jdbcTemplate.query(...)`.
3. **Runtime proof (included in this project)**: `src/test/java/.../NormalTelemetryStatsRepositoryH2Test.java` executes the exact production query path against a real (embedded H2) database:
   - a legitimate criterion returns correct statistics;
   - an injection-shaped criterion name (`TEMP1' OR '1'='1`) returns **zero rows** — the payload is bound as a literal value, not interpreted as SQL;
   - an injection-shaped telemetry name (`TEMP1' OR '1'='1' --`) likewise returns zero rows.
4. **Unit proof**: `src/test/java/.../TelemetryValueCriteriaSqlBuilderTest.java` (6 tests, byte-identical to production) asserts the generated SQL contains only `?` placeholders and that values land in `params`.

**Our hypothesis about why the query fires:** the tainted `params` list is stored in the `SqlWithParams` record together with the constant `sql` string; the engine appears to treat the whole `SqlWithParams` object — including its `sql` component — as tainted, so `String.format(template, ..., criteria.sql())` is flagged. This looks like an object/field-level taint-granularity limitation rather than a real injectable flow.

## 3. What we ask the vendor

1. Confirm whether this flow is a false positive of `SQL_Injection` under the current query implementation.
2. If confirmed, advise the recommended way to mark it (Not Exploitable) durably, and/or whether query tuning / sanitizer registration can make the engine recognize the `SqlWithParams.sql` component as constant.
3. If you assess it as a true positive, please identify which concrete value in the flow can reach the SQL text unbound.

## 4. File mapping (production → this project)

Production modules: `hd-trend-tlm-data`, `hd-trend-tlm-stats`, `hd-trend-commons`. Package names and class/method/field names are unchanged.

**Every file on the flagged taint path — and every file in the controller/service/repository slice — is a byte-for-byte copy of production (CRLF line endings preserved, verified with `cmp`).** The full REST call chain is reproduced end-to-end: all three controller endpoints, all three service methods, both stats repositories.

| File in this project | Provenance |
|---|---|
| `tlmdata/controller/TelemetryStatsQueryController.java` | **Verbatim (complete file, all 3 endpoints: `query`, `batchQuery`, `batchQueryReset`)** |
| `tlmdata/controller/TlmStatsBatchRequest.java`, `TlmResetStatsBatchRequest.java` | Verbatim |
| `tlmdata/service/TelemetryStatsQueryService.java`, `TelemetryStatsQueryServiceImpl.java` | **Verbatim (complete files, all methods)** |
| `tlmdata/config/TelemetryRepositoryConfig.java` | **Trimmed**: only the two stats-repository beans (`normalTelemetryStatsRepository`, `resetTelemetryStatsRepository`) kept; production additionally registers value / time-range / aggregate repositories from unrelated packages |
| `tlmstats/repository/NormalTelemetryStatsRepository.java` | **Verbatim (complete file)** |
| `tlmstats/repository/ResetTelemetryStatsRepository.java` | **Verbatim (complete file)** |
| `tlmstats/repository/DbMedianCalculator.java`, `AbstractDbMedianCalculator.java`, `DbMedianCalculator_H2.java`, `DbMedianCalculator_POSTGRES.java`, `DbMedianCalculatorFactory.java` | Verbatim |
| `tlmstats/model/TelemetryStatistics.java`, `TelemetryStatisticsAccumulator.java`, `TelemetryStatisticsCalculator.java`, `TelemetryStatisticsComputation.java`, `DefaultTelemetryStatisticsCalculator.java`, `AbstractTelemetryStatisticsCalculator.java` | Verbatim |
| `model/tlmfilter/TelemetryValueCriteriaSqlBuilder.java`, `TelemetryValueCriterion.java` | **Verbatim (the core of the finding)** |
| `model/SohType.java`, `sql/SqlWithParams.java`, `time/TelemetryTimeParser.java` | Verbatim |
| `ReproApplication.java`, `config/ReproJacksonConfig.java`, `lombok.config`, `schema.sql`, `data.sql`, `application.properties` | **Repro-only scaffolding** — does not exist in production; only needed to boot/run the demo |

**Scan guidance:** to reproduce the finding, scan this project with the **same preset (`OWASP TOP 10 - 2021`) and a full scan** (the production finding is query id 594, `Java\Cx\Java Critical Risk\SQL Injection 版本:2`; a preset without this query will not report it). Expected flow as in §1.

**Confidentiality:** this project intentionally contains **no YAML, no credentials, no secrets of any kind**. The datasource is an embedded in-memory H2 database auto-configured by Spring Boot; `application.properties` holds no connection settings. Table/column names match the production schema, sample rows are synthetic.

Build parity: Java 21, Spring Boot 4.0.6, Lombok, `maven-compiler-plugin` `<parameters>true</parameters>` — same as the production build. `lombok.config` adds `@ConstructorProperties` so reviewers can POST JSON to the endpoint; it does not alter source code.

## 5. Build, run, scan

Requires JDK 21+ and Maven 3.9+.

```bash
# unit + runtime proof (9 tests)
mvn test

# boot the demo service (embedded H2, synthetic data, port 8080)
mvn spring-boot:run

# legitimate request → returns statistics for values in [15, 100]
curl -X POST http://localhost:8080/api/tlm-stats/batch \
  -H "Content-Type: application/json" \
  -d '{"sohTypes":["VC0"],"startMs":1767139200000,"endMs":1767312000000,"series":["TEMP1"],"telemetryValueCriteria":[{"name":"TEMP1","min":15.0,"max":100.0}]}'

# injection-shaped input → bound as a literal value, returns []
curl -X POST http://localhost:8080/api/tlm-stats/batch \
  -H "Content-Type: application/json" \
  -d '{"sohTypes":["VC0"],"startMs":1767139200000,"endMs":1767312000000,"series":["TEMP1"],"telemetryValueCriteria":[{"name":"TEMP1'"'"' OR '"'"'1'"'"'='"'"'1","min":0.0,"max":999999.0}]}'

# the other two production endpoints work as well (ISO-8601 instants for GET)
curl "http://localhost:8080/api/tlm-stats/TEMP1?sohType=VC0&start=2025-12-31T00:00:00Z&end=2026-01-02T00:00:00Z"
curl -X POST http://localhost:8080/api/tlm-stats/reset/batch \
  -H "Content-Type: application/json" \
  -d '{"sohTypes":["VC0"],"importFileName":"sample-import.tdp","segmentIndex":0,"series":["TEMP1"],"telemetryValueCriteria":[{"name":"TEMP1","min":15.0,"max":100.0}]}'
```

To reproduce the finding: scan this project with the same preset (OWASP TOP 10 - 2021). Expected: `SQL_Injection` at `TelemetryStatsQueryController.batchQuery` / `NormalTelemetryStatsRepository`, same flow as §1.

## 6. Contact

Prepared by the hd-trend2 development team (TASA), 2026-07-20. Production finding details: `sast/checkmarx/` in the hd-trend2 repository (not required to analyze this project).
