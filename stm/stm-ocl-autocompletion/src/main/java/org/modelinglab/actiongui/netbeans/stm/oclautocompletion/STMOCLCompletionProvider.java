/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.stm.oclautocompletion;

import java.io.IOException;
import java.util.Collection;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.modelinglab.actiongui.netbeans.stm.oclautocompletion.exceptions.STMAutocompletionException;
import org.modelinglab.ocl.core.ast.expressions.OclExpression;
import org.modelinglab.ocl.core.exceptions.OclException;
import org.modelinglab.ocl.parser.OclLexerException;
import org.modelinglab.ocl.parser.OclParser;
import org.modelinglab.ocl.parser.OclParserException;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
@MimeRegistration(mimeType = "text/x-stm", service = CompletionProvider.class)
public class STMOCLCompletionProvider implements CompletionProvider{

    @Override
    public CompletionTask createTask(int queryType, final JTextComponent jtc) {
        if(queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return null;
        }
        return new AsyncCompletionTask(new AsyncCompletionQuery() {

            @Override
            protected void query(CompletionResultSet completionResultSet, Document document, int caretOffset) {                 
                
                // 1) Check the cursor position is within an authorization constraint declaration (i.e. between '[' and ']')
                boolean validPosition = false;
                try {
                    validPosition = isValidPosition(document, caretOffset);
                } 
                catch (BadLocationException ex) {
                    //Exceptions.printStackTrace(ex);
                    completionResultSet.finish();
                    return;
                }
                
                if (!validPosition) {
                    //JOptionPane.showMessageDialog(jtc, "OCL auto-completion feaure is enabled only when an " +
                    //    "authorization constraint is being defined (i.e between '[' and ']').");
                    completionResultSet.finish();
                    return;
                }                                
                
                // 2) Get the OCL parser.
                OCLParserProvider oclParserProvider = OCLParserProvider.getInstance();
                OclParser parser;
                try {
                    parser = oclParserProvider.getParser(document, caretOffset);
                } 
                catch (STMAutocompletionException ex) {
                    completionResultSet.finish();
                    return;
                }
                
                // 3) Get the collection of available items for autocompletion
                Collection<CompletionItem> completionItems;
                try {
                    completionItems = buildCompletionItems(document, caretOffset, parser);
                }
                catch (BadLocationException ex) {
                    completionResultSet.finish();
                    return;
                }
                
                completionResultSet.addAllItems(completionItems);
                completionResultSet.finish();

            }
        }, jtc);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent jtc, String string) {
        // a dot '.' operator has been typed
        if(string.equals(".")){
            return COMPLETION_QUERY_TYPE;
        }
        // an arrow '->' operator has been typed
        StyledDocument doc = (StyledDocument) jtc.getDocument();
        try {
            String text = doc.getText(jtc.getCaretPosition() - 2, 2);
            if(text.equals("->")) {
                return COMPLETION_QUERY_TYPE;
            }
        } 
        catch (BadLocationException ex) {
            return 0;
        }
        return 0;
    }
    
    static int getRowFirstNonWhite(StyledDocument doc, int offset) throws BadLocationException {
        Element lineElement = doc.getParagraphElement(offset);
        int start = lineElement.getStartOffset();
        while (start + 1 < lineElement.getEndOffset()) {
            try {
                if (doc.getText(start, 1).charAt(0) != ' ') {
                    break;
                }
            } catch (BadLocationException ex) {
                throw (BadLocationException)new BadLocationException(
                        "calling getText(" + start + ", " + (start + 1) +
                        ") on doc of length: " + doc.getLength(), start
                        ).initCause(ex);
            }
            start++;
        }
        return start;
    }
    
