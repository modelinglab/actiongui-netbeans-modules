/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.variables;

import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.variables.GTMVariableCompletionItem;
import java.awt.Color;
import javax.swing.text.JTextComponent;
import org.modelinglab.ocl.core.ast.types.Classifier;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class GTMTemporalVariableCompletionItem extends GTMVariableCompletionItem{
    private final String idVar;
    private final Classifier typeVar;
    
    
    public GTMTemporalVariableCompletionItem(String prefix, int caretOffset, String idVar, Classifier typeVar) {
        super(prefix, caretOffset);
        assert idVar.startsWith(prefix);
        this.idVar = idVar;
        this.typeVar = typeVar;
    }

    @Override
    public int getSortPriority() {
        return 3;
    }

    @Override
    public CharSequence getSortText() {
        return idVar;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return idVar;
    }

    @Override
    protected String getTextToInsert() {
        return idVar;
    }

    @Override
    protected String getLeftText() {
        return  idVar;
    }

    @Override
    protected String getRightText() {
        return typeVar.toString();
    }

    @Override
    protected void setCaretOffset(JTextComponent component) {

    }

    @Override
    protected Color getColor() {
        return Color.GREEN;
    }
    
}
