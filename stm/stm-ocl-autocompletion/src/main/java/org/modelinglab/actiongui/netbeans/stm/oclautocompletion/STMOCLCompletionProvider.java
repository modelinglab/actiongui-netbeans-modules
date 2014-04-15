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
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.modelinglab.actiongui.netbeans.stm.oclautocompletion.completionitems.STMOCLErrorCompletionItem;
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
                    validPosition = STMOCLCompletionUtils.isValidPosition(document, caretOffset);
                } 
                catch (BadLocationException ex) {
                    String errorMessage = "OCL auto-completion is disabled: " + ex.getMessage();
                    STMOCLErrorCompletionItem item = new STMOCLErrorCompletionItem(null, caretOffset, errorMessage);
                    completionResultSet.addItem(item);
                    completionResultSet.finish();
                    return;
                }
                
                if (!validPosition) {
                    String errorMessage = "OCL auto-completion is disabled: the caret position must be within square brackets '[' and ']'.";
                    STMOCLErrorCompletionItem item = new STMOCLErrorCompletionItem(null, caretOffset, errorMessage);
                    completionResultSet.addItem(item);
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
                    String errorMessage = "OCL auto-completion is disabled: " + ex.getMessage();
                    STMOCLErrorCompletionItem item = new STMOCLErrorCompletionItem(null, caretOffset, errorMessage);
                    completionResultSet.addItem(item);
                    completionResultSet.finish();
                    return;
                }
                
                // 3) Get the collection of available items for autocompletion
                Collection<CompletionItem> completionItems;
                try {
                    completionItems = buildCompletionItems(document, caretOffset, parser);
                }
                catch (BadLocationException ex) {
                    String errorMessage = "OCL auto-completion is disabled: " + ex.getMessage();
                    STMOCLErrorCompletionItem item = new STMOCLErrorCompletionItem(null, caretOffset, errorMessage);
                    completionResultSet.addItem(item);
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
        StringBuilder sb = STMOCLCompletionUtils.getTextFromCaretToStartSymbol(document, caretOffset);
        
        // 1) find the dot or arrow operator and build the accumulator
        while (sb.length() > 0) {
            if(STMOCLCompletionUtils.isDotOperatorSymbol(sb)) {
                state = STMOCLCompletionState.DOT_OPERATOR;
                sb.deleteCharAt(sb.length()-1);
                break;
            }
            if(STMOCLCompletionUtils.isArrowOperatorSymbol(sb)) {
                state = STMOCLCompletionState.ARROW_OPERATOR;
                sb.deleteCharAt(sb.length()-1);
                sb.deleteCharAt(sb.length()-1);
                break;
            }
            if(!STMOCLCompletionUtils.isIdentifierSymbol(sb)) {
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
        StringBuilder sb = STMOCLCompletionUtils.getTextFromCaretToStartSymbol(document, caretOffset);
        
        // 1) Build the accumulator
        while (sb.length() > 0) {
            if(!STMOCLCompletionUtils.isIdentifierSymbol(sb)) {
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
}

