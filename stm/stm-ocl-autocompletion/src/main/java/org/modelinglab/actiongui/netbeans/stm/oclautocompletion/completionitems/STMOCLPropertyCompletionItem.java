/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.stm.oclautocompletion.completionitems;

import java.awt.Color;
import javax.swing.text.JTextComponent;
import org.modelinglab.ocl.core.ast.Property;
import org.modelinglab.ocl.core.ast.types.BagType;
import org.modelinglab.ocl.core.ast.types.Classifier;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class STMOCLPropertyCompletionItem extends STMOCLCompletionItem{

    private final Property property;
    private final boolean fromImplicitCollect;

    public STMOCLPropertyCompletionItem(Property property, boolean fromImplicitCollect, String prefix, int caretOffset) {
        super(prefix, caretOffset);
        assert property.getName().startsWith(prefix);
        this.property = property;
        this.fromImplicitCollect = fromImplicitCollect;
    }            

    @Override
    public int getSortPriority() {
        return 0;
    }

    @Override
    public CharSequence getSortText() {
        return property.getName();
    }

    @Override
    public CharSequence getInsertPrefix() {
        return property.getName();
    }
    
    @Override
    protected String getTextToInsert() {
        return property.getName();
    }
    
    @Override
    protected String getLeftText() {
        return property.getName();
    }
    
    @Override
    protected String getRightText() {
        Classifier type = null;
        if(fromImplicitCollect) {
            Classifier referredType = property.getReferredType();
            BagType bagType = new BagType(referredType);
            type = bagType;
        }
        else {
            type = property.getType();
        }
        return type.toString();
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