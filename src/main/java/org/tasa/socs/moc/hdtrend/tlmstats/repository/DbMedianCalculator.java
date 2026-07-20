package org.tasa.socs.moc.hdtrend.tlmstats.repository;

import java.util.List;
import java.util.Map;

interface DbMedianCalculator {

    String medianExpression();

    Map<String, Double> queryMedian(String sql, List<Object> params);
}
