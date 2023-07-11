package com.meo.sqlWrapper.core;

import com.github.vertical_blank.sqlformatter.core.FormatConfig;
import com.github.vertical_blank.sqlformatter.languages.Dialect;
import org.apache.commons.lang.StringUtils;

/**
 * @author Meo
 * @date 2022/9/18
 * @path com.meo.sqlWrapper.handler.ProcessConfig
 */
public class ProcessConfig {
    public static final String SQL_FORMAT_INDENT = "    ";
    public static final int SQL_FORMAT_MAX_LEN_IN_LINE = 100;
    public static final boolean SQL_FORMAT_UPPERCASE = true;
    public static final Dialect SQL_FORMAT_DIALECT_MYSQL = Dialect.MySql;

    private String indent = SQL_FORMAT_INDENT;
    private int maxLen = SQL_FORMAT_MAX_LEN_IN_LINE;
    private boolean upperCase = SQL_FORMAT_UPPERCASE;
    private Dialect dialect = SQL_FORMAT_DIALECT_MYSQL;

    public static final String DEFAULT_VAR_ALIAS_NAME = "sql";

    private static ProcessConfig instance;

    private String varAliasName = DEFAULT_VAR_ALIAS_NAME;
    private boolean isOnlyFormat = false;

    private ProcessConfig() {
    }

    public static ProcessConfig build() {
        if (instance == null) {
            instance = new ProcessConfig();
        }
        return instance;
    }

    public void copy(ProcessConfig cfg) {
        instance = cfg;
    }

    public FormatConfig createFormatWithDefaultConfig() {
        return FormatConfig.builder().indent(indent).maxColumnLength(maxLen).uppercase(upperCase).build();
    }

    public String getIndent() {
        return indent;
    }

    public int getMaxLen() {
        return maxLen;
    }

    public boolean isUpperCase() {
        return upperCase;
    }

    public Dialect getDefaultDialect() {
        return dialect;
    }

    public ProcessConfig getInstance() {
        return instance;
    }

    public String getVarAliasName() {
        return varAliasName;
    }

    public ProcessConfig setVarAliasName(String varAliasName) {
        if (StringUtils.isNotBlank(varAliasName)) {
            this.varAliasName = varAliasName;
        }
        return this;
    }

    public boolean isOnlyFormat() {
        return isOnlyFormat;
    }

    public ProcessConfig setFormatType(boolean isOnlyFormat) {
        this.isOnlyFormat = isOnlyFormat;
        return this;
    }

}
