/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.stm.oclautocompletion.completionitems;

import java.awt.Color;
import javax.swing.text.JTextComponent;
import org.modelinglab.ocl.core.ast.UmlEnumLiteral;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class STMOCLEnumLiteralCompletionItem extends STMOCLCompletionItem{

    private final UmlEnumLiteral literal;

    public STMOCLEnumLiteralCompletionItem(UmlEnumLiteral literal, String prefix, int caretOffset) {
        super(prefix, caretOffset);
        assert literal.toString().startsWith(prefix);
        this.literal = literal;
    }        

    @Override
    public int getSortPriority() {
        return 3;
    }

    @Override
    public CharSequence getSortText() {
        return literal.toString();
    }

    @Override
    public CharSequence getInsertPrefix() {
        return literal.toString();
    }
    
    @Override
    protected String getTextToInsert() {
        return literal.toString();
    }
    
    @Override
    protected String getLeftText() {
        return literal.toString();
    }
    
    @Override
    protected String getRightText() {
        return literal.getType().toString();
    }

    @Override
    protected void setCaretOffset(JTextComponent component) {

    }

    @Override
    protected Color getColor() {
        Color c = new Color(222, 184, 135);
        return c;
    }
}
