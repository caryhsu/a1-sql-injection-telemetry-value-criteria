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
2. `NormalTelemetryStatsRepositoryH2Test`(3 測試,新增):用真實 H2 跑 production 查詢路徑 —— 合法 criteria 正確回傳;注入字串(`TEMP1' OR '1'='1`)回 0 筆，證明被當字面值綁定
3. 端對端:`mvn spring-boot:run` + curl,注入 payload 回 `[]`(驗證時間 2026-07-20)

## 與 production 的差異(已於 README.md §4 聲明)

- Controller/Service/Config 裁掉與本 finding 無關的端點與方法(query、batchQueryReset、Reset 相關);保留的 method body 與 production 一致
- `NormalTelemetryStatsRepository` **整檔逐字複製**
- 新增 repro 專用鷹架:`ReproApplication`、`ReproJacksonConfig`(空殼)、`lombok.config`、`schema.sql`/`data.sql`(合成資料)、`application.properties`(無任何連線設定)

## 內部觀察(不影響 SAST 判定,未寫進廠商 README)

- production 無任何 Jackson 客製;`TelemetryValueCriterion` 雙建構子在沒有 `@ConstructorProperties` 時,JSON 物件反序列化會 400 —— 此為**潛在的既有行為**,與 SQL Injection 判定無關,前端實際走的是 `/api/tlm-statistics`。repro 用 `lombok.config` 讓 curl 可演示,不改任何原始碼。
- Spring Boot 4 用 Jackson 3(`tools.jackson.*`),`com.fasterxml.jackson.databind` 不存在 —— 後續 repro 專案若需 Jackson 客製,勿引用舊 package。

## 寄送前檢查清單

- [x] 無 yaml、無 user/password/token、無內部主機位址
- [x] `mvn test` 全綠(9/9)
- [x] `mvn spring-boot:run` 可啟動,endpoint 可演示
- [ ] 壓縮前刪除 `target/`(`mvn clean` 即可)
