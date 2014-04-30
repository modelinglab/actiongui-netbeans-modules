/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.autocompletion.ocl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems.OCLCompletionItem;
import org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems.OCLDotOrArrowOperationCompletionItem;
import org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems.OCLEntityCompletionItem;
import org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems.OCLEnumLiteralCompletionItem;
import org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems.OCLIfThenElseCompletionItem;
import org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems.OCLIteratorCompletionItem;
import org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems.OCLOtherOperationCompletionItem;
import org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems.OCLPrefixOperationCompletionItem;
import org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems.OCLPropertyCompletionItem;
import org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems.OCLUmlClassCompletionItem;
import org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems.OCLVariableCompletionItem;
import org.modelinglab.actiongui.netbeans.autocompletion.ocl.exceptions.OCLAutocompletionException;
import org.modelinglab.actiongui.netbeans.autocompletion.ocl.utils.OCLAutocompletionUtils;
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
import org.modelinglab.ocl.core.ast.types.ClassifierType;
import org.modelinglab.ocl.core.ast.types.CollectionType;
import org.modelinglab.ocl.core.ast.types.PrimitiveType;
import org.modelinglab.ocl.core.exceptions.OclException;
import org.modelinglab.ocl.core.standard.OclStandardIterators;
import org.modelinglab.ocl.ext.time.UmlWrapperClass;
import org.modelinglab.ocl.parser.OclLexerException;
import org.modelinglab.ocl.parser.OclParser;
import org.modelinglab.ocl.parser.OclParserException;
import org.openide.util.Exceptions;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class OCLCompletionItemsProvider {

    public Collection<OCLCompletionItem> buildOCLCompletionItems(StringBuilder expr, OclParser parser, int caretOffset) {
        Collection<OCLCompletionItem> completionItems;
         
        // 1) Analyze if the completion case is either dot_operator or arrow_operator
        StringBuilder sb = new StringBuilder(expr.toString());
        completionItems = analyzeDotOrArrowOperatorCase(sb, caretOffset, parser);
        if(completionItems != null) {
            return completionItems;
        }
        
        // 2) Analyze if the completion case is either other_operator or init_expr
        sb = new StringBuilder(expr.toString());
        completionItems = analyzeInitExprOrOtherOperatorCase(sb, caretOffset, parser);

        assert completionItems != null;
        
        return completionItems;
    }
    
    /**
     * If the auto-completion case is either dot_operator or arrow_operator, then it returns the available collection
     * of completion items.
     * @param sb
     * @param caretOffset
     * @param parser
     * @return 
     */
    private Collection<OCLCompletionItem> analyzeDotOrArrowOperatorCase(StringBuilder sb, int caretOffset, OclParser parser){
        OCLCompletionState state = null;
        StringBuilder expBeforeOperator = new StringBuilder("");
        StringBuilder accumulator = new StringBuilder("");
        
        // 1) Build the (possibly) accumulator
        while (sb.length() > 0) {
            if(!OCLAutocompletionUtils.isIdentifierSymbol(sb)) {
                break;
            }
            accumulator.append(sb.charAt(sb.length()-1));
            sb.deleteCharAt(sb.length()-1); 
        }
        accumulator = accumulator.reverse();
        
        // 2) Remove the (possibly) whitespaces
        OCLAutocompletionUtils.removeTailWhiteSpaces(sb);
        
        // 3) Find the dot or arrow operator
        if(OCLAutocompletionUtils.isDotOperatorSymbol(sb)) {
            state = OCLCompletionState.DOT_OPERATOR;
            sb.deleteCharAt(sb.length()-1);
        }
        if(OCLAutocompletionUtils.isArrowOperatorSymbol(sb)) {
            state = OCLCompletionState.ARROW_OPERATOR;
            sb.deleteCharAt(sb.length()-1);
            sb.deleteCharAt(sb.length()-1);
        }
        
        // 4) If operator was not found --> finish
        if(state == null) {
            return null;
        }
        
        // 5) Find the source operator expression
        OCLAutocompletionUtils.removeTailWhiteSpaces(sb);
        OCLAutocompletionUtils.removeWhiteSpacesWrappingOperators(sb);
        int numOpenParenthesis = 0;
        int numOpenBrakets = 0;
        while(sb.length() > 0){
            char charAt = sb.charAt(sb.length()-1);
            if(charAt == ')') {
                numOpenParenthesis++;
            }
            else if(charAt == '}'){
                numOpenBrakets++;
            }
            else if(charAt == '(') {
                if(numOpenParenthesis > 0) {
                    numOpenParenthesis--;
                }
                else {
                    break;
                }
            }
            else if(charAt == '{') {
                if(numOpenBrakets > 0) {
                    numOpenBrakets--;
                }
                else {
                    break;
                }
            }
            else if(Character.isWhitespace(charAt) || charAt == ',' || charAt == '|') {
                if(!((numOpenParenthesis + numOpenBrakets) > 0)) {
                    break;
                }
            }
            expBeforeOperator.append(charAt);
            sb = sb.deleteCharAt(sb.length()-1);
        }
        expBeforeOperator = expBeforeOperator.reverse();
        if(expBeforeOperator.length() == 0) {
            return null;
        }
        
        // 6) Parse the source operator expression
        OclExpression expBO;
        try {
            expBO = parser.parse(expBeforeOperator.toString());
        }
        // If the expression does not parse, then no items are provided.
        catch (OclParserException | OclLexerException | IOException | OclException ex) {
            return null;
        }
        
        // 7) Build the completion items
        Collection<OCLCompletionItem> completionItems = buildDotOrArrowOperatorCompletionItems(expBO, accumulator.toString(), parser, caretOffset, state);
        return completionItems;
    }
    
    /**
     * If the auto-completion case is either init_expr or other_operator, then it returns the available collection
     * of completion items.
     * @param sb
     * @param caretOffset
     * @param parser
     * @return 
     */
    private Collection<OCLCompletionItem> analyzeInitExprOrOtherOperatorCase(StringBuilder sb, int caretOffset, OclParser parser) {
        StringBuilder expBeforeOperator = new StringBuilder("");
        StringBuilder accumulator = new StringBuilder("");
        
        // 1) Build the accumulator
        while (sb.length() > 0) {
            if(!OCLAutocompletionUtils.isIdentifierSymbol(sb)) {
                break;
            }
            accumulator.append(sb.charAt(sb.length()-1));
            sb.deleteCharAt(sb.length()-1); 
        }
        accumulator = accumulator.reverse();
        // if there are no more text, we are in init_expr case
        if(sb.length() == 0) {
            Collection<OCLCompletionItem> completionItems = buildInitExprCompletionItems(accumulator.toString(), parser, caretOffset);
            return completionItems;
        }
        
        // 2) Delete all initial whitespaces
        OCLAutocompletionUtils.removeTailWhiteSpaces(sb);
        // if there are no more text, we are in init_expr case
        if(sb.length() == 0) {
            Collection<OCLCompletionItem> completionItems = buildInitExprCompletionItems(accumulator.toString(), parser, caretOffset);
            return completionItems;
        }

        // 3) find the source operator expression
        OCLAutocompletionUtils.removeTailWhiteSpaces(sb);
        OCLAutocompletionUtils.removeWhiteSpacesWrappingOperators(sb);
        int numOpenParenthesis = 0;
        int numOpenBrakets = 0;
        while(sb.length() > 0){
            char charAt = sb.charAt(sb.length()-1);
            if(charAt == ')') {
                numOpenParenthesis++;
            }
            else if(charAt == '}'){
                numOpenBrakets++;
            }
            else if(charAt == '(') {
                if(numOpenParenthesis > 0) {
                    numOpenParenthesis--;
                }
                else {
                    break;
                }
            }
            else if(charAt == '{') {
                if(numOpenBrakets > 0) {
                    numOpenBrakets--;
                }
                else {
                    break;
                }
            }
            else if(Character.isWhitespace(charAt) || charAt == ',' || charAt == '|') {
                if(!((numOpenParenthesis + numOpenBrakets) > 0)) {
                    break;
                }
            }
            expBeforeOperator.append(charAt);
            sb = sb.deleteCharAt(sb.length()-1);
        }
        expBeforeOperator = expBeforeOperator.reverse();
        // if sub expression before caret position is empty, we are in init_expr case 
        if(expBeforeOperator.length() == 0) {
            Collection<OCLCompletionItem> completionItems = buildInitExprCompletionItems(accumulator.toString(), parser, caretOffset);
            return completionItems;
        }
        
        // 3) Parse the source operator expression
        OclExpression expBO;
        try {
            expBO = parser.parse(expBeforeOperator.toString());
        }
        // If the expression does not parse, then we are in init_expr case
        catch (OclParserException | OclLexerException | IOException | OclException ex) {
            Collection<OCLCompletionItem> completionItems = buildInitExprCompletionItems(accumulator.toString(), parser, caretOffset);
            return completionItems;
        }
        
        // in this case, we have a sub expression before caret position, so we are in other operator case
        Collection<OCLCompletionItem> completionItems = buildOtherOperatorCompletionItems(expBO, accumulator.toString(), parser, caretOffset);
        return completionItems;
    }   
    
    private OCLIteratorCompletionItem buildOCLIteratorCompletionItem(String nameIterator, CollectionType sourceType, String prefix, int caretOffset, OclParser parser) throws OCLAutocompletionException {
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
                throw new OCLAutocompletionException("The autocompletion feature can not handle the iterator '" + nameIterator +"'");
            }
        }
        // get the next free default variable
        String nameItVar = OCLAutocompletionUtils.getNextFreeVar(parser);
        OCLIteratorCompletionItem item = new OCLIteratorCompletionItem(nameIterator, returnedType, bodyType, prefix, caretOffset, nameItVar);
        return item;
    }

    private Collection<OCLCompletionItem> buildDotOrArrowOperatorCompletionItems(OclExpression sourceExpr, String accumulator, OclParser parser, int caretOffset, OCLCompletionState state) {
        Collection<OCLCompletionItem> completionItems = new ArrayList<>();

        // 1) get the type of the source expr
        Classifier typeSourceExpr = sourceExpr.getType();

        // 2) Find:
        //      2.1) If the type is a collection or not. It is needed for properties and iterators.
        //      2.2) If the type is a umlclass or not. It is needed for properties.
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
            if(!OCLAutocompletionUtils.isDotOrArrowOperation(op)) {
                continue;
            }
            String name = op.getName();
            if(!name.startsWith(accumulator)) {
                continue;
            }
            OCLDotOrArrowOperationCompletionItem item = new OCLDotOrArrowOperationCompletionItem(op, accumulator, caretOffset);
            completionItems.add(item);
        }



        // 4) If dot_operator, if the type of the parsed expression is either an entity or a collection of entities,
        //      then get all features of the referred entity must be provided.
        //      Also, a filter with the given accumulator must be applied.
        if(state == OCLCompletionState.DOT_OPERATOR && umlClass != null) {
            Iterator<Attribute> attributes = umlClass.getAllAttributes().iterator();
            while(attributes.hasNext()) {
                Attribute attribute = attributes.next();
                String name = attribute.getName();
                if(!name.startsWith(accumulator)) {
                    continue;
                }
                OCLPropertyCompletionItem item = new OCLPropertyCompletionItem(attribute, isCollection, accumulator, caretOffset);
                completionItems.add(item);
            }
            Iterator<AssociationEnd> assocEnds = umlClass.getAllAssociationEnds().iterator();
            while(assocEnds.hasNext()) {
                AssociationEnd assocEnd = assocEnds.next();
                String name = assocEnd.getName();
                if(!name.startsWith(accumulator)) {
                    continue;
                }
                OCLPropertyCompletionItem item = new OCLPropertyCompletionItem(assocEnd, isCollection, accumulator, caretOffset);
                completionItems.add(item);
            }
        }


        // 5) If arrow_operator, if the type of the parsed expression is a collection, then get all iterators that matches with
        //      the type of the parsed expression, as source.
        //      Also, a filter with the given accumulator must be applied.
        if(state == OCLCompletionState.ARROW_OPERATOR && isCollection) {
            CollectionType sourceType = (CollectionType) typeSourceExpr;
            Set<String> iteratorNames = OclStandardIterators.getInstance().getIteratorNames(sourceType);
            for (String name : iteratorNames) {
                if(!name.startsWith(accumulator)) {
                    continue;
                }
                OCLIteratorCompletionItem item;
                try {
                    item = buildOCLIteratorCompletionItem(name, sourceType, accumulator, caretOffset, parser);
                    completionItems.add(item);
                }
                catch (OCLAutocompletionException ex) {
                }
            }
        }
        return completionItems;
    }

    private Collection<OCLCompletionItem> buildOtherOperatorCompletionItems(OclExpression sourceExpr, String accumulator, OclParser parser, int caretOffset) {
        Collection<OCLCompletionItem> completionItems = new ArrayList<>();
        
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
            if(!OCLAutocompletionUtils.isOtherOperation(op)) {
                continue;
            }
            String name = op.getName();
            if(!name.startsWith(accumulator)) {
                continue;
            }
            OCLOtherOperationCompletionItem item = new OCLOtherOperationCompletionItem(op, accumulator, caretOffset);
            completionItems.add(item);
        }
        
        return completionItems;
    }

    private Collection<OCLCompletionItem> buildInitExprCompletionItems(String accumulator, OclParser parser, int caretOffset) {
        Collection<OCLCompletionItem> completionItems = new ArrayList<>();
        StaticEnvironment env = parser.getEnv();
        
        // 1) Add if statement
        if("if".startsWith(accumulator)) {
            OCLIfThenElseCompletionItem item = new OCLIfThenElseCompletionItem(accumulator, caretOffset);
            completionItems.add(item);
        }
        
        // 2) Add all operations with prefix operators. Currently they are: negation "not" and negative "-"
        Set<Operation> prefixOperations = OCLAutocompletionUtils.getPrefixOperations(env);
        for (Operation op : prefixOperations) {
            String name = op.getName();
            if(!name.startsWith(accumulator)) {
                continue;
            }
            OCLPrefixOperationCompletionItem item = new OCLPrefixOperationCompletionItem(op, accumulator, caretOffset);
            completionItems.add(item);
        }
        
        // 3) Add all variables
        Set<Element> ownedMembers = env.getOwnedMembers();
        for (Element element : ownedMembers) {
            if(element instanceof Variable) {
                Variable variable = (Variable) element;
                // if variable contains annotations, it means that the varibale comes from other language different
                // than OCL language --> skip
                if(!variable.getAllAnnotations().isEmpty()) {
                    continue;
                }
                String name = variable.getName();
                if(!name.startsWith(accumulator)) {
                    continue;
                }
                OCLVariableCompletionItem item = new OCLVariableCompletionItem(variable, accumulator, caretOffset);
                completionItems.add(item);
            }
        }
        
        // 4) Add all entities
        Set<UmlClass> entities = OCLAutocompletionUtils.getEntities(env);
        for (UmlClass entity : entities) {
            String name = entity.getName();
            if(!name.startsWith(accumulator)) {
                continue;
            }
            OCLEntityCompletionItem item = new OCLEntityCompletionItem(entity, accumulator, caretOffset);
            completionItems.add(item);
        }
        
        // 5) Add other uml classes that are not entities
        Set<UmlClass> otherUmlClasses = OCLAutocompletionUtils.getOtherUmlClasses(env);
        for (UmlClass umlClass : otherUmlClasses) {
            String name = umlClass.getName();
            if(!name.startsWith(accumulator)) {
                continue;
            }
            OCLUmlClassCompletionItem item = new OCLUmlClassCompletionItem(umlClass, accumulator, caretOffset);
            completionItems.add(item);
        }
        
        // 6) Add all enumerations
        Set<UmlEnum> enumerations = OCLAutocompletionUtils.getEnumerations(env);
        for (UmlEnum umlEnum : enumerations) {
            List<UmlEnumLiteral> literals = umlEnum.getLiterals();
            for (UmlEnumLiteral umlEnumLiteral : literals) {
                String name = umlEnumLiteral.toString();
                if(!name.startsWith(accumulator)) {
                    continue;
                }
                OCLEnumLiteralCompletionItem item = new OCLEnumLiteralCompletionItem(umlEnumLiteral, accumulator, caretOffset);
                completionItems.add(item);
            }
        }
        return completionItems;
    }
}