    static int indexOfWhite(char[] line){
        int i = line.length;
        while(--i > -1){
            final char c = line[i];
            if(Character.isWhitespace(c)){
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Calculates if the position of the cursor is a valid position for making OCL auto-completion.
     * The position will be valid if the cursor is within '[' and ']', since it means an OCL constraint is being defined.
     * @param document
     * @param caretOffset
     * @return true if the position of the cursor is in valid position
     */
    private boolean isValidPosition(Document document, int caretOffset) throws BadLocationException {
        // find '['
        String textBefore = document.getText(document.getStartPosition().getOffset(), caretOffset);
        boolean leftSquareBracketFound = false;
        for (int i = textBefore.length()-1; i >= 0; i--) {
            char charAt = textBefore.charAt(i);
            if(charAt == '['){
                leftSquareBracketFound = true;
                break;
            }
            else if(charAt == ']') {
                break;
            }
        }
        if(!leftSquareBracketFound) {
            return false;
        }
        
        // find ']'
        String textAfter = document.getText(caretOffset, document.getEndPosition().getOffset() - caretOffset);
        boolean rightSquareBracketFound = false;
        for (int i = 0; i < textAfter.length(); i++) {
            char charAt = textAfter.charAt(i);
            if(charAt == ']'){
                rightSquareBracketFound = true;
                break;
            }
            else if(charAt == '['){
                break;
            }
        }
        return rightSquareBracketFound;
    }
    
    /**
     * 
     * @param document
     * @param caretOffset
     * @return The sub-expression before the position of the cursor, until a delimiter character is found
     * @throws BadLocationException 
     */
    private String getTextFromCaretToBreakSymbol(Document document, int caretOffset) throws BadLocationException {
        StringBuilder subExpression = new StringBuilder("");
        String text = document.getText(document.getStartPosition().getOffset(), caretOffset);
        assert text.contains("[");
        StringBuilder textBefore = new StringBuilder(text);
        while (textBefore.length() > 0) {
            if(isDotOperatorSymbol(textBefore)) {
                subExpression.append(textBefore.charAt(textBefore.length()-1));
                textBefore.deleteCharAt(textBefore.length()-1);
                continue;
            }
            if(isArrowOperatorSymbol(textBefore)) {
                subExpression.append(textBefore.charAt(textBefore.length()-1));
                textBefore.deleteCharAt(textBefore.length()-1);
                subExpression.append(textBefore.charAt(textBefore.length()-1));
                textBefore.deleteCharAt(textBefore.length()-1);
                continue;
            }
            if(isBreakSymbol(textBefore)) {
                break;
            } 
            subExpression.append(textBefore.charAt(textBefore.length()-1));
            textBefore.deleteCharAt(textBefore.length()-1);
        }
        subExpression = subExpression.reverse();        
        return subExpression.toString();
    }
    
    private String getTextFromEndToBreakSymbol(StringBuilder sb) throws BadLocationException {
        StringBuilder subExpression = new StringBuilder("");
        while (sb.length() > 0) {
            if(isDotOperatorSymbol(sb)) {
                subExpression.append(sb.charAt(sb.length()-1));
                sb.deleteCharAt(sb.length()-1);
                continue;
            }
            if(isArrowOperatorSymbol(sb)) {
                subExpression.append(sb.charAt(sb.length()-1));
                sb.deleteCharAt(sb.length()-1);
                subExpression.append(sb.charAt(sb.length()-1));
                sb.deleteCharAt(sb.length()-1);
                continue;
            }
            if(isBreakSymbol(sb)) {
                break;
            } 
            subExpression.append(sb.charAt(sb.length()-1));
            sb.deleteCharAt(sb.length()-1);
        }
        subExpression = subExpression.reverse();        
        return subExpression.toString();
    }
    
    private StringBuilder getTextFromCaretToStartSymbol(Document document, int caretOffset) throws BadLocationException {
        StringBuilder sb = new StringBuilder("");
        // find '['
        String textBefore = document.getText(document.getStartPosition().getOffset(), caretOffset);
        assert textBefore.contains("[");
        for (int i = textBefore.length()-1; i >= 0; i--) {
            char charAt = textBefore.charAt(i);
            if(charAt == '['){
                break;
            }
            sb.append(charAt);
        }
        sb = sb.reverse();
        return sb;
    }
    
    private String getOCLExpression(Document document, int caretOffset) throws BadLocationException {
        StringBuilder sb = new StringBuilder("");
        // find '['
        String textBefore = document.getText(document.getStartPosition().getOffset(), caretOffset);
        for (int i = textBefore.length()-1; i >= 0; i--) {
            char charAt = textBefore.charAt(i);
            if(charAt == '['){
                break;
            }
            sb.append(charAt);
        }
        sb = sb.reverse();
        
        // find ']'
        String textAfter = document.getText(caretOffset, document.getEndPosition().getOffset() - caretOffset);
        for (int i = 0; i < textAfter.length(); i++) {
            char charAt = textAfter.charAt(i);
            if(charAt == ']'){
                break;
            }
            sb.append(charAt);
        }   
        return sb.toString();
    }
    
    private Collection<CompletionItem> buildCompletionItems(Document document, int caretOffset, OclParser parser) throws BadLocationException {
        Collection<CompletionItem> completionItems;
        
        // 1) Analyze if the completion case is either dot_operator or arrow_operator
        completionItems = analyzeDotOrArrowOperatorCase(document, caretOffset, parser);
        if(completionItems != null) {
            return completionItems;
        }
        
        // 2) Analyze if the completion case is either other_operator or init_expr
        completionItems = analyzeInitExprOrOtherOperatorCase(document, caretOffset, parser);
        return completionItems;
    }
    
    /*
    private Collection<CompletionItem> getCompletionItems(Document document, int caretOffset, OclParser parser) throws BadLocationException {
        // 1) Analyze the text, looking for the autocompletion case
        STMOCLCompletionState state = null;
        String expBeforeOperator = null;
        StringBuilder accumulator = new StringBuilder("");
        
        // 1.1) Check if autocompletion case is either dot_operator or arrow_operator
        String text = getTextFromCaretToBreakSymbol(document, caretOffset);
        StringBuilder sb = new StringBuilder(text);
        // find the dot or arrow operator
        while (sb.length() > 0) {
            if(isDotOperatorSymbol(sb)) {
                int index = accumulator.length() + 1;
                expBeforeOperator = text.substring(0, text.length() - index);
                state = STMOCLCompletionState.DOT_OPERATOR;
                break;
            }
            if(isArrowOperatorSymbol(sb)) {
                int index = accumulator.length() + 2;
                expBeforeOperator = text.substring(0, text.length() - index);
                state = STMOCLCompletionState.ARROW_OPERATOR;
                break;
            }
            if(!isIdentifierSymbol(sb)) {
                break;
            }
            accumulator.append(sb.charAt(sb.length()-1));
            sb.deleteCharAt(sb.length()-1); 
        }
        
        
        
        // 1.2) Check if autocompletion case is either other_operator or init_expression
        if(state == null) {
            text = getTextFromCaretToStartSymbol(document, caretOffset);
            sb = new StringBuilder(text);
            
            MAL: contraejemplo; 3+2 no tiene por que haber espacios en blanco!! otro ejemplo (3)+1
            while(sb.length() > 0) {
                char charAt = sb.charAt(sb.length()-1);
                if(Character.isWhitespace(charAt)) {
                    break;
                }
                accumulator.append(sb.charAt(sb.length()-1));
                sb.deleteCharAt(sb.length()-1); 
            }
            if(sb.length() > 0) {
                while(sb.length() > 0) {
                    char charAt = sb.charAt(sb.length()-1);
                    if(!Character.isWhitespace(charAt)) {
                        break;
                    }
                    sb.deleteCharAt(sb.length()-1); 
                }
                if(sb.length() > 0) {
                    expBeforeOperator = getTextFromEndToBreakSymbol(sb);
                    state = STMOCLCompletionState.OTHER_OPERATOR;
                }
                else {
                    state = STMOCLCompletionState.INIT_EXPR;
                }
            }
            else {
                state = STMOCLCompletionState.INIT_EXPR;
            }
        }
        assert state != null;
        
        // 2) Build the completion items
        Collection<CompletionItem> completionItems = CompletionItemsProvider.buildCompletionItems(expBeforeOperator, accumulator.toString(), parser, caretOffset, state);
        return completionItems;
        
        -----------------------------
        // 1) Get the sub-expression before the cursor
        String subExpression = getSubExpression(document, caretOffset);
        
        // 2) Search for either an operator symbol, a break symbol, or the beginning of the sub-expression.
        STMOCLCompletionState state = null;
        
        StringBuilder sb = new StringBuilder(subExpression);
        StringBuilder accumulator = new StringBuilder("");
        String expBeforeOperator = null;
        while (sb.length() > 0) {
            if(isDotOperatorSymbol(sb)) {
                int index = accumulator.length() + 1;
                expBeforeOperator = subExpression.substring(0, subExpression.length() - index);
                state = STMOCLCompletionState.DOT_OPERATOR;
                break;
            }
            if(isArrowOperatorSymbol(sb)) {
                int index = accumulator.length() + 2;
                expBeforeOperator = subExpression.substring(0, subExpression.length() - index);
                state = STMOCLCompletionState.ARROW_OPERATOR;
                break;
            }
            if(isBreakSymbol(sb)) {
                state = STMOCLCompletionState.INIT_EXPR;
                break;
            } 
            accumulator.append(sb.charAt(sb.length()-1));
            sb.deleteCharAt(sb.length()-1); 
        }
        if(sb.length() == 0) {
            state = STMOCLCompletionState.INIT_EXPR;
        }
        accumulator = accumulator.reverse();
        
        assert state != null;
        // 3) Build the completion items
        Collection<CompletionItem> completionItems = CompletionItemsProvider.buildCompletionItems(expBeforeOperator, accumulator.toString(), parser, caretOffset, state);
        return completionItems;
        
    }
    */
    
    /**
     * If the auto-completion case is either dot_operator or arrow_operator, then it returns the available collection
     * of completion items.
     * @param document
     * @param caretOffset
     * @param parser
     * @return
     * @throws BadLocationException 
     */
    private Collection<CompletionItem> analyzeDotOrArrowOperatorCase(Document document, int caretOffset, OclParser parser) throws BadLocationException {
        STMOCLCompletionState state = null;
        StringBuilder expBeforeOperator = new StringBuilder("");
        StringBuilder accumulator = new StringBuilder("");
        StringBuilder sb = getTextFromCaretToStartSymbol(document, caretOffset);
        
        // 1) find the dot or arrow operator and build the accumulator
        while (sb.length() > 0) {
            if(isDotOperatorSymbol(sb)) {
                state = STMOCLCompletionState.DOT_OPERATOR;
                sb.deleteCharAt(sb.length()-1);
                break;
            }
            if(isArrowOperatorSymbol(sb)) {
                state = STMOCLCompletionState.ARROW_OPERATOR;
                sb.deleteCharAt(sb.length()-1);
                sb.deleteCharAt(sb.length()-1);
                break;
            }
            if(!isIdentifierSymbol(sb)) {
                break;
            }
            accumulator.append(sb.charAt(sb.length()-1));
            sb.deleteCharAt(sb.length()-1); 
        }
        accumulator = accumulator.reverse();
        if(state == null) {
            return null;
        }
        
        // 2) find the source operator expression
        int numOpenParenthesis = 0;
        int numOpenBrakets = 0;
        while(sb.length() > 0){
            char charAt = sb.charAt(sb.length()-1);
            if(charAt == ')') {
                numOpenParenthesis++;
            }
            else if(charAt == '}'){
                numOpenBrakets++;
            }
            else if(charAt == '(') {
                if(numOpenParenthesis > 0) {
                    numOpenParenthesis--;
                }
                else {
                    break;
                }
            }
            else if(charAt == '{') {
                if(numOpenBrakets > 0) {
                    numOpenBrakets--;
                }
                else {
                    break;
                }
            }
            else if(Character.isWhitespace(charAt) || charAt == ',' || charAt == '|') {
                if(!((numOpenParenthesis + numOpenBrakets) > 0)) {
                    break;
                }
            }
            expBeforeOperator.append(charAt);
            sb = sb.deleteCharAt(sb.length()-1);
        }
        expBeforeOperator = expBeforeOperator.reverse();
        if(expBeforeOperator.length() == 0) {
            return null;
        }
        
        // 3) Parse the source operator expression
        OclExpression expBO;
        try {
            expBO = parser.parse(expBeforeOperator.toString());
        }
        // If the expression does not parse, then no items are provided.
        catch (OclParserException | OclLexerException | IOException | OclException ex) {
            return null;
        }
        
        
        Collection<CompletionItem> completionItems = CompletionItemsProvider.buildCompletionItems(expBO, accumulator.toString(), parser, caretOffset, state);
        return completionItems;
    }
    
    /**
     * If the auto-completion case is either init_expr or other_operator, then it returns the available collection
     * of completion items.
     * @param document
     * @param caretOffset
     * @param parser
     * @return
     * @throws BadLocationException 
     */
    private Collection<CompletionItem> analyzeInitExprOrOtherOperatorCase(Document document, int caretOffset, OclParser parser) throws BadLocationException {
        STMOCLCompletionState state;
        StringBuilder expBeforeOperator = new StringBuilder("");
        StringBuilder accumulator = new StringBuilder("");
        StringBuilder sb = getTextFromCaretToStartSymbol(document, caretOffset);
        
        // 1) Build the accumulator
        while (sb.length() > 0) {
            if(!isIdentifierSymbol(sb)) {
                break;
            }
            accumulator.append(sb.charAt(sb.length()-1));
            sb.deleteCharAt(sb.length()-1); 
        }
        accumulator = accumulator.reverse();
        // if there are no more text, we are in init_expr case
        if(sb.length() == 0) {
            state = STMOCLCompletionState.INIT_EXPR;
            Collection<CompletionItem> completionItems = CompletionItemsProvider.buildCompletionItems(null, accumulator.toString(), parser, caretOffset, state);
            return completionItems;
        }
        
        // 2) Delete all initial whitespaces
        while(sb.length() > 0) {
            char charAt = sb.charAt(sb.length()-1);
            if(!Character.isWhitespace(charAt)) {
                break;
            }
            sb.deleteCharAt(sb.length()-1);
        }
        // if there are no more text, we are in init_expr case
        if(sb.length() == 0) {
            state = STMOCLCompletionState.INIT_EXPR;
            Collection<CompletionItem> completionItems = CompletionItemsProvider.buildCompletionItems(null, accumulator.toString(), parser, caretOffset, state);
            return completionItems;
        }

        // 3) find the source operator expression
        int numOpenParenthesis = 0;
        int numOpenBrakets = 0;
        while(sb.length() > 0){
            char charAt = sb.charAt(sb.length()-1);
            if(charAt == ')') {
                numOpenParenthesis++;
            }
            else if(charAt == '}'){
                numOpenBrakets++;
            }
            else if(charAt == '(') {
                if(numOpenParenthesis > 0) {
                    numOpenParenthesis--;
                }
                else {
                    break;
                }
            }
            else if(charAt == '{') {
                if(numOpenBrakets > 0) {
                    numOpenBrakets--;
                }
                else {
                    break;
                }
            }
            else if(Character.isWhitespace(charAt) || charAt == ',' || charAt == '|') {
                if(!((numOpenParenthesis + numOpenBrakets) > 0)) {
                    break;
                }
            }
            expBeforeOperator.append(charAt);
            sb = sb.deleteCharAt(sb.length()-1);
        }
        expBeforeOperator = expBeforeOperator.reverse();
        // if sub expression before caret position is empty, we are in init_expr case 
        if(expBeforeOperator.length() == 0) {
            state = STMOCLCompletionState.INIT_EXPR;
            Collection<CompletionItem> completionItems = CompletionItemsProvider.buildCompletionItems(null, accumulator.toString(), parser, caretOffset, state);
            return completionItems;
        }
        
        // 3) Parse the source operator expression
        OclExpression expBO;
        try {
            expBO = parser.parse(expBeforeOperator.toString());
        }
        // If the expression does not parse, then we are in init_expr case
        catch (OclParserException | OclLexerException | IOException | OclException ex) {
            state = STMOCLCompletionState.INIT_EXPR;
            Collection<CompletionItem> completionItems = CompletionItemsProvider.buildCompletionItems(null, accumulator.toString(), parser, caretOffset, state);
            return completionItems;
        }
        
        // in this case, we have a sub expression before caret position, so we are in other operator case
        state = STMOCLCompletionState.OTHER_OPERATOR;
        Collection<CompletionItem> completionItems = CompletionItemsProvider.buildCompletionItems(expBO, accumulator.toString(), parser, caretOffset, state);
        return completionItems;
    }
    
    private boolean isIdentifierSymbol(StringBuilder sb) {
        assert sb.length() > 0;
        char charLast = sb.charAt(sb.length()-1);
        return Character.isLetterOrDigit(charLast) || charLast == '_';
    }
    
    private boolean isBreakSymbol(StringBuilder sb) {
        assert sb.length() > 0;
        char charLast = sb.charAt(sb.length()-1);
        return !(Character.isLetterOrDigit(charLast) || charLast == '_' || charLast == '\'');
    }

    private boolean isDotOperatorSymbol(StringBuilder sb) {
        assert sb.length() > 0;
        
        return sb.charAt(sb.length()-1) == '.';
    }

    private boolean isArrowOperatorSymbol(StringBuilder sb) {
        if(sb.length() < 2) {
            return false;
        }
        char charLast = sb.charAt(sb.length()-1);
        char charNextToLast = new Character(sb.charAt(sb.length()-2));
        return charNextToLast == '-' && charLast == '>';
    }
}

