# A1 重現專案說明(內部參考)

> 廠商向文件見 `README.md`(英文)。本檔供內部快速了解。

## 對應 finding

- **A1**:SQL_Injection(CWE-89,嚴重),`TelemetryStatsQueryController.java:56` `batchQuery`,SimilarityId `-2029866169`
- 首次出現:hd-trend2-0717.xml(掃描 2026-07-16);0720 仍持續(工具狀態已轉「反復出現的」)
- FIX-0717 判定:**Likely false positive**(commit `c699b1379`)

## 誤判理由(一句話)

`name`/`min`/`max` 全部走 `?` bind 參數;SQL 本文只由常數欄位名(建構子注入 `"n.TLM_NAME"`/`"d.TLM_VALUE"`)與 `?` 佔位符組成。工具把裝著受汙染 `params` 的 `SqlWithParams` 物件**整個**當成受汙染(含常數 `sql` 欄位),屬欄位層級誤判。

## 本專案如何證明

1. `TelemetryValueCriteriaSqlBuilderTest`(6 測試,與 production 逐字相同):斷言產生的 SQL 只有 `?`、值全在 `params`
2. `NormalTelemetryStatsRepositoryH2Test`(3 測試,新增):用真實 H2 跑 production 查詢路徑 —— 合法 criteria 正確回傳;注入字串(`TEMP1' OR '1'='1`)回 0 筆,證明被當字面值綁定
3. 端對端:`mvn spring-boot:run` + curl,注入 payload 回 `[]`(驗證時間 2026-07-20)

## 與 production 的差異(已於 README.md §4 聲明)

- **2026-07-20 v2:全鏈路逐字還原**。因首次投遞掃描未觸發,已把 controller(3 端點全)、service(3 方法全)、`ResetTelemetryStatsRepository`、完整 `TelemetryStatisticsCalculator` 家族、`TelemetryTimeParser` 全部改為 **production 逐字複製(`cp` 保留 CRLF,`cmp` 驗證位元一致)**;39 節點 taint path 涉及的檔案與方法 100% 對齊 production
- 唯一 Trimmed:`TelemetryRepositoryConfig`(只留 normal/reset 兩個 stats bean;production 另有 value/time-range/aggregate repository bean,與本 finding 無關)
- 新增 repro 專用鷹架:`ReproApplication`、`ReproJacksonConfig`(空殼)、`lombok.config`、`schema.sql`/`data.sql`(合成資料,含 RESET_DATA)、`application.properties`(無任何連線設定)

## 首次投遞未觸發的排查結論(v2 依據)

- 從 0717 XML 抽出完整 **39 節點 taint path**,只涉及 4 檔:Controller、ServiceImpl、NormalTelemetryStatsRepository、TelemetryValueCriteriaSqlBuilder —— v1 皆已保留,但 controller/service 為裁減版
- 逐 byte 比對:v1 鏈路檔案僅換行差異(LF vs production CRLF),內容一致 → 未觸發原因傾向①掃描 preset 未含 query id 594(Java Critical Risk\SQL Injection)②周邊程式碼差異影響引擎啟發式
- v2 對策:全鏈路檔案逐字還原(含 CRLF)消除②;README 加註掃描指引(同 preset OWASP TOP 10 - 2021、全量掃描)消除①
- 若 v2 仍不觸發,基本可確立是掃描設定差異,請廠商以同 preset 複掃或直接以 0717/0720 原報告判定

## 內部觀察(不影響 SAST 判定,未寫進廠商 README)

- production 無任何 Jackson 客製;`TelemetryValueCriterion` 雙建構子在沒有 `@ConstructorProperties` 時,JSON 物件反序列化會 400 —— 此為**潛在的既有行為**,與 SQL Injection 判定無關,前端實際走的是 `/api/tlm-statistics`。repro 用 `lombok.config` 讓 curl 可演示,不改任何原始碼。
- Spring Boot 4 用 Jackson 3(`tools.jackson.*`),`com.fasterxml.jackson.databind` 不存在 —— 後續 repro 專案若需 Jackson 客製,勿引用舊 package。
- GET `/api/tlm-stats/{tlmName}` 的 `start`/`end` 參數走 `TelemetryTimeParser.parse`,ISO-8601 instant 格式(如 `2026-01-01T00:00:00Z`)才能解析;純日期會 500(與 production 行為一致)

## 寄送前檢查清單

- [x] 無 yaml、無 user/password/token、無內部主機位址
- [x] `mvn test` 全綠(9/9)
- [x] `mvn spring-boot:run` 可啟動,三個 endpoint 皆可演示(2026-07-20 v2 驗證)
- [ ] 壓縮前刪除 `target/`(`mvn clean` 即可)
