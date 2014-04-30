/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.gtm.oclautocompletion.utils;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class GTMOCLAutocompletionUtils {
    /**
     * Calculates  the position of the cursor for making OCL auto-completion.
     * If the cursor is not between '[' and ']' and is not between '$'.
     * @param document
     * @param caretOffset
     * @return 
     * <p>
     * <b>0</b> if the cursor is not between '[' and ']'.
     * <p>
     * <b>1</b> if the cursor is between '[' and ']', and between '[' and the cursor the number of '$' is odd.
     * <p>
     * <b>2</b> if the cursor is between '[' and ']', and between '[' and the cursor the number of '$' is even.
     */
    public static int getCaretPosition(Document document, int caretOffset) throws BadLocationException {
        // find '['
        String textBefore = document.getText(document.getStartPosition().getOffset(), caretOffset);
        int dollarCount = 0;
        for (int i = textBefore.length()-1; i >= 0; i--) {
            char charAt = textBefore.charAt(i);
            if(charAt == '$') {
                dollarCount++;
            }
            if(charAt == '['){
                break;
            }
            if(charAt == ']') {
                return 0;
            }
        }

        // find ']'
        String textAfter = document.getText(caretOffset, document.getEndPosition().getOffset() - caretOffset);
        for (int i = 0; i < textAfter.length(); i++) {
            char charAt = textAfter.charAt(i);
            if(charAt == ']'){
                break;
            }
            if(charAt == '['){
                return 0;
            }
        }
        
        if((dollarCount % 2) == 1) {
            return 1;
        }
        
        return 2;
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
