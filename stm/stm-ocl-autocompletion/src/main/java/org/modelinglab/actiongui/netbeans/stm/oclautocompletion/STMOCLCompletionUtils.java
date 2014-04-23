/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.stm.oclautocompletion;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.modelinglab.ocl.core.ast.Operation;
import org.modelinglab.ocl.core.ast.OperationsStore;
import org.modelinglab.ocl.core.ast.StaticEnvironment;
import org.modelinglab.ocl.core.ast.UmlClass;
import org.modelinglab.ocl.core.ast.UmlEnum;
import org.modelinglab.ocl.core.ast.annotations.EntityAnnotation;
import org.modelinglab.ocl.core.ast.types.PrimitiveType;
import org.modelinglab.ocl.core.standard.operations.bag.IsEqual;
import org.modelinglab.ocl.core.standard.operations.bool.And;
import org.modelinglab.ocl.core.standard.operations.bool.Implies;
import org.modelinglab.ocl.core.standard.operations.bool.Not;
import org.modelinglab.ocl.core.standard.operations.bool.Or;
import org.modelinglab.ocl.core.standard.operations.bool.Xor;
import org.modelinglab.ocl.core.standard.operations.collection.IsDifferent;
import org.modelinglab.ocl.core.standard.operations.integer.Addition;
import org.modelinglab.ocl.core.standard.operations.integer.Division;
import org.modelinglab.ocl.core.standard.operations.integer.Multiplication;
import org.modelinglab.ocl.core.standard.operations.integer.Negative;
import org.modelinglab.ocl.core.standard.operations.integer.Substraction;
import org.modelinglab.ocl.core.standard.operations.real.Greater;
import org.modelinglab.ocl.core.standard.operations.real.GreaterOrEqual;
import org.modelinglab.ocl.core.standard.operations.real.Less;
import org.modelinglab.ocl.core.standard.operations.real.LessOrEqual;
import org.modelinglab.ocl.parser.OclParser;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class STMOCLCompletionUtils {
    /**
     * Calculates if the position of the cursor is a valid position for making OCL auto-completion.
     * The position will be valid if the cursor is within '[' and ']', since it means an OCL constraint is being defined.
     * @param document
     * @param caretOffset
     * @return true if the position of the cursor is in valid position
     */
    public static boolean isValidPosition(Document document, int caretOffset) throws BadLocationException {
        // find '['
        String textBefore = document.getText(document.getStartPosition().getOffset(), caretOffset);
        boolean leftSquareBracketFound = false;
        for (int i = textBefore.length()-1; i >= 0; i--) {
            char charAt = textBefore.charAt(i);
            if(charAt == '['){
                leftSquareBracketFound = true;
                break;
            }
            else if(charAt == ']') {
                break;
            }
        }
        if(!leftSquareBracketFound) {
            return false;
        }
        
        // find ']'
        String textAfter = document.getText(caretOffset, document.getEndPosition().getOffset() - caretOffset);
        boolean rightSquareBracketFound = false;
        for (int i = 0; i < textAfter.length(); i++) {
            char charAt = textAfter.charAt(i);
            if(charAt == ']'){
                rightSquareBracketFound = true;
                break;
            }
            else if(charAt == '['){
                break;
            }
        }
        return rightSquareBracketFound;
    }
    
    /**
     * 
     * @param document
     * @param caretOffset
     * @return The sub-expression before the position of the cursor, until a delimiter character is found
     * @throws BadLocationException 
     */
    public static String getTextFromCaretToBreakSymbol(Document document, int caretOffset) throws BadLocationException {
        StringBuilder subExpression = new StringBuilder("");
        String text = document.getText(document.getStartPosition().getOffset(), caretOffset);
        assert text.contains("[");
        StringBuilder textBefore = new StringBuilder(text);
        while (textBefore.length() > 0) {
            if(isDotOperatorSymbol(textBefore)) {
                subExpression.append(textBefore.charAt(textBefore.length()-1));
                textBefore.deleteCharAt(textBefore.length()-1);
                continue;
            }
            if(isArrowOperatorSymbol(textBefore)) {
                subExpression.append(textBefore.charAt(textBefore.length()-1));
                textBefore.deleteCharAt(textBefore.length()-1);
                subExpression.append(textBefore.charAt(textBefore.length()-1));
                textBefore.deleteCharAt(textBefore.length()-1);
                continue;
            }
            if(isBreakSymbol(textBefore)) {
                break;
            } 
            subExpression.append(textBefore.charAt(textBefore.length()-1));
            textBefore.deleteCharAt(textBefore.length()-1);
        }
        subExpression = subExpression.reverse();        
        return subExpression.toString();
    }
    
    public static String getTextFromEndToBreakSymbol(StringBuilder sb) throws BadLocationException {
        StringBuilder subExpression = new StringBuilder("");
        while (sb.length() > 0) {
            if(isDotOperatorSymbol(sb)) {
                subExpression.append(sb.charAt(sb.length()-1));
                sb.deleteCharAt(sb.length()-1);
                continue;
            }
            if(isArrowOperatorSymbol(sb)) {
                subExpression.append(sb.charAt(sb.length()-1));
                sb.deleteCharAt(sb.length()-1);
                subExpression.append(sb.charAt(sb.length()-1));
                sb.deleteCharAt(sb.length()-1);
                continue;
            }
            if(isBreakSymbol(sb)) {
                break;
            } 
            subExpression.append(sb.charAt(sb.length()-1));
            sb.deleteCharAt(sb.length()-1);
        }
        subExpression = subExpression.reverse();        
        return subExpression.toString();
    }
    
    public static StringBuilder getTextFromCaretToStartSymbol(Document document, int caretOffset) throws BadLocationException {
        StringBuilder sb = new StringBuilder("");
        // find '['
        String textBefore = document.getText(document.getStartPosition().getOffset(), caretOffset);
        assert textBefore.contains("[");
        for (int i = textBefore.length()-1; i >= 0; i--) {
            char charAt = textBefore.charAt(i);
            if(charAt == '['){
                break;
            }
            sb.append(charAt);
        }
        sb = sb.reverse();
        return sb;
    }
    
    public static String getOCLExpression(Document document, int caretOffset) throws BadLocationException {
        StringBuilder sb = new StringBuilder("");
        // find '['
        String textBefore = document.getText(document.getStartPosition().getOffset(), caretOffset);
        for (int i = textBefore.length()-1; i >= 0; i--) {
            char charAt = textBefore.charAt(i);
            if(charAt == '['){
                break;
            }
            sb.append(charAt);
        }
        sb = sb.reverse();
        
        // find ']'
        String textAfter = document.getText(caretOffset, document.getEndPosition().getOffset() - caretOffset);
        for (int i = 0; i < textAfter.length(); i++) {
            char charAt = textAfter.charAt(i);
            if(charAt == ']'){
                break;
            }
            sb.append(charAt);
        }   
        return sb.toString();
    }
    
    
    public static boolean isIdentifierSymbol(StringBuilder sb) {
        assert sb.length() > 0;
        char charLast = sb.charAt(sb.length()-1);
        return Character.isLetterOrDigit(charLast) || charLast == '_' || charLast == ':';
    }
    
    public static boolean isBreakSymbol(StringBuilder sb) {
        assert sb.length() > 0;
        char charLast = sb.charAt(sb.length()-1);
        return !(Character.isLetterOrDigit(charLast) || charLast == '_' || charLast == '\'' || charLast == ':');
    }

    public static boolean isDotOperatorSymbol(StringBuilder sb) {
        if(sb == null) {
            return false;
        }
        if(sb.length() == 0) {
            return false;
        }
        return sb.charAt(sb.length()-1) == '.';
    }

    public static boolean isArrowOperatorSymbol(StringBuilder sb) {
        if(sb == null) {
            return false;
        }
        if(sb.length() < 2) {
            return false;
        }
        char charLast = sb.charAt(sb.length()-1);
        char charNextToLast = sb.charAt(sb.length()-2);
        return charNextToLast == '-' && charLast == '>';
    }
    
    public static String getNextFreeVar(OclParser parser) {
        int counter = 1;
        StaticEnvironment env = parser.getEnv();
        while(env.lookup("v"+counter) != null) {
            counter++;
        }
        return "v"+counter;
    }
    
    public static boolean isDotOrArrowOperation(Operation op) {
        
        // bag operations
        if(op instanceof IsEqual) {
            return false;
        }
        
        // boolean operations
        if(op instanceof And || op instanceof Implies || op instanceof Not || op instanceof Or || op instanceof Xor){
            return false;
        }
        
        // collection operations
        if(op instanceof IsDifferent || 
                op instanceof org.modelinglab.ocl.core.standard.operations.collection.IsEqual){
            return false;
        }
        
        // integer operations
        if(op instanceof Addition || op instanceof Division || op instanceof Multiplication
                || op instanceof Negative || op instanceof Substraction){
            return false;
        }
        
        // oclany operations
        if(op instanceof org.modelinglab.ocl.core.standard.operations.oclAny.IsEqual ||
                op instanceof org.modelinglab.ocl.core.standard.operations.oclAny.IsDifferent) {
            return false;
        }
        
        //oclvoid operations
        if(op instanceof org.modelinglab.ocl.core.standard.operations.oclVoid.IsEqual) {
            return false;
        }
        
        // real operations
        if(op instanceof org.modelinglab.ocl.core.standard.operations.real.Addition ||
                op instanceof org.modelinglab.ocl.core.standard.operations.real.Division ||
                op instanceof Greater || op instanceof GreaterOrEqual || op instanceof Less ||
                op instanceof LessOrEqual ||
                op instanceof org.modelinglab.ocl.core.standard.operations.real.Multiplication ||
                op instanceof org.modelinglab.ocl.core.standard.operations.real.Negative ||
                op instanceof org.modelinglab.ocl.core.standard.operations.real.Substraction) {
            return false;
        }
        
        // sequence operations
        if(op instanceof org.modelinglab.ocl.core.standard.operations.sequence.IsEqual) {
            return false;
        }
        
        // set operations
        if(op instanceof org.modelinglab.ocl.core.standard.operations.set.IsEqual ||
                op instanceof org.modelinglab.ocl.core.standard.operations.set.Substraction){
            return false;
        }
        
        // string operations
        if(op instanceof org.modelinglab.ocl.core.standard.operations.string.Addition ||
                op instanceof org.modelinglab.ocl.core.standard.operations.string.Greater ||
                op instanceof org.modelinglab.ocl.core.standard.operations.string.GreaterOrEqual ||
                op instanceof org.modelinglab.ocl.core.standard.operations.string.Less ||
                op instanceof org.modelinglab.ocl.core.standard.operations.string.LessOrEqual) {
            return false;
        }
        
        return true;                
    }

    public static boolean isOtherOperation(Operation op) {
        if (isDotOrArrowOperation(op)) {
            return false;
        }
        if(op instanceof Not || op instanceof Negative ||
                op instanceof org.modelinglab.ocl.core.standard.operations.real.Negative){
            return false;
        }
        
        return true;
    }
    
    public static Set<Operation> getPrefixOperations(StaticEnvironment env) {
        OperationsStore opStore = env.getOpStore();
        Set<Operation> prefixOperations = new HashSet<>();
        Iterator<Operation> notOperations = opStore.getOperations(PrimitiveType.BOOLEAN, "not");
        while(notOperations.hasNext()) {
            Operation op = notOperations.next();
            if(op.getOwnedParameters().isEmpty()) {
                prefixOperations.add(op);
            }
        }
        Iterator<Operation> negativeIntegerOperations = opStore.getOperations(PrimitiveType.INTEGER, "-");
        while(negativeIntegerOperations.hasNext()) {
            Operation op = negativeIntegerOperations.next();
            if(op.getOwnedParameters().isEmpty()) {
                prefixOperations.add(op);
            }
        }
        Iterator<Operation> negativeRealOperations = opStore.getOperations(PrimitiveType.REAL, "-");
        while(negativeRealOperations.hasNext()) {
            Operation op = negativeRealOperations.next();
            if(op.getOwnedParameters().isEmpty()) {
                prefixOperations.add(op);
            }
        }
        
        return prefixOperations;
    }
    
    public static Set<UmlClass> getEntities(StaticEnvironment env) {
        Set<UmlClass> allClasses = env.getAllClasses();
        Set<UmlClass> entities = new HashSet<>();
        for (UmlClass umlClass : allClasses) {
            if(isEntity(umlClass)){
                entities.add(umlClass);
            }
        }
        return entities;
    }
    
    public static Set<UmlEnum> getEnumerations(StaticEnvironment env) {
        Set<UmlClass> allClasses = env.getAllClasses();
        Set<UmlEnum> enumerations = new HashSet<>();
        for (UmlClass umlClass : allClasses) {
            if(umlClass instanceof UmlEnum){
                UmlEnum enumeration = (UmlEnum) umlClass;
                enumerations.add(enumeration);
            }
        }
        return enumerations;
    }
    
    public static boolean isEntity(UmlClass entity) {
        return entity.getAnnotation(EntityAnnotation.class) != null;
    }
    
    public static void removeTailWhiteSpaces(StringBuilder sb) {
        if(sb == null) {
            return;
        }
        while(sb.length() > 0) {
            char charAt = sb.charAt(sb.length()-1);
            if(!Character.isWhitespace(charAt)) {
                break;
            }
            sb.deleteCharAt(sb.length()-1);
        }
    }
    
    public static void removeWhiteSpacesWrappingOperators(StringBuilder sb) {
        if(sb == null) {
            return;
        }
        String text = sb.toString();

        text = text.replaceAll("(\\s*)->(\\s*)", "->");
        text = text.replaceAll("(\\s*)(\\.)(\\s*)", ".");
        sb.delete(0, sb.length());
        sb.append(text);
    }
}
