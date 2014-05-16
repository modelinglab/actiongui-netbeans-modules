/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.actions;

import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.GTMStatementCompletionItem;
import java.awt.Color;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public abstract class GTMActionCompletionItem extends GTMStatementCompletionItem{

    protected GTMActionCompletionItem(String prefix, int caretOffset, String nameAction, String textAction) {
        super(prefix, caretOffset, nameAction, textAction);
    }

    @Override
    protected Color getColor() {
        return Color.BLACK;
    }

    @Override
    public int getSortPriority() {
        return super.getSortPriority() + 1;
    }
}
