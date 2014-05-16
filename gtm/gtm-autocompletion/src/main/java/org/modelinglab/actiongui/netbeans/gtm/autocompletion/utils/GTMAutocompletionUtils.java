/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.gtm.autocompletion.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.modelinglab.actiongui.mm.gtm.StandardGtm;
import org.modelinglab.actiongui.mm.gtm.analysis.DepthFirstAdapter;
import org.modelinglab.actiongui.mm.gtm.node.ACatch;
import org.modelinglab.actiongui.mm.gtm.node.AComboBoxWidget;
import org.modelinglab.actiongui.mm.gtm.node.AConditionalAction;
import org.modelinglab.actiongui.mm.gtm.node.AForeachAction;
import org.modelinglab.actiongui.mm.gtm.node.AGtmExp;
import org.modelinglab.actiongui.mm.gtm.node.AOnChangeEvent;
import org.modelinglab.actiongui.mm.gtm.node.AOnClickEvent;
import org.modelinglab.actiongui.mm.gtm.node.AOnCreateEvent;
import org.modelinglab.actiongui.mm.gtm.node.AOnViewEvent;
import org.modelinglab.actiongui.mm.gtm.node.ASimpleWidget;
import org.modelinglab.actiongui.mm.gtm.node.ATableWidget;
import org.modelinglab.actiongui.mm.gtm.node.ATryCatchAction;
import org.modelinglab.actiongui.mm.gtm.node.AVar;
import org.modelinglab.actiongui.mm.gtm.node.AVarGtmExpBase;
import org.modelinglab.actiongui.mm.gtm.node.AWindowWidget;
import org.modelinglab.actiongui.mm.gtm.node.Node;
import org.modelinglab.actiongui.mm.gtm.node.PWidget;
import org.modelinglab.actiongui.mm.gtm.utils.UtilsGtm;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.GTMAutocompletionCase;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.GTMForeachStatementCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.GTMIfThenElseStatementCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.GTMIfThenStatementCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.GTMTryCatchStatementCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.actions.GTMAddPropertyActionCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.actions.GTMBackActionCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.actions.GTMDeleteActionCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.actions.GTMExitActionCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.actions.GTMMethodCallActionCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.actions.GTMNewActionCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.actions.GTMNotificationActionCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.actions.GTMOpenActionCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.actions.GTMReevaluateActionCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.actions.GTMRemovePropertyActionCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.actions.GTMSetActionCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.actions.GTMSetLocateActionCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.actions.GTMSetMethodCallActionCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.actions.GTMUpdateActionCompletionItem;
import org.modelinglab.ocl.core.ast.types.Classifier;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class GTMAutocompletionUtils {
    private static GTMAutocompletionUtils instance;
    
    private GTMAutocompletionUtils() {
        
    }
    
    public static GTMAutocompletionUtils getInstance() {
        if(instance == null) {
            instance = new GTMAutocompletionUtils();
        }
        return instance;
    }
    
    /**
     * Returns the text to parse from the document, adding a character in case the caret is between two dollar symbols
     * (and nothing else), in order to avoid GUIML model parser exception
     * @param document
     * @param caretOffset
     * @return 
     */
    public String textToParse(Document document, int caretOffset) throws BadLocationException {
        String textToParse = new String();
        String textBefore = document.getText(document.getStartPosition().getOffset(), caretOffset);
        boolean foundStartDollar = false;
        if(!textBefore.isEmpty()) {
            if(textBefore.charAt(textBefore.length()-1) == '$') {
                foundStartDollar = true;
            }
        }
        String textAfter = document.getText(caretOffset, document.getEndPosition().getOffset() - caretOffset);
        boolean foundEndDollar = false;
        if(!textAfter.isEmpty()) {
            if(textAfter.charAt(0) == '$') {
              foundEndDollar = true;  
            }
        }
        
        textToParse += textBefore;
        if(foundStartDollar && foundEndDollar) {
            textToParse += "a";
        }
        textToParse += textAfter;
        return textToParse;
    }

    public Collection<CompletionItem> getAllStatements(String prefix, int caretOffset) {
        Collection<CompletionItem> statements = new ArrayList<>();
        // add statements
        if(GTMIfThenStatementCompletionItem.NAME_STATEMENT.startsWith(prefix)) {
            statements.add(new GTMIfThenStatementCompletionItem(prefix, caretOffset));
        }
        if(GTMIfThenElseStatementCompletionItem.NAME_STATEMENT.startsWith(prefix)) {
            statements.add(new GTMIfThenElseStatementCompletionItem(prefix, caretOffset));
        }
        if(GTMForeachStatementCompletionItem.NAME_STATEMENT.startsWith(prefix)) {
            statements.add(new GTMForeachStatementCompletionItem(prefix, caretOffset));
        }
        if(GTMTryCatchStatementCompletionItem.NAME_STATEMENT.startsWith(prefix)) {
            statements.add(new GTMTryCatchStatementCompletionItem(prefix, caretOffset));
        }
        
        // add actions
        if(GTMAddPropertyActionCompletionItem.NAME_ACTION.startsWith(prefix)) {
            statements.add(new GTMAddPropertyActionCompletionItem(prefix, caretOffset));
        }
        if(GTMBackActionCompletionItem.NAME_ACTION.startsWith(prefix)) {
            statements.add(new GTMBackActionCompletionItem(prefix, caretOffset));
        }
        if(GTMDeleteActionCompletionItem.NAME_ACTION.startsWith(prefix)) {
            statements.add(new GTMDeleteActionCompletionItem(prefix, caretOffset));
        }
        if(GTMExitActionCompletionItem.NAME_ACTION.startsWith(prefix)) {
            statements.add(new GTMExitActionCompletionItem(prefix, caretOffset));
        }
        if(GTMMethodCallActionCompletionItem.NAME_ACTION.startsWith(prefix)) {
            statements.add(new GTMMethodCallActionCompletionItem(prefix, caretOffset));
        }
        if(GTMNewActionCompletionItem.NAME_ACTION.startsWith(prefix)) {
            statements.add(new GTMNewActionCompletionItem(prefix, caretOffset));
        }
        if(GTMNotificationActionCompletionItem.NAME_ACTION.startsWith(prefix)) {
            statements.add(new GTMNotificationActionCompletionItem(prefix, caretOffset));
        }
        if(GTMOpenActionCompletionItem.NAME_ACTION.startsWith(prefix)) {
            statements.add(new GTMOpenActionCompletionItem(prefix, caretOffset));
        }
        if(GTMReevaluateActionCompletionItem.NAME_ACTION.startsWith(prefix)) {
            statements.add(new GTMReevaluateActionCompletionItem(prefix, caretOffset));
        }
        if(GTMRemovePropertyActionCompletionItem.NAME_ACTION.startsWith(prefix)) {
            statements.add(new GTMRemovePropertyActionCompletionItem(prefix, caretOffset));
        }
        if(GTMSetActionCompletionItem.NAME_ACTION.startsWith(prefix)) {
            statements.add(new GTMSetActionCompletionItem(prefix, caretOffset));
        }
        if(GTMSetLocateActionCompletionItem.NAME_ACTION.startsWith(prefix)) {
            statements.add(new GTMSetLocateActionCompletionItem(prefix, caretOffset));
        }
        if(GTMSetMethodCallActionCompletionItem.NAME_ACTION.startsWith(prefix)) {
            statements.add(new GTMSetMethodCallActionCompletionItem(prefix, caretOffset));
        }
        if(GTMUpdateActionCompletionItem.NAME_ACTION.startsWith(prefix)) {
            statements.add(new GTMUpdateActionCompletionItem(prefix, caretOffset));
        }
        return statements;
    }
    
    public Collection<GTMAutocompletionCase> getAutocompletionCases(StandardGtm standardGtm, int caretOffset) {
        GtmStateCollectorDFA collectorDFA = new GtmStateCollectorDFA(standardGtm, caretOffset);
        standardGtm.getMergedGtmModel().apply(collectorDFA);
        Set<GTMAutocompletionCase> states = collectorDFA.getAutocompletionStates();
        return states;
    }
    
    public Map<String, Classifier> getVisibleTemporalVariables(Map<Node, Map<String, Classifier>> temporalVariables, StandardGtm standardGtm, int caretOffset) {
        Map<String, Classifier> visibleVars = new HashMap<>();
        // 1) Find the possibly Gtm expression where the caret is located and return the visible temporal variables from that Gtm expression
        GtmExpFinderDFA finder = new GtmExpFinderDFA(standardGtm, caretOffset);
        standardGtm.getMergedGtmModel().apply(finder);
        AGtmExp gtmExp = finder.getGtmExp();
        if(gtmExp != null) {
            visibleVars = temporalVariables.get(gtmExp);
        }
        return visibleVars;
    }

    public String getCurrentWidgetGlobalId(StandardGtm standardGtm, int caretOffset) {
        GtmWidgetGlobalIdFinderDFA finder = new GtmWidgetGlobalIdFinderDFA(standardGtm, caretOffset);
        standardGtm.getMergedGtmModel().apply(finder);
        return finder.getWidgetGlobalId();
    }
    
    private class GtmWidgetGlobalIdFinderDFA extends DepthFirstAdapter {
        private String widgetGlobalId;
        private final StandardGtm standardGtm;
        private final int caretPosition;
        
        public GtmWidgetGlobalIdFinderDFA(StandardGtm standardGtm, int gtmExprPosition) {
            this.standardGtm = standardGtm;
            this.caretPosition = gtmExprPosition;
        }

        @Override
        public void inAComboBoxWidget(AComboBoxWidget node) {
            findWidgetGlobalId(node, node.getLBrace(), node.getRBrace());
        }

        @Override
        public void inATableWidget(ATableWidget node) {
            findWidgetGlobalId(node, node.getLBrace(), node.getRBrace());
        }

        @Override
        public void inAWindowWidget(AWindowWidget node) {
            findWidgetGlobalId(node, node.getLBrace(), node.getRBrace());
        }

        @Override
        public void inASimpleWidget(ASimpleWidget node) {
            findWidgetGlobalId(node, node.getLBrace(), node.getRBrace());
        }
        
        private void findWidgetGlobalId(PWidget widget, Node startNode, Node endNode) {
            int start = standardGtm.getSourceSection(startNode).getEndPosition().getOffset();
            int end = standardGtm.getSourceSection(endNode).getStartPosition().getOffset();
            if(start < caretPosition && end >= caretPosition) {
                widgetGlobalId = UtilsGtm.getGlobalId(widget);
            }
        }
        
        public String getWidgetGlobalId() {
            return widgetGlobalId;
        }
    }
    
    private class GtmExpFinderDFA extends DepthFirstAdapter {
        private AGtmExp gtmExp;
        private final StandardGtm standardGtm;
        private final int caretPosition;
        
        public GtmExpFinderDFA(StandardGtm standardGtm, int gtmExprPosition) {
            this.standardGtm = standardGtm;
            this.caretPosition = gtmExprPosition;
        }

        @Override
        public void inAGtmExp(AGtmExp node) {
            // These cases are necessary to avoid problems with nodes created by the syntactic sugar module
            if(node.getLBracket() == null || node.getRBracket() == null) {
                return;
            }
            if(standardGtm.getSourceSection(node.getLBracket()) == null || standardGtm.getSourceSection(node.getRBracket()) == null) {
                return;
            }
            
            int start = standardGtm.getSourceSection(node.getLBracket()).getEndPosition().getOffset();
            int end = standardGtm.getSourceSection(node.getRBracket()).getStartPosition().getOffset();
            if(start < caretPosition && end >= caretPosition) {
                gtmExp = node;
            }
        }
        
        public AGtmExp getGtmExp() {
            return gtmExp;
        }
    }
    
    private class GtmStateCollectorDFA extends DepthFirstAdapter {
        private final StandardGtm standardGtm;
        private final int caretPosition;
        private boolean insideBlockOfActions;
        private boolean insideGtmExpr;
        private boolean insideVarRefInGtmExpr;
        private boolean insideOnChangeVarRef;

        public GtmStateCollectorDFA(StandardGtm standardGtm, int gtmExprPosition) {
            this.standardGtm = standardGtm;
            this.caretPosition = gtmExprPosition;
            this.insideBlockOfActions = false;
            this.insideGtmExpr = false;
            this.insideVarRefInGtmExpr = false;
            this.insideOnChangeVarRef = false;
        }

        public Set<GTMAutocompletionCase> getAutocompletionStates() {
            Set<GTMAutocompletionCase> states = new HashSet<>();
            if(insideVarRefInGtmExpr || insideOnChangeVarRef || (insideBlockOfActions && !insideGtmExpr)) {
                states.add(GTMAutocompletionCase.VARIABLES);
            }
            if(insideBlockOfActions && !insideGtmExpr) {
                states.add(GTMAutocompletionCase.ACTIONS);
            }
            return states;
        }

        @Override
        public void inAOnChangeEvent(AOnChangeEvent node) {
            setInsideOnChangeVarRef(node.getLParen(), node.getRParen());
            setInsideBlockOfActions(node.getLBrace(), node.getRBrace());
        }

        @Override
        public void inAOnViewEvent(AOnViewEvent node) {
            setInsideBlockOfActions(node.getLBrace(), node.getRBrace());
        }

        @Override
        public void inAOnClickEvent(AOnClickEvent node) {
            setInsideBlockOfActions(node.getLBrace(), node.getRBrace());
        }

        @Override
        public void inAOnCreateEvent(AOnCreateEvent node) {
            setInsideBlockOfActions(node.getLBrace(), node.getRBrace());
        }

        @Override
        public void inAVar(AVar node) {
            if(node.getLBrace() == null || node.getRBrace() == null) {
                return;
            }
            setInsideBlockOfActions(node.getLBrace(), node.getRBrace());
        }

        @Override
        public void inACatch(ACatch node) {
            setInsideBlockOfActions(node.getLBrace(), node.getRBrace());
        }

        @Override
        public void inATryCatchAction(ATryCatchAction node) {
            setInsideBlockOfActions(node.getLBrace(), node.getRBrace());
        }

        @Override
        public void inAForeachAction(AForeachAction node) {
            setInsideBlockOfActions(node.getLBrace(), node.getRBrace());
        }

        @Override
        public void inAConditionalAction(AConditionalAction node) {
            setInsideBlockOfActions(node.getLb1(), node.getRb1());
            if(node.getLb2() == null || node.getRb2() == null) {
                return;
            }
            setInsideBlockOfActions(node.getLb2(), node.getRb2());
        }

        
        
        @Override
        public void inAGtmExp(AGtmExp node) {
            setInsideGtmExpr(node.getLBracket(), node.getRBracket());
        }

        @Override
        public void inAVarGtmExpBase(AVarGtmExpBase node) {
            setInsideVarRefInGtmExpr(node.getDollar1(), node.getDollar2());
        }

        private void setInsideBlockOfActions(Node startNode, Node endNode) {
            if(!insideBlockOfActions) {
                insideBlockOfActions = checkInside(startNode, endNode);
            }
        }

        private void setInsideGtmExpr(Node startNode, Node endNode) {
            if(!insideGtmExpr) {
                insideGtmExpr = checkInside(startNode, endNode);
            }
        }

        private void setInsideVarRefInGtmExpr(Node startNode, Node endNode) {
            if(!insideVarRefInGtmExpr) {
                insideVarRefInGtmExpr = checkInside(startNode, endNode);
            }
        }

        private void setInsideOnChangeVarRef(Node startNode, Node endNode) {
            if(!insideOnChangeVarRef) {
                insideOnChangeVarRef = checkInside(startNode, endNode);
            }
        }

        private boolean checkInside(Node startNode, Node endNode) {
            // These cases are necessary to avoid problems with nodes created by the syntactic sugar module
            if(startNode == null || endNode == null) {
                return false;
            }
            if(standardGtm.getSourceSection(startNode) == null || standardGtm.getSourceSection(endNode) == null) {
                return false;
            }
            
            int start = standardGtm.getSourceSection(startNode).getEndPosition().getOffset();
            int end = standardGtm.getSourceSection(endNode).getStartPosition().getOffset();
            if(start < caretPosition && end >= caretPosition) {
                return true;
            } 
            return false;
        }
    }
    
    
}