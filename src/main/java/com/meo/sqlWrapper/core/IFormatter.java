package com.meo.sqlWrapper.core;

import java.util.List;

/**
 * @author Meo
 * @date 2023-06-15
 * @path com.meo.sqlWrapper.core.IFormater
 */
public interface IFormatter {
    List<String> formatSql(String text);
}
