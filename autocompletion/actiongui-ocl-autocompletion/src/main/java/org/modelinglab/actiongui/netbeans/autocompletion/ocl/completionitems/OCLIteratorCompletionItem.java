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
public class OCLIteratorCompletionItem extends OCLCompletionItem{

    private final String nameIterator;
    private final String returnedType;
    private final String bodyType;
    private final String nameItVar;

    public OCLIteratorCompletionItem(String nameIterator, String returnedType, String bodyType, String prefix, int caretOffset, String nameItVar) {
        super(prefix, caretOffset, 2);
        assert nameIterator.startsWith(prefix);
        this.nameIterator = nameIterator;
        this.returnedType = returnedType;
        this.bodyType = bodyType;
        this.nameItVar = nameItVar;
    }        

    @Override
    public CharSequence getSortText() {
        return nameIterator;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return nameIterator;
    }
    
    @Override
    protected String getTextToInsert() {
        String textToInsert = nameIterator + "(" + nameItVar + "|)";
        return textToInsert;
    }
    
    @Override
    protected String getLeftText() {
        String textToShow = nameIterator + "(body:" + bodyType + ")";
        return textToShow;
    }
    
    @Override
    protected String getRightText() {
        return returnedType;
    }

    @Override
    protected void setCaretOffset(JTextComponent component) {
        // set the caret position in the body of the iterator, just after the '|'
        component.setCaretPosition(caretOffset - prefix.length() + nameIterator.length() + nameItVar.length() + 2);
    }

    @Override
    protected Color getColor() {
        return Color.PINK;
    }
}
