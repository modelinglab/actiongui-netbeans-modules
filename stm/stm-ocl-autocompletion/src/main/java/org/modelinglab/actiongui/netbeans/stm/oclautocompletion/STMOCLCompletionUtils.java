/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.stm.oclautocompletion;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.modelinglab.ocl.core.ast.StaticEnvironment;
import org.modelinglab.ocl.parser.OclParser;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class STMOCLCompletionUtils {
    /**
     * Calculates if the position of the cursor is a valid position for making OCL auto-completion.
     * The position will be valid if the cursor is within '[' and ']', since it means an OCL constraint is being defined.
     * @param document
     * @param caretOffset
     * @return true if the position of the cursor is in valid position
     */
    public static boolean isValidPosition(Document document, int caretOffset) throws BadLocationException {
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
    public static String getTextFromCaretToBreakSymbol(Document document, int caretOffset) throws BadLocationException {
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
    
    public static String getTextFromEndToBreakSymbol(StringBuilder sb) throws BadLocationException {
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
    
    public static StringBuilder getTextFromCaretToStartSymbol(Document document, int caretOffset) throws BadLocationException {
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
    
    public static String getOCLExpression(Document document, int caretOffset) throws BadLocationException {
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
    
    
    public static boolean isIdentifierSymbol(StringBuilder sb) {
        assert sb.length() > 0;
        char charLast = sb.charAt(sb.length()-1);
        return Character.isLetterOrDigit(charLast) || charLast == '_';
    }
    
    public static boolean isBreakSymbol(StringBuilder sb) {
        assert sb.length() > 0;
        char charLast = sb.charAt(sb.length()-1);
        return !(Character.isLetterOrDigit(charLast) || charLast == '_' || charLast == '\'');
    }

    public static boolean isDotOperatorSymbol(StringBuilder sb) {
        assert sb.length() > 0;
        
        return sb.charAt(sb.length()-1) == '.';
    }

    public static boolean isArrowOperatorSymbol(StringBuilder sb) {
        if(sb.length() < 2) {
            return false;
        }
        char charLast = sb.charAt(sb.length()-1);
        char charNextToLast = sb.charAt(sb.length()-2);
        return charNextToLast == '-' && charLast == '>';
    }
    
    public static String getNextFreeVar(OclParser parser) {
        int counter = 1;
        StaticEnvironment env = parser.getEnv();
        while(env.lookup("v"+counter) != null) {
            counter++;
        }
        return "v"+counter;
    }
}
