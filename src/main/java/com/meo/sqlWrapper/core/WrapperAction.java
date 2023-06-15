package com.meo.sqlWrapper.core;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Meo
 * @date 2023-06-15
 * @path com.meo.sqlWrapper.core.WrapperAction
 */
public class WrapperAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        // Get all the required data from data keys
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        Document document = editor.getDocument();

        // Work off of the primary caret to get the selection info
        Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
        String text = primaryCaret.getSelectedText();
        IFormatter formatter = new ImpSqlFormatter();
        List<String> formattedLines = formatter.formatSql(text);

        // Get the caret information
        LogicalPosition logicalPos = primaryCaret.getLogicalPosition();
        VisualPosition visualPos = primaryCaret.getVisualPosition();
        int caretOffset = primaryCaret.getOffset();

    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        // Get required data keys
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);

        // Set visibility only in the case of
        // existing project editor, and selection
        event.getPresentation().setEnabledAndVisible(project != null
                && editor != null && editor.getSelectionModel().hasSelection());
    }
}
