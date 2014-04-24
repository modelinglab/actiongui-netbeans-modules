/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.stm.oclautocompletion.utils;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class STMOCLAutocompletionUtils {
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
}
