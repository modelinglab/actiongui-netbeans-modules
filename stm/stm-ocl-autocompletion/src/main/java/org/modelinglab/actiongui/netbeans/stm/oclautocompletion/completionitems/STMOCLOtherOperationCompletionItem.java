/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.stm.oclautocompletion.completionitems;

import javax.swing.text.JTextComponent;
import org.modelinglab.ocl.core.ast.Operation;
import org.modelinglab.ocl.core.ast.Parameter;

/**
 * This Completion item handles the OCL operations that are not provided neither for dot operator nor arrow operator,
 * and contains a source and exactly one parameter. E.g.: and, or, =, implies...
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class STMOCLOtherOperationCompletionItem extends STMOCLOperationCompletionItem{

    public STMOCLOtherOperationCompletionItem(Operation op, String prefix, int caretOffset) {
        super(op, prefix, caretOffset);
        assert op.getOwnedParameters().size() == 1;
    }

    @Override
    protected void setCaretOffset(JTextComponent component) {
        component.setCaretPosition(caretOffset - prefix.length() + op.getName().length() + 1);
    }

    @Override
    protected String getTextToInsert() {
        return op.getName() + " ";
    }

    @Override
    protected String getLeftText() {
        Parameter parameter = op.getOwnedParameters().get(0);
        return op.getName() + " " + parameter.getName() + ":" + parameter.getType().toString();
    }
    
}
