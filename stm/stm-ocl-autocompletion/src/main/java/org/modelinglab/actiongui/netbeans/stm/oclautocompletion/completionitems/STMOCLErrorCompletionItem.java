/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.stm.oclautocompletion.completionitems;

import java.awt.Color;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class STMOCLErrorCompletionItem extends STMOCLCompletionItem{

    private final String errorMessage;
    
    public STMOCLErrorCompletionItem(String prefix, int caretOffset, String errorMessage) {
        super(prefix, caretOffset);
        this.errorMessage = errorMessage;
    }

    @Override
    public void defaultAction(JTextComponent component) {
        return;
    }

    
    
    @Override
    public int getSortPriority() {
        return 0;
    }

    @Override
    public CharSequence getSortText() {
        return errorMessage;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return errorMessage;
    }

    @Override
    protected String getTextToInsert() {
        return "";
    }

    @Override
    protected String getLeftText() {
        return errorMessage;
    }

    @Override
    protected String getRightText() {
        return "";
    }

    @Override
    protected void setCaretOffset(JTextComponent component) {
    }

    @Override
    protected Color getColor() {
        return Color.WHITE;
    }
}
