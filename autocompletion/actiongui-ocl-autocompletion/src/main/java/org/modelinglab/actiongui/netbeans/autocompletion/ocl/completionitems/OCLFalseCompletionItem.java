/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems;

import java.awt.Color;
import javax.swing.text.JTextComponent;
import org.modelinglab.ocl.core.values.BooleanValue;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class OCLFalseCompletionItem extends OCLCompletionItem{
    
    private final BooleanValue trueValue;
    
    public OCLFalseCompletionItem(String prefix, int caretOffset) {
        super(prefix, caretOffset, 0); 
        this.trueValue = BooleanValue.FALSE;
        assert trueValue.toString().startsWith(prefix);
    }

    @Override
    public CharSequence getSortText() {
        return trueValue.toString();
    }

    @Override
    public CharSequence getInsertPrefix() {
        return trueValue.toString();
    }

    @Override
    protected String getTextToInsert() {
        return trueValue.toString();
    }
        
    @Override
    protected String getLeftText() {
        return trueValue.toString();
    }

    @Override
    protected String getRightText() {
        return trueValue.getType().toString();
    }

    @Override
    protected Color getColor() {
        return Color.BLACK;
    }

    @Override
    protected void setCaretOffset(JTextComponent component) {

    }
    
}
