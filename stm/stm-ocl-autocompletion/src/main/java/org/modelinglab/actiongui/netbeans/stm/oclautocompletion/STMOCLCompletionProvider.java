/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.stm.oclautocompletion;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
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
import org.modelinglab.ocl.parser.sablecc.lexer.Lexer;
import org.modelinglab.ocl.parser.sablecc.lexer.LexerException;
import org.modelinglab.ocl.parser.sablecc.node.Start;
import org.modelinglab.ocl.parser.sablecc.parser.Parser;
import org.modelinglab.ocl.parser.sablecc.parser.ParserException;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

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
                boolean validPosition;
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
                // 2.1) Get the data model URI
                Project p = TopComponent.getRegistry().getActivated().getLookup().lookup(Project.class);
                if (p == null) {
                    DataObject dob = TopComponent.getRegistry().getActivated().getLookup().lookup(DataObject.class);
                    if (dob != null) {
                        FileObject fo = dob.getPrimaryFile();
                        p = FileOwnerQuery.getOwner(fo);
                    }
                }
                if(p == null) {
                    return;
                }
                FileObject projectDirectory = p.getProjectDirectory();
                if(projectDirectory == null){
                    String errorMessage = "Error getting the current project";
                    STMOCLErrorCompletionItem item = new STMOCLErrorCompletionItem(null, caretOffset, errorMessage);
                    completionResultSet.addItem(item);
                    completionResultSet.finish();
                    return;
                }
                FileObject parent = projectDirectory.getParent();
                if(parent == null) {
                    String errorMessage = "Error getting the parent project of the current project";
                    STMOCLErrorCompletionItem item = new STMOCLErrorCompletionItem(null, caretOffset, errorMessage);
                    completionResultSet.addItem(item);
                    completionResultSet.finish();
                    return;
                }
                FileObject datamodelFO = parent.getFileObject("dtm/target/classes/umlclasses.xml");
                if(datamodelFO == null) {
                    String errorMessage = "Error getting the application data model";
                    STMOCLErrorCompletionItem item = new STMOCLErrorCompletionItem(null, caretOffset, errorMessage);
                    completionResultSet.addItem(item);
                    completionResultSet.finish();
                    return;
                }
                URI datamodelURI = datamodelFO.toURI();
                // 2.2) Get the security model URI
                DataObject dob = TopComponent.getRegistry().getActivated().getLookup().lookup(DataObject.class);
                FileObject fob = dob.getPrimaryFile();
                URI securitymodelURI = fob.toURI();

                // 2.3) Obtain the OCL parser
                OCLParserProvider oclParserProvider = OCLParserProvider.getInstance();
                OclParser parser;
                try {
                    parser = oclParserProvider.getParser(datamodelURI, securitymodelURI, document, caretOffset);
                } 
                catch (STMAutocompletionException ex) {
                    STMOCLErrorCompletionItem item = new STMOCLErrorCompletionItem(null, caretOffset, ex.getMessage());
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
                    STMOCLErrorCompletionItem item = new STMOCLErrorCompletionItem(null, caretOffset, ex.getMessage());
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
        
        // 1) Build the (possibly) accumulator
        while (sb.length() > 0) {
            if(!STMOCLCompletionUtils.isIdentifierSymbol(sb)) {
                break;
            }
            accumulator.append(sb.charAt(sb.length()-1));
            sb.deleteCharAt(sb.length()-1); 
        }
        accumulator = accumulator.reverse();
        
        // 2) Remove the (possibly) whitespaces
        STMOCLCompletionUtils.removeTailWhiteSpaces(sb);
        
        // 3) Find the dot or arrow operator
        if(STMOCLCompletionUtils.isDotOperatorSymbol(sb)) {
            state = STMOCLCompletionState.DOT_OPERATOR;
            sb.deleteCharAt(sb.length()-1);
        }
        if(STMOCLCompletionUtils.isArrowOperatorSymbol(sb)) {
            state = STMOCLCompletionState.ARROW_OPERATOR;
            sb.deleteCharAt(sb.length()-1);
            sb.deleteCharAt(sb.length()-1);
        }
        
        // 4) If operator was not found --> finish
        if(state == null) {
            return null;
        }
        
        // 5) Find the source operator expression
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
        
        // 6) Parse the source operator expression
        OclExpression expBO;
        try {
            expBO = parser.parse(expBeforeOperator.toString());
        }
        // If the expression does not parse, then no items are provided.
        catch (OclParserException | OclLexerException | IOException | OclException ex) {
            return null;
        }
        
        // 7) Build the completion items
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
        STMOCLCompletionUtils.removeTailWhiteSpaces(sb);
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

