package com.meo.sqlWrapper.core;

import com.github.vertical_blank.sqlformatter.SqlFormatter;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Meo
 * @date 2023-06-15
 * @path com.meo.sqlWrapper.core.ImpSqlFormater
 */
public class ImpSqlFormatter implements IFormatter {
    private static final Logger logger = Logger.getInstance(ImpSqlFormatter.class);
    private ProcessConfig cfg = ProcessConfig.build();

    @Override
    public List<String> formatSql(String text) {
        List<String> lines = new ArrayList<>(0);
        try {
            String formatSql = SqlFormatter.of(cfg.getDefaultDialect()).format(text, cfg.createFormatConfig());
            logger.debug(formatSql);
            lines = Arrays.stream(StringUtils.split(formatSql, '\n')).map(StringUtils::trim).collect(Collectors.toList());
        } catch (Exception e) {
            logger.debug("Failed sql:" + text);
            logger.error("Format Failed.", e);
        }
        return updateLines(lines);
    }

    private List<String> updateLines(List<String> lines) {
        String sbObjName = "sql";
        String prefix = sbObjName + ".append(" + "\" ";
        String suffix = " \");\n";
        return lines.stream().filter(StringUtils::isNotBlank).map(line -> prefix + line + suffix).collect(Collectors.toList());
    }
}
