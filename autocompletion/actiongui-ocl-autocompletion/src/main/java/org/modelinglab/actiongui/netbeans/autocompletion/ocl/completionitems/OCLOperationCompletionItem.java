/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems;

import java.awt.Color;
import javax.swing.text.JTextComponent;
import org.modelinglab.ocl.core.ast.Operation;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public abstract class OCLOperationCompletionItem extends OCLCompletionItem{

    protected Operation op;

    protected OCLOperationCompletionItem(Operation op, String prefix, int caretOffset) {
        super(prefix, caretOffset, 1);
        assert op.getName().startsWith(prefix);
        this.op = op;
    }        

    @Override
    public CharSequence getSortText() {
        return op.getName();
    }

    @Override
    public CharSequence getInsertPrefix() {
        return op.getName();
    }

    @Override
    protected Color getColor() {
        return Color.BLUE;
    }

    @Override
    protected abstract void setCaretOffset(JTextComponent component);
    
    @Override
    protected abstract String getTextToInsert();
    
    @Override
    protected abstract String getLeftText();
    
    @Override
    protected String getRightText() {
        return op.getType().toString();
    }
}
