/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems;

import java.awt.Color;
import javax.swing.text.JTextComponent;
import org.modelinglab.ocl.core.values.VoidValue;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class OCLNullCompletionItem extends OCLCompletionItem{
    
    private final VoidValue voidValue;
    
    public OCLNullCompletionItem(String prefix, int caretOffset) {
        super(prefix, caretOffset); 
        this.voidValue = VoidValue.instantiate();
        assert voidValue.toString().startsWith(prefix);
    }

    @Override
    public int getSortPriority() {
        return 0;
    }

    @Override
    public CharSequence getSortText() {
        return voidValue.toString();
    }

    @Override
    public CharSequence getInsertPrefix() {
        return voidValue.toString();
    }

    @Override
    protected String getTextToInsert() {
        return voidValue.toString();
    }
        
    @Override
    protected String getLeftText() {
        return voidValue.toString();
    }

    @Override
    protected String getRightText() {
        return voidValue.getType().toString();
    }

    @Override
    protected Color getColor() {
        return Color.BLACK;
    }

    @Override
    protected void setCaretOffset(JTextComponent component) {

    }
    
}
