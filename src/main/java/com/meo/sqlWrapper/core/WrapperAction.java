package com.meo.sqlWrapper.core;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
        assert editor != null;
        Document document = editor.getDocument();

        // Work off of the primary caret to get the selection info
        Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
        String text = primaryCaret.getSelectedText();

        boolean isOnlyFormat = isOnlyFormat(event);

        // create the StringBuilder object and define the variable name
        Map<String, String> sqlDefineMap = isOnlyFormat ? new HashMap<>() : checkAndCreateSqlVarName(editor, project, primaryCaret.getSelectionEnd());
        String sqlVarAliasName = sqlDefineMap.get("name");
        String nameDefine = sqlDefineMap.get("nameDefine");

        // get the formatted sql lines
        IFormatter formatter = new ImpSqlFormatter(ProcessConfig.build().setVarAliasName(sqlVarAliasName).setFormatType(isOnlyFormat));
        List<String> formattedLines = formatter.formatSql(text);
        if (formattedLines.isEmpty()) {
            return;
        }
        if (StringUtils.isNotBlank(nameDefine)) {
            formattedLines.add(0, nameDefine);
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
                insertNewLine(editor, event.getDataContext());
                document.insertString(offsetCurr, line);
                offsetCurr += columnOffset + line.length() + 1;
            }
        });

        // move the caret to the end of the inserted text
        // select the inserted lines, then format them
        currCaret = editor.getCaretModel().getPrimaryCaret();
        int endOffset = currCaret.getOffset();
        reformatJavaFile(editor, project, lineStartOffset, endOffset);
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        // Get required data keys
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);

        // get the current document file name
        String fileName = Objects.requireNonNull(event.getData(CommonDataKeys.PSI_FILE)).getName();

        // Set visibility only in the case of
        // existing project editor, and selection
        event.getPresentation().setEnabledAndVisible(project != null
                && editor != null && editor.getSelectionModel().hasSelection());
    }

    /**
     * if the current file is .java, format and wrap the sql, or only format the sql
     */
    private boolean isOnlyFormat(AnActionEvent event) {
        String fileName = Objects.requireNonNull(event.getData(CommonDataKeys.PSI_FILE)).getName();
        return !StringUtils.endsWith(fileName, ".java");
    }

    private void insertNewLine(Editor editor, DataContext dataContext) {
        EditorActionManager actionManager = EditorActionManager.getInstance();
        EditorActionHandler enterHandler = actionManager.getActionHandler(IdeActions.ACTION_EDITOR_ENTER);

        // Execute the Enter action to create a new line
        CaretModel caretModel = editor.getCaretModel();
        enterHandler.execute(editor, caretModel.getCurrentCaret(), dataContext);
    }

    private static void reformatJavaFile(Editor editor, Project project, int startOffset, int endOffset) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
            psiDocumentManager.commitDocument(editor.getDocument());

            PsiFile psiFile = psiDocumentManager.getPsiFile(editor.getDocument());
            CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
            try {
                assert psiFile != null;
                codeStyleManager.reformatText(psiFile, startOffset, endOffset);
            } catch (Exception e) {
                e.printStackTrace();
            }

            psiDocumentManager.commitDocument(editor.getDocument());
        });
    }

    private static Map<String, String> checkAndCreateSqlVarName(Editor editor, Project project, int lineEndOffset) {
        List<String> avaliableVarNameList = new ArrayList<>(0);
        List<String> excludeVarNameList = new ArrayList<>(0);
        Map<String, String> map = new HashMap<>();
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        assert psiFile != null;
        PsiElement psiElement = psiFile.findElementAt(lineEndOffset);
        if (psiElement != null) {
            // get the current codes of method
            PsiMethod containingMethod = PsiTreeUtil.getParentOfType(psiElement, PsiMethod.class);
            assert containingMethod != null;
            // visit the method's variables to find the local variable which name is StringBuilder/StringBuffer
            containingMethod.accept(new JavaRecursiveElementVisitor() {
                @Override
                public void visitLocalVariable(PsiLocalVariable variable) {
                    super.visitLocalVariable(variable);
                    if (StringUtils.endsWith(variable.getType().toString(), "StringBuilder")
                            || StringUtils.endsWith(variable.getType().toString(), "StringBuffer")) {
                        excludeVarNameList.add(variable.getName());
                        // skip the variable if it over the format sql line
                        if (variable.getLastChild().getTextRange().getStartOffset() <= lineEndOffset) {
                            avaliableVarNameList.add(variable.getName());
                        } else {
                            excludeVarNameList.add(variable.getName());
                        }
                    }
                }
            });
        }
        String sqlVarName = createSqlVarName(avaliableVarNameList, excludeVarNameList);
        String sql = "StringBuilder " + sqlVarName + " = new StringBuilder();";
        if (avaliableVarNameList.contains(sqlVarName)) {
            sql = null;
        }
        map.put("name", sqlVarName);
        map.put("nameDefine", sql);
        return map;
    }

    private static String createSqlVarName(List<String> avaliableList, List<String> excludeList) {
        if (!avaliableList.isEmpty()) {
            return avaliableList.get(0);
        } else if (excludeList.isEmpty()) {
            return "sql";
        } else {
            List<String> nameList = new ArrayList<>(Arrays.asList("sql", "sqlStr", "querySql"));
            nameList.removeAll(excludeList);
            if (nameList.isEmpty()) {
                int idx = 0;
                String name = "sql" + idx;
                while (excludeList.contains(name)) {
                    name = "sql" + ++idx;
                }
                return name;
            } else {
                return nameList.get(0);
            }
        }
    }
}
