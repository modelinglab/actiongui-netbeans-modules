/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.stm.oclautocompletion.completionitems;

import java.awt.Color;
import javax.swing.text.JTextComponent;
import org.modelinglab.ocl.core.ast.expressions.Variable;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class STMOCLVariableCompletionItem extends STMOCLCompletionItem{

    private final Variable variable;

    public STMOCLVariableCompletionItem(Variable variable, String prefix, int caretOffset) {
        super(prefix, caretOffset);
        assert variable.getName().startsWith(prefix);
        this.variable = variable;
    }            

    @Override
    public int getSortPriority() {
        return 0;
    }

    @Override
    public CharSequence getSortText() {
        return variable.getName();
    }

    @Override
    public CharSequence getInsertPrefix() {
        return variable.getName();
    }
    
    @Override
    protected String getTextToInsert() {
        return variable.getName();
    }
    
    @Override
    protected String getLeftText() {
        return variable.getName();
    }
    
    @Override
    protected String getRightText() {
        return variable.getType().toString();
    }

    @Override
    protected void setCaretOffset(JTextComponent component) {

    }

    @Override
    protected Color getColor() {
        Color c = new Color(0, 204, 0);
        return c;
    }
}
