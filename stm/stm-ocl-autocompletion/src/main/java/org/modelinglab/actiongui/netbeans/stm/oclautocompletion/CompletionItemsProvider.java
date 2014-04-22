/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.stm.oclautocompletion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.modelinglab.actiongui.netbeans.stm.oclautocompletion.completionitems.STMOCLDotOrArrowOperationCompletionItem;
import org.modelinglab.actiongui.netbeans.stm.oclautocompletion.completionitems.STMOCLEntityCompletionItem;
import org.modelinglab.actiongui.netbeans.stm.oclautocompletion.completionitems.STMOCLEnumLiteralCompletionItem;
import org.modelinglab.actiongui.netbeans.stm.oclautocompletion.completionitems.STMOCLIteratorCompletionItem;
import org.modelinglab.actiongui.netbeans.stm.oclautocompletion.completionitems.STMOCLOtherOperationCompletionItem;
import org.modelinglab.actiongui.netbeans.stm.oclautocompletion.completionitems.STMOCLPrefixOperationCompletionItem;
import org.modelinglab.actiongui.netbeans.stm.oclautocompletion.completionitems.STMOCLPropertyCompletionItem;
import org.modelinglab.actiongui.netbeans.stm.oclautocompletion.completionitems.STMOCLVariableCompletionItem;
import org.modelinglab.actiongui.netbeans.stm.oclautocompletion.exceptions.STMAutocompletionException;
import org.modelinglab.ocl.core.ast.AssociationEnd;
import org.modelinglab.ocl.core.ast.Attribute;
import org.modelinglab.ocl.core.ast.Element;
import org.modelinglab.ocl.core.ast.Operation;
import org.modelinglab.ocl.core.ast.OperationsStore;
import org.modelinglab.ocl.core.ast.StaticEnvironment;
import org.modelinglab.ocl.core.ast.UmlClass;
import org.modelinglab.ocl.core.ast.UmlEnum;
import org.modelinglab.ocl.core.ast.UmlEnumLiteral;
import org.modelinglab.ocl.core.ast.expressions.OclExpression;
import org.modelinglab.ocl.core.ast.expressions.Variable;
import org.modelinglab.ocl.core.ast.types.Classifier;
import org.modelinglab.ocl.core.ast.types.CollectionType;
import org.modelinglab.ocl.core.ast.types.PrimitiveType;
import org.modelinglab.ocl.core.standard.OclStandardIterators;
import org.modelinglab.ocl.parser.OclParser;
import org.netbeans.spi.editor.completion.CompletionItem;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class CompletionItemsProvider {
    
    public static Collection<CompletionItem> buildCompletionItems(OclExpression sourceExpr, String accumulator, OclParser parser, int caretOffset, STMOCLCompletionState state) {
        assert state != null;
        assert (sourceExpr != null && (state == STMOCLCompletionState.ARROW_OPERATOR || state == STMOCLCompletionState.DOT_OPERATOR || state == STMOCLCompletionState.OTHER_OPERATOR )) ||
                (sourceExpr == null && state == STMOCLCompletionState.INIT_EXPR);
        
        Collection<CompletionItem> completionItems = null;           
        switch(state){
            case DOT_OPERATOR:
            case ARROW_OPERATOR: {
                completionItems = buildDotOrArrowOperatorCompletionItems(sourceExpr, accumulator, parser, caretOffset, state);
                break;
            }
            case OTHER_OPERATOR: {
                completionItems = buildOtherOperatorCompletionItems(sourceExpr, accumulator, parser, caretOffset, state);
                break;
            }
            case INIT_EXPR: {
                completionItems = buildInitExprCompletionItems(accumulator, parser, caretOffset, state);
                break;
            }
        }
        assert completionItems != null;
        
        return completionItems;
    }
    
    private static STMOCLIteratorCompletionItem buildOCLIteratorCompletionItem(String nameIterator, CollectionType sourceType, String prefix, int caretOffset, OclParser parser) throws STMAutocompletionException {
        String returnedType = null;
        String bodyType = null;
        switch(nameIterator){
            case "forAll":
            case "exists":
            case "one":{
                bodyType = PrimitiveType.BOOLEAN.toString();
                returnedType = PrimitiveType.BOOLEAN.toString();
                break;
            }
            case "select":
            case "reject":{
                bodyType = PrimitiveType.BOOLEAN.toString();
                returnedType = sourceType.toString();
                break;
            }
            case "isUnique":{
                bodyType = "T";
                returnedType = PrimitiveType.BOOLEAN.toString();
                break;
            }
            case "any":{
                bodyType = PrimitiveType.BOOLEAN.toString();
                returnedType = sourceType.getElementType().toString();
                break;
            }
            case "collect":{
                bodyType = "T(E)";
                returnedType = sourceType.getCollectionKind().toString() + "(E)";
                break;
            }
            case "collectNested":{
                bodyType = "T";
                returnedType = sourceType.getCollectionKind().toString() + "(T)";
                break;
            }
            case "sortedBy":{
                bodyType = "T";
                returnedType = sourceType.toString();
                break;
            }
            default:{
                throw new STMAutocompletionException("The autocompletion feature can not handle the iterator '" + nameIterator +"'");
            }
        }
        // get the next free default variable
        String nameItVar = STMOCLCompletionUtils.getNextFreeVar(parser);
        STMOCLIteratorCompletionItem item = new STMOCLIteratorCompletionItem(nameIterator, returnedType, bodyType, prefix, caretOffset, nameItVar);
        return item;
    }

    private static Collection<CompletionItem> buildDotOrArrowOperatorCompletionItems(OclExpression sourceExpr, String accumulator, OclParser parser, int caretOffset, STMOCLCompletionState state) {
        Collection<CompletionItem> completionItems = new ArrayList<>();

        // 1) get the tye of the source expr
        Classifier typeSourceExpr = sourceExpr.getType();

        // 2) Find:
        //      2.1) If the type is a collection or not. It is needed for properties and iterators.
        //      2.1) If the type is an umlclass or not. It is needed for properties.
        boolean isCollection = false;
        UmlClass umlClass = null;
        if(typeSourceExpr instanceof CollectionType) {
            isCollection = true;
            CollectionType collectionType = (CollectionType) typeSourceExpr;
            Classifier elementType = collectionType.getElementType();
            if(elementType instanceof UmlClass) {                    
                umlClass = (UmlClass) elementType;
            }
        }
        else if(typeSourceExpr instanceof UmlClass){
            umlClass = (UmlClass) typeSourceExpr;                
        }


        // 3) Get all operations that match with the type of the parsed expression, as source of the operation,
        //      except the operations "not" and "-" (negative), because they start with the name of the operation.
        //      Also, a filter with the given accumulator must be applied.
        StaticEnvironment env = parser.getEnv();
        OperationsStore opStore = env.getOpStore();
        Iterator<Operation> operations = opStore.getOperations(typeSourceExpr, null);
        while(operations.hasNext()) {
            Operation op = operations.next();
            if(!STMOCLCompletionUtils.isDotOrArrowOperation(op)) {
                continue;
            }
            String name = op.getName();
            if(!name.startsWith(accumulator)) {
                continue;
            }
            STMOCLDotOrArrowOperationCompletionItem item = new STMOCLDotOrArrowOperationCompletionItem(op, accumulator, caretOffset);
            completionItems.add(item);
        }



        // 4) If dot_operator, if the type of the parsed expression is either an entity or a collection of entities,
        //      then get all features of the referred entity must be provided.
        //      Also, a filter with the given accumulator must be applied.
        if(state == STMOCLCompletionState.DOT_OPERATOR && umlClass != null) {
            Iterator<Attribute> attributes = umlClass.getAllAttributes().iterator();
            while(attributes.hasNext()) {
                Attribute attribute = attributes.next();
                String name = attribute.getName();
                if(!name.startsWith(accumulator)) {
                    continue;
                }
                STMOCLPropertyCompletionItem item = new STMOCLPropertyCompletionItem(attribute, isCollection, accumulator, caretOffset);
                completionItems.add(item);
            }
            Iterator<AssociationEnd> assocEnds = umlClass.getAllAssociationEnds().iterator();
            while(assocEnds.hasNext()) {
                AssociationEnd assocEnd = assocEnds.next();
                String name = assocEnd.getName();
                if(!name.startsWith(accumulator)) {
                    continue;
                }
                STMOCLPropertyCompletionItem item = new STMOCLPropertyCompletionItem(assocEnd, isCollection, accumulator, caretOffset);
                completionItems.add(item);
            }
        }


        // 5) If arrow_operator, if the type of the parsed expression is a collection, then get all iterators that matches with
        //      the type of the parsed expression, as source.
        //      Also, a filter with the given accumulator must be applied.
        if(state == STMOCLCompletionState.ARROW_OPERATOR && isCollection) {
            CollectionType sourceType = (CollectionType) typeSourceExpr;
            Set<String> iteratorNames = OclStandardIterators.getInstance().getIteratorNames(sourceType);
            for (String name : iteratorNames) {
                if(!name.startsWith(accumulator)) {
                    continue;
                }
                STMOCLIteratorCompletionItem item;
                try {
                    item = buildOCLIteratorCompletionItem(name, sourceType, accumulator, caretOffset, parser);
                    completionItems.add(item);
                }
                catch (STMAutocompletionException ex) {
                }
            }
        }
        return completionItems;
    }

    private static Collection<CompletionItem> buildOtherOperatorCompletionItems(OclExpression sourceExpr, String accumulator, OclParser parser, int caretOffset, STMOCLCompletionState state) {
        Collection<CompletionItem> completionItems = new ArrayList<>();
        
        // 1) get the type of the source expr
        Classifier typeSourceExpr = sourceExpr.getType();
        
        // 2) Get all operations that match with the type of the parsed expression, as source of the operation,
        //      excluding the operations of dot and arrow operators.
        //      Also, a filter with the given accumulator must be applied.
        StaticEnvironment env = parser.getEnv();
        OperationsStore opStore = env.getOpStore();
        Iterator<Operation> operations = opStore.getOperations(typeSourceExpr, null);
        while(operations.hasNext()) {
            Operation op = operations.next();
            if(!STMOCLCompletionUtils.isOtherOperation(op)) {
                continue;
            }
            String name = op.getName();
            if(!name.startsWith(accumulator)) {
                continue;
            }
            STMOCLOtherOperationCompletionItem item = new STMOCLOtherOperationCompletionItem(op, accumulator, caretOffset);
            completionItems.add(item);
        }
        
        return completionItems;
    }

    private static Collection<CompletionItem> buildInitExprCompletionItems(String accumulator, OclParser parser, int caretOffset, STMOCLCompletionState state) {
        Collection<CompletionItem> completionItems = new ArrayList<>();
        
        // 1) Add all variables
        StaticEnvironment env = parser.getEnv();
        Set<Element> ownedMembers = env.getOwnedMembers();
        for (Element element : ownedMembers) {
            if(element instanceof Variable) {
                Variable variable = (Variable) element;
                String name = variable.getName();
                if(!name.startsWith(accumulator)) {
                    continue;
                }
                STMOCLVariableCompletionItem item = new STMOCLVariableCompletionItem(variable, accumulator, caretOffset);
                completionItems.add(item);
            }
        }
        
        // 2) Add all operations with prefix operators. Currently they are: negation "not" and negative "-"
        Set<Operation> prefixOperations = STMOCLCompletionUtils.getPrefixOperations(env);
        for (Operation op : prefixOperations) {
            String name = op.getName();
            if(!name.startsWith(accumulator)) {
                continue;
            }
            STMOCLPrefixOperationCompletionItem item = new STMOCLPrefixOperationCompletionItem(op, accumulator, caretOffset);
            completionItems.add(item);
        }
        
        // 3) Add all entities
        Set<UmlClass> entities = STMOCLCompletionUtils.getEntities(env);
        for (UmlClass entity : entities) {
            String name = entity.getName();
            if(!name.startsWith(accumulator)) {
                continue;
            }
            STMOCLEntityCompletionItem item = new STMOCLEntityCompletionItem(entity, accumulator, caretOffset);
            completionItems.add(item);
        }
        
        // 4) Add all enumerations
        Set<UmlEnum> enumerations = STMOCLCompletionUtils.getEnumerations(env);
        for (UmlEnum umlEnum : enumerations) {
            List<UmlEnumLiteral> literals = umlEnum.getLiterals();
            for (UmlEnumLiteral umlEnumLiteral : literals) {
                String name = umlEnumLiteral.toString();
                if(!name.startsWith(accumulator)) {
                    continue;
                }
                STMOCLEnumLiteralCompletionItem item = new STMOCLEnumLiteralCompletionItem(umlEnumLiteral, accumulator, caretOffset);
                completionItems.add(item);
            }
        }
        return completionItems;
    }
}
