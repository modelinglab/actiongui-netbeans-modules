/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems;

import java.awt.Color;
import javax.swing.text.JTextComponent;
import org.modelinglab.ocl.core.ast.UmlClass;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class OCLUmlClassCompletionItem extends OCLCompletionItem{

    private final UmlClass ucmlClass;

    public OCLUmlClassCompletionItem(UmlClass umlClass, String prefix, int caretOffset) {
        super(prefix, caretOffset, 4);  
        assert umlClass.getName().startsWith(prefix);
        this.ucmlClass = umlClass;
    }        

    @Override
    public CharSequence getSortText() {
        return ucmlClass.getName();
    }

    @Override
    public CharSequence getInsertPrefix() {
        return ucmlClass.getName();
    }
    
    @Override
    protected String getTextToInsert() {
        return ucmlClass.getName();
    }
    
    @Override
    protected String getLeftText() {
        return ucmlClass.getName();
    }
    
    @Override
    protected String getRightText() {
        return ucmlClass.getClassifierType().toString();
    }

    @Override
    protected void setCaretOffset(JTextComponent component) {

    }

    @Override
    protected Color getColor() {
        Color c = new Color(165, 42, 42);
        return c;
    }
}
