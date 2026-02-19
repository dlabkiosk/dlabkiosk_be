package com.moduletest.deasungkioskbackend.common.config;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;

import org.hibernate.engine.jdbc.internal.FormatStyle;

public class P6SpyFormatter implements MessageFormattingStrategy {

    @Override
    public String formatMessage(int connectionId, String now, long elapsed,
                                String category, String prepared, String sql, String url) {

        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }

        // commit, rollback 제외
        if ("commit".equalsIgnoreCase(category) || "rollback".equalsIgnoreCase(category)) {
            return "";
        }

        // SQL 포맷팅
        String formattedSql = formatSql(sql);

        return String.format("\n실행시간: %dms | 연결: %d | 시간: %s\n%s\n",
                elapsed, connectionId, now, formattedSql);
    }

    private String formatSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        try {
            String trimmedSql = sql.trim().toLowerCase();
            if (trimmedSql.startsWith("select") || trimmedSql.startsWith("insert")
                    || trimmedSql.startsWith("update") || trimmedSql.startsWith("delete")) {
                return FormatStyle.BASIC.getFormatter().format(sql);
            }
            return sql;
        } catch (Exception e) {
            return sql;
        }
    }
}
