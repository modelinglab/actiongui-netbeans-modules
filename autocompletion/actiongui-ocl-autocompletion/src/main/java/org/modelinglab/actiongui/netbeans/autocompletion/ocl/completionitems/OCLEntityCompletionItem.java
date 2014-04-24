/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems;

import java.awt.Color;
import javax.swing.text.JTextComponent;
import org.modelinglab.actiongui.netbeans.autocompletion.ocl.utils.OCLAutocompletionUtils;
import org.modelinglab.ocl.core.ast.UmlClass;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class OCLEntityCompletionItem extends OCLCompletionItem{

    private final UmlClass entity;

    public OCLEntityCompletionItem(UmlClass entity, String prefix, int caretOffset) {
        super(prefix, caretOffset);
        assert OCLAutocompletionUtils.isEntity(entity);
        assert entity.getName().startsWith(prefix);
        this.entity = entity;
    }        

    @Override
    public int getSortPriority() {
        return 3;
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
