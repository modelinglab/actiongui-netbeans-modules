/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class GTMIfThenElseStatementCompletionItem extends GTMStatementCompletionItem{

    public static final String NAME_STATEMENT = "if-then-else";
    public static final String TEXT_STATEMENT = "if [condition_expr] {} else {}";
    
    public GTMIfThenElseStatementCompletionItem(String prefix, int caretOffset) {
        super(prefix, caretOffset, NAME_STATEMENT, TEXT_STATEMENT);
    }
    
}
