package com.meo.sqlWrapper.core;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.impl.CaretModelImpl;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Meo
 * @date 2023-06-15
 * @path com.meo.sqlWrapper.core.WrapperAction
 */
public class WrapperAction extends AnAction {
    private static final Logger logger = Logger.getInstance(WrapperAction.class);

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

        if (formattedLines.isEmpty()) {
            return;
        }

        // delete selected text
        WriteCommandAction.runWriteCommandAction(project, () -> {
            document.deleteString(primaryCaret.getSelectionStart(), primaryCaret.getSelectionEnd());
        });

        // move the caret to the start position of the current line
        // insert multiple lines with same indent
        Caret currCaret = editor.getCaretModel().getPrimaryCaret();
        int startOffset = currCaret.getOffset();
        int lineNumber = document.getLineNumber(startOffset);
        int lineStartOffset = document.getLineStartOffset(lineNumber);
        int columnOffset = startOffset - lineStartOffset;
        WriteCommandAction.runWriteCommandAction(project, () -> {
            int offsetCurr = startOffset;
            for (String line : formattedLines) {
                insertNewLine(event);
                document.insertString(offsetCurr, line);
                offsetCurr += columnOffset + line.length() + 1;
            }
        });

        // move the caret to the end of the inserted text
        // select the inserted lines, then format them
        currCaret = editor.getCaretModel().getPrimaryCaret();
        int endOffset = currCaret.getOffset();

        SelectionModel selectionModel = editor.getSelectionModel();
        selectionModel.setSelection(startOffset, endOffset);
        System.out.println("===selectionModel.getSelectedText():" + selectionModel.getSelectedText());
        reformatJavaFile(event, lineStartOffset, endOffset);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        // Get required data keys
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);

        // get the current document file name
        String fileName = event.getData(CommonDataKeys.PSI_FILE).getName();

        // Set visibility only in the case of
        // existing project editor, and selection
        event.getPresentation().setEnabledAndVisible(project != null
                && editor != null && StringUtils.endsWith(fileName, ".java") && editor.getSelectionModel().hasSelection());
    }

    private void insertNewLine(AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        EditorActionManager actionManager = EditorActionManager.getInstance();
        EditorActionHandler enterHandler = actionManager.getActionHandler(IdeActions.ACTION_EDITOR_ENTER);

        // Execute the Enter action to create a new line
        CaretModel caretModel = editor.getCaretModel();
        enterHandler.execute(editor, caretModel.getCurrentCaret(), event.getDataContext());
    }

    private static void reformatJavaFile(AnActionEvent event, int startOffset, int endOffset) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        assert editor != null;

        WriteCommandAction.runWriteCommandAction(editor.getProject(), () -> {
            PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(editor.getProject());
            psiDocumentManager.commitDocument(editor.getDocument());

            PsiFile psiFile = psiDocumentManager.getPsiFile(editor.getDocument());
            CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(editor.getProject());
            try {
                codeStyleManager.reformatText(psiFile, startOffset, endOffset);
            } catch (Exception e) {
                e.printStackTrace();
            }

            psiDocumentManager.commitDocument(editor.getDocument());
        });

    }
}
