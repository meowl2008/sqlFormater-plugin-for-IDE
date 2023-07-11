package com.meo.sqlWrapper.core;

import java.util.List;

/**
 * @author Meo
 * @date 2023-06-15
 * @path com.meo.sqlWrapper.core.IFormater
 */
public interface IFormatter {
    ProcessConfig formatCfg = ProcessConfig.build();

    public default void setFormatConfig(ProcessConfig cfg) {
        formatCfg.copy(cfg);
    }

    List<String> formatSql(String text);

    default boolean validateSql(String text) {
        return true;
    }
}
