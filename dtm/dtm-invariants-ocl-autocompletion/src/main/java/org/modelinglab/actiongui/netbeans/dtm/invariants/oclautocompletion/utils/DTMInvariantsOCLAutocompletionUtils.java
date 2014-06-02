/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.dtm.invariants.oclautocompletion.utils;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class DTMInvariantsOCLAutocompletionUtils {
    private static DTMInvariantsOCLAutocompletionUtils instance;
    
    private DTMInvariantsOCLAutocompletionUtils() {
        
    }
    
    public static DTMInvariantsOCLAutocompletionUtils getInstance() {
        if(instance == null) {
            instance = new DTMInvariantsOCLAutocompletionUtils();
        }
        return instance;
    }
    
    public static boolean isIdentifierSymbol(StringBuilder sb) {
        assert sb.length() > 0;
        char charLast = sb.charAt(sb.length()-1);
        return Character.isLetterOrDigit(charLast) || charLast == '_' || charLast == ':';
    }
    
    
}