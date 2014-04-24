/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems;

import javax.swing.text.JTextComponent;
import org.modelinglab.ocl.core.ast.Operation;

/**
 * This Completion item handles the OCL operations that are not provided neither for dot operator nor arrow operator,
 * and contains only the source. E.g.: not, -,...
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class OCLPrefixOperationCompletionItem extends OCLOperationCompletionItem{

    public OCLPrefixOperationCompletionItem(Operation op, String prefix, int caretOffset) {
        super(op, prefix, caretOffset);
        assert op.getOwnedParameters().isEmpty();
    }

    @Override
    protected void setCaretOffset(JTextComponent component) {

    }

    @Override
    protected String getTextToInsert() {
        return op.getName();
    }

    @Override
    protected String getLeftText() {
        return op.getName();
    }
    
}
