/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.actions;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class GTMSetMethodCallActionCompletionItem extends GTMActionCompletionItem{

    public static final String NAME_ACTION = "set-method-call";
    public static final String TEXT_ACTION = "variable := method([arg_1],[arg_n])";
    
    public GTMSetMethodCallActionCompletionItem(String prefix, int caretOffset) {
        super(prefix, caretOffset, NAME_ACTION, TEXT_ACTION);
    }
}
