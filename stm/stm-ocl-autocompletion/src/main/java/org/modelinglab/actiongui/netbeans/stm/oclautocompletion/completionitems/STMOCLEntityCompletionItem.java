/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.stm.oclautocompletion.completionitems;

import java.awt.Color;
import javax.swing.text.JTextComponent;
import org.modelinglab.actiongui.netbeans.stm.oclautocompletion.STMOCLCompletionUtils;
import org.modelinglab.ocl.core.ast.UmlClass;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class STMOCLEntityCompletionItem extends STMOCLCompletionItem{

    private final UmlClass entity;

    public STMOCLEntityCompletionItem(UmlClass entity, String prefix, int caretOffset) {
        super(prefix, caretOffset);
        assert STMOCLCompletionUtils.isEntity(entity);
        assert entity.getName().startsWith(prefix);
        this.entity = entity;
    }        

    @Override
    public int getSortPriority() {
        return 2;
    }

    @Override
    public CharSequence getSortText() {
        return entity.getName();
    }

    @Override
    public CharSequence getInsertPrefix() {
        return entity.getName();
    }
    
    @Override
    protected String getTextToInsert() {
        return entity.getName();
    }
    
    @Override
    protected String getLeftText() {
        return entity.getName();
    }
    
    @Override
    protected String getRightText() {
        return entity.getClassifierType().toString();
    }

    @Override
    protected void setCaretOffset(JTextComponent component) {

    }

    @Override
    protected Color getColor() {
        return Color.RED;
    }
}
