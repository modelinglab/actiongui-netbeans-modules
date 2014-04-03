/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.stm.oclautocompletion.completionitems;

import java.util.List;
import javax.swing.text.JTextComponent;
import org.modelinglab.ocl.core.ast.Operation;
import org.modelinglab.ocl.core.ast.Parameter;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class STMOCLDotOrArrowOperationCompletionItem extends STMOCLOperationCompletionItem{

    public STMOCLDotOrArrowOperationCompletionItem(Operation op, String prefix, int caretOffset) {
        super(op, prefix, caretOffset);
    }
        
    @Override
    protected String getTextToInsert() {
        StringBuilder textToInsert = new StringBuilder(op.getName() + "(");
        List<Parameter> ownedParameters = op.getOwnedParameters();
        for (Parameter parameter : ownedParameters) {
            String name = parameter.getName();
            textToInsert.append(name).append(",");
        }
        if(!ownedParameters.isEmpty()) {
            textToInsert.deleteCharAt(textToInsert.length()-1);
        }
        textToInsert.append(')');
        return textToInsert.toString();
    }

    @Override
    protected String getLeftText() {
        StringBuilder leftText = new StringBuilder(op.getName() + "(");
        List<Parameter> ownedParameters = op.getOwnedParameters();
        for (Parameter parameter : ownedParameters) {
            String name = parameter.getName();
            String type = parameter.getType().toString();
            leftText.append(name).append(":").append(type).append(",");
        }
        if(!ownedParameters.isEmpty()) {
            leftText.deleteCharAt(leftText.length()-1);
        }
        leftText.append(")");
        return leftText.toString();
    }

    @Override
    protected void setCaretOffset(JTextComponent component) {
        if(!op.getOwnedParameters().isEmpty()) {
            component.setCaretPosition(caretOffset - prefix.length() + op.getName().length() + 1);
        }
    }
}
