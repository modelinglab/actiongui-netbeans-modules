/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.swing.text.JTextComponent;
import org.modelinglab.ocl.core.ast.types.Classifier;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class GTMWidgetVariableCompletionItem extends GTMCompletionItem{
    private final String variableGlobalId;
    private final String currentWidgetGlobalId;
    private final Classifier typeVariable;
    private final Set<String> allVariableGlobalIds;
    
    public GTMWidgetVariableCompletionItem(String prefix, int caretOffset, String variableGlobalId, String currentWidgetGlobalId, Classifier typeVariable, Set<String> allVariableGlobalIds) {
        super(prefix, caretOffset);
        assert variableGlobalId.contains(prefix);
        this.variableGlobalId = variableGlobalId;
        this.currentWidgetGlobalId = currentWidgetGlobalId;
        this.typeVariable = typeVariable;
        this.allVariableGlobalIds = allVariableGlobalIds;
    }

    @Override
    public int getSortPriority() {
        return 2;
    }

    @Override
    public CharSequence getSortText() {
        return variableGlobalId;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return variableGlobalId;
    }

    @Override
    protected String getTextToInsert() {
        // 1) If the variable belongs to the current widget --> write only the localId of the variable
        String widgetGlobalId = variableGlobalId.substring(0, variableGlobalId.lastIndexOf('.'));
        if(widgetGlobalId.equals(currentWidgetGlobalId)) {
            return variableGlobalId.substring(variableGlobalId.lastIndexOf('.') + 1, variableGlobalId.length());
        }
        // 2) If the variable belongs to a different widget --> write the minimum text to unambiguosly express the variable
        String[] split = variableGlobalId.split("\\.");
        List<String> ids = Arrays.asList(split);
        String path = "";
        boolean found = false;
        
        for(int i = ids.size()-1; i >= 0; i--) {
            if(i == 0) {
                path = ids.get(i) + path;
            }
            else {
                path = '.' + ids.get(i) + path;
            }
            int numOfMatches = 0;
            for (String globalId : allVariableGlobalIds) {
                if(globalId.endsWith(path)) {
                    numOfMatches++;
                }
            }
            if(numOfMatches == 1) {
                found = true;
                break;
            }
        }
        if(path.startsWith(".")){
            path = path.substring(1);
        }
        assert found;
        return path;
    }

    @Override
    protected String getLeftText() {
        return variableGlobalId;
    }

    @Override
    protected String getRightText() {
        return typeVariable.toString();
    }

    @Override
    protected void setCaretOffset(JTextComponent component) {
        component.setCaretPosition(caretOffset - prefix.length() + getTextToInsert().length() + 1);
    }

    @Override
    protected Color getColor() {
        return Color.BLUE;
    }
    
}
