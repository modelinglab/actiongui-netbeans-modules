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
public class GTMOpenActionCompletionItem extends GTMActionCompletionItem{

    public static final String NAME_ACTION = "open";
    public static final String TEXT_ACTION = "open window_id(var_id_1 : [value_1], var_id_n : [value_n])";
    
    public GTMOpenActionCompletionItem(String prefix, int caretOffset) {
        super(prefix, caretOffset, NAME_ACTION, TEXT_ACTION);
    }
}
