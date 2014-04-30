/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems;

import java.awt.Color;
import org.modelinglab.actiongui.netbeans.autocompletion.ocl.utils.OCLAutocompletionUtils;
import org.modelinglab.ocl.core.ast.UmlClass;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class OCLEntityCompletionItem extends OCLUmlClassCompletionItem{

    public OCLEntityCompletionItem(UmlClass entity, String prefix, int caretOffset) {
        super(entity, prefix, caretOffset);
        assert OCLAutocompletionUtils.isEntity(entity);
    }        

    @Override
    public int getSortPriority() {
        return 3;
    }

    @Override
    protected Color getColor() {
        return Color.RED;
    }
}
