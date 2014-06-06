/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems;

import javax.swing.text.JTextComponent;
import org.modelinglab.ocl.core.ast.Operation;
import org.modelinglab.ocl.core.ast.Parameter;

/**
 * This Completion item handles the OCL operations that are not provided neither for dot operator nor arrow operator,
 * and contains a source and exactly one parameter. E.g.: and, or, =, implies...
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class OCLOtherOperationCompletionItem extends OCLOperationCompletionItem{
    private final boolean fromXML;
    
    public OCLOtherOperationCompletionItem(Operation op, String prefix, int caretOffset, boolean fromXML) {
        super(op, prefix, caretOffset);
        assert op.getOwnedParameters().size() == 1;
        this.fromXML = fromXML;
    }

    @Override
    protected void setCaretOffset(JTextComponent component) {
        component.setCaretPosition(caretOffset - prefix.length() + getTextToInsert().length());
    }

    @Override
    protected String getTextToInsert() {
        String textToInsert = op.getName() + " ";
        if(fromXML) {
            textToInsert = adaptTextToHtml(textToInsert);
        }
        return textToInsert;
    }

    @Override
    protected String getLeftText() {
        Parameter parameter = op.getOwnedParameters().get(0);
        return op.getName() + " " + parameter.getName() + ":" + parameter.getType().toString();
    }
    
}
