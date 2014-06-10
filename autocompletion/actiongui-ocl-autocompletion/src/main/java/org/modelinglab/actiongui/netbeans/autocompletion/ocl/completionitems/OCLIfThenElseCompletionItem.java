/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems;

import java.awt.Color;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class OCLIfThenElseCompletionItem extends OCLCompletionItem{
    
    private final String leftText;
    private final String rightText;
    private final String textToInsert;
    
    public OCLIfThenElseCompletionItem(String prefix, int caretOffset) {
        super(prefix, caretOffset, 0);
        assert "if".startsWith(prefix);
        this.leftText = "if condition_expr:Boolean then then_expr:T else else_expr:T endif        ";
        this.rightText = "T";
        this.textToInsert = "if condition_expr then then_expr else else_expr endif";
    }

    @Override
    public CharSequence getSortText() {
        return leftText;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return textToInsert;
    }

    @Override
    protected String getTextToInsert() {
        return textToInsert;
    }
        
    @Override
    protected String getLeftText() {
        return leftText;
    }

    @Override
    protected String getRightText() {
        return rightText;
    }

    @Override
    protected void setCaretOffset(JTextComponent component) {
        component.setCaretPosition(caretOffset - prefix.length() + 3);
    }

    @Override
    protected Color getColor() {
        return Color.BLACK;
    }
    
}
