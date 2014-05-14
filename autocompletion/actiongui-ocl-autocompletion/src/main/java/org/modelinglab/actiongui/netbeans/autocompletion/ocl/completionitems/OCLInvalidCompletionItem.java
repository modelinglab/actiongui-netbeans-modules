/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems;

import java.awt.Color;
import javax.swing.text.JTextComponent;
import org.modelinglab.ocl.core.values.InvalidValue;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class OCLInvalidCompletionItem extends OCLCompletionItem{
    
    private final InvalidValue invalidValue;
    
    public OCLInvalidCompletionItem(String prefix, int caretOffset) {
        super(prefix, caretOffset); 
        this.invalidValue = InvalidValue.instantiate();
        assert invalidValue.toString().startsWith(prefix);
    }

    @Override
    public int getSortPriority() {
        return 0;
    }

    @Override
    public CharSequence getSortText() {
        return invalidValue.toString();
    }

    @Override
    public CharSequence getInsertPrefix() {
        return invalidValue.toString();
    }

    @Override
    protected String getTextToInsert() {
        return invalidValue.toString();
    }
        
    @Override
    protected String getLeftText() {
        return invalidValue.toString();
    }

    @Override
    protected String getRightText() {
        return invalidValue.getType().toString();
    }

    @Override
    protected Color getColor() {
        return Color.BLACK;
    }

    @Override
    protected void setCaretOffset(JTextComponent component) {

    }
    
}
