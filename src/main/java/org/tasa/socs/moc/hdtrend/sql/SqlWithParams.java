package org.tasa.socs.moc.hdtrend.sql;

import java.util.List;

public record SqlWithParams(String sql, List<Object> params) {
}
