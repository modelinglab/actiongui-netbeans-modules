/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.gtm.oclautocompletion;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.modelinglab.actiongui.core.DefaultImplicitTypesProvider;
import org.modelinglab.actiongui.core.WidgetVariableAnnotation;
import org.modelinglab.actiongui.maven.tools.AGMavenInterface;
import org.modelinglab.actiongui.maven.tools.AGMavenInterfaceFactory;
import org.modelinglab.actiongui.mm.gtm.Gtm;
import org.modelinglab.actiongui.mm.gtm.StandardGtm;
import org.modelinglab.actiongui.mm.gtm.analysis.DepthFirstAdapter;
import org.modelinglab.actiongui.mm.gtm.node.AGtmExp;
import org.modelinglab.actiongui.mm.gtm.node.AOclGtmExpBase;
import org.modelinglab.actiongui.mm.gtm.node.AVarGtmExpBase;
import org.modelinglab.actiongui.mm.gtm.node.PGtmExpBase;
import org.modelinglab.actiongui.mm.gtm.utils.UtilsGtm;
import org.modelinglab.actiongui.netbeans.gtm.oclautocompletion.exceptions.GTMOCLAutocompletionException;
import org.modelinglab.actiongui.netbeans.gtm.oclautocompletion.utils.GTMOCLAutocompletionUtils;
import org.modelinglab.actiongui.tasks.gtmanalyzer.analysis.utils.UtilsGtmA;
import org.modelinglab.actiongui.tasks.gtmmerge.GtmMerge;
import org.modelinglab.actiongui.tasks.gtmmerge.GtmMergeRequest;
import org.modelinglab.actiongui.tasks.gtmparser.GtmParser;
import org.modelinglab.actiongui.tasks.gtmparser.GtmParserRequest;
import org.modelinglab.mm.source.SourceError;
import org.modelinglab.mm.source.SourceSection;
import org.modelinglab.mm.source.SourceTaskResult;
import org.modelinglab.ocl.core.ast.Element;
import org.modelinglab.ocl.core.ast.Namespace;
import org.modelinglab.ocl.core.ast.OperationsStore;
import org.modelinglab.ocl.core.ast.StaticEnvironment;
import org.modelinglab.ocl.core.ast.UmlClass;
import org.modelinglab.ocl.core.ast.expressions.OclExpression;
import org.modelinglab.ocl.core.ast.expressions.TypeExp;
import org.modelinglab.ocl.core.ast.expressions.Variable;
import org.modelinglab.ocl.core.ast.types.Classifier;
import org.modelinglab.ocl.core.ast.types.CollectionType;
import org.modelinglab.ocl.core.exceptions.OclException;
import org.modelinglab.ocl.parser.OclLexerException;
import org.modelinglab.ocl.parser.OclParser;
import org.modelinglab.ocl.parser.OclParserException;
import org.modelinglab.ocl.parser.sablecc.lexer.Lexer;
import org.modelinglab.ocl.parser.sablecc.lexer.LexerException;
import org.modelinglab.ocl.parser.sablecc.node.AInitVariableDeclarationCS;
import org.modelinglab.ocl.parser.sablecc.node.AItsArgOrItVar;
import org.modelinglab.ocl.parser.sablecc.node.ANameOclExpressionCS;
import org.modelinglab.ocl.parser.sablecc.node.ANoTypedNoInitVariableDeclarationCS;
import org.modelinglab.ocl.parser.sablecc.node.ATypedAndInitVariableDeclarationCS;
import org.modelinglab.ocl.parser.sablecc.node.ATypedVariableDeclarationCS;
import org.modelinglab.ocl.parser.sablecc.node.AUntypedItAndOtherItArgOrItVar;
import org.modelinglab.ocl.parser.sablecc.node.Node;
import org.modelinglab.ocl.parser.sablecc.node.PArgOrItVar;
import org.modelinglab.ocl.parser.sablecc.node.POclExpressionCS;
import org.modelinglab.ocl.parser.sablecc.node.PTypeCS;
import org.modelinglab.ocl.parser.sablecc.node.PVariableDeclarationCS;
import org.modelinglab.ocl.parser.sablecc.parser.Parser;
import org.modelinglab.ocl.parser.sablecc.parser.ParserException;
import org.modelinglab.ocl.parser.sablecc.parser.State;
import org.modelinglab.ocl.parser.walker.ConcreteToAbstractMap;
import org.modelinglab.ocl.parser.walker.OclWalker;
import org.openide.util.Utilities;

/**
 * This class obtains an OCL parser each time the auto-completion is activated.
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class GTMOCLParserProvider {
    private static GTMOCLParserProvider instance;
    private String modifiedExpr;
    
    private GTMOCLParserProvider(){
        
    }
    
    public static GTMOCLParserProvider getInstance() {
      if(instance == null) {
         instance = new GTMOCLParserProvider();
      }
      return instance;
    }

    public String getModifiedExpr() {
        return modifiedExpr;
    }
    
    public OclParser getParser(URI dataModelURI, URI guiModelURI, Document guiModelDocument, int caretOffset) throws GTMOCLAutocompletionException {   
        // 1) Reset the modified expression
        this.modifiedExpr = "";
        
        // 2) Load namespace
        AGMavenInterface agmi = AGMavenInterfaceFactory.getDefaultInterface();  
        Namespace namespace;
        try {
            namespace = agmi.unserializeNamespace(Utilities.toFile(dataModelURI));
        } 
        catch (AGMavenInterface.AGMavenInterfaceException ex) {
            throw new GTMOCLAutocompletionException(ex.getMessage());
        }
        
        // 3) Build the ocl parser
        OperationsStore os = new DefaultImplicitTypesProvider().createStore();
        StaticEnvironment env = new StaticEnvironment(os);    
        OclParser oclParser = new OclParser(env);
           
        // 4) Add all entities and enumerations in a first scope
        Set<Element> entities = namespace.getOwnedMembers();
        for (Element element : entities) {
            if(element instanceof UmlClass){
                UmlClass entity = (UmlClass) element;
                env.addElement(entity, false);
            }
        }        
        
        // 5) if the expression is empty --> return the parser only with datamodel information
        StringBuilder initialExpr;
        try {
            initialExpr = GTMOCLAutocompletionUtils.getTextFromCaretToStartSymbol(guiModelDocument, caretOffset);
        } 
        catch (BadLocationException ex) {
            throw new GTMOCLAutocompletionException("Error obtaining the expression: " + ex.getMessage());
        }
        if(initialExpr.toString().isEmpty()) {
            return oclParser;
        }

        // 6) Parse the document to get a gtm (GUI text model)
        Gtm gtm = parseGTMModel(guiModelURI, guiModelDocument);
        List<Gtm> gtms = new ArrayList<>();
        gtms.add(gtm);
        GtmMergeRequest mergeRequest = new GtmMergeRequest(gtms, namespace);
        GtmMerge merge = new GtmMerge(mergeRequest);
        SourceTaskResult<Gtm, StandardGtm> mergeResult = merge.call();
        Collection<SourceError<Gtm>> mergeErrors = mergeResult.getErrors();
        if(!mergeErrors.isEmpty()) {
            SourceError<Gtm> sourceError = mergeErrors.iterator().next();
            StringBuilder sb = new StringBuilder("Error in the GUI model at ");
            if (sourceError.getErrorSection() == null) {
                sb.append("[Undefined section] ");
            }
            else {
                sb.append("[");
                sb.append(sourceError.getErrorSection().getStartPosition().getLine());
                sb.append(',');
                sb.append(sourceError.getErrorSection().getStartPosition().getColumn());
                sb.append("]");
            }
            sb.append(": ").append(sourceError.getErrorMsg());
            throw new GTMOCLAutocompletionException(sb.toString());
        }
        StandardGtm standardGtm = mergeResult.getOutput();
        

        
        

        // 7) Add widget variables in a second scope and create the modified expression
        addWidgetVariables(namespace, standardGtm, caretOffset, oclParser);
        assert modifiedExpr != null;
        
        // 8) Add the possible visible iterator and let variables, in a third scope
        addVisibleExpVariables(env);

        // 9) Return the parser
        return oclParser;
    }
    
    private Gtm parseGTMModel(URI guiModelURI, Document guiModelDocument) throws GTMOCLAutocompletionException {
        // get the text
        String text;
        try {
            // get the input stream
            text = guiModelDocument.getText(0, guiModelDocument.getLength());
        } 
        catch (BadLocationException ex) {
            throw new GTMOCLAutocompletionException(ex.getMessage());
        }
        
        // parse the document
        GtmParserRequest request = new GtmParserRequest(guiModelURI, text);
        GtmParser parser = new GtmParser(request);
        SourceTaskResult<Gtm, Gtm> result;
        try {
            result = parser.call();
        } 
        catch (IOException ex) {
            throw new GTMOCLAutocompletionException(ex.getMessage());
        }
        
        // If there are errors --> return an exception with he first error information.
        Collection<SourceError<Gtm>> gtmParserErrors = result.getErrors();
        if (!gtmParserErrors.isEmpty()) {
            SourceError<Gtm> sourceError = gtmParserErrors.iterator().next();           
            StringBuilder sb = new StringBuilder("Error parsing the GUI model at ");
            if (sourceError.getErrorSection() == null) {
                sb.append("[Undefined section] ");
            }
            else {
                sb.append("[");
                sb.append(sourceError.getErrorSection().getStartPosition().getLine());
                sb.append(',');
                sb.append(sourceError.getErrorSection().getStartPosition().getColumn());
                sb.append("]");
            }
            sb.append(": ").append(sourceError.getErrorMsg());
            throw new GTMOCLAutocompletionException(sb.toString());
        }
        
        Gtm gtm = result.getOutput();
        return gtm;
    }
    
    private void addVisibleExpVariables(StaticEnvironment env) {        
        env.addScope();
        
        // 1) Get the expression from the beginning to the caret position
        if(modifiedExpr.isEmpty()) {
            return;
        }
        
        // 2) Parse (at grammar level) the expression
        PushbackReader reader = new PushbackReader(new StringReader(modifiedExpr), modifiedExpr.length());
        Lexer lexer = new Lexer(reader);
        Parser p = new Parser(lexer);       
        boolean parserException = false;
        try {
            p.parse();
        } 
        // if lexer or IO exception parsing the expression --> return empty vars
        catch (LexerException | IOException ex) {
            return;
        } 
        // if parser exception --> there would be open iterator and let with visible variables
        catch (ParserException ex) {
            parserException = true;
        }
        
        // 3) If not parser exception --> the expression parses correctly --> there are no visible variables
        if(!parserException) {
            return;
        }
        
        // 4) Get and analyze the stack, looking for visible variables
        ListIterator<Object> stack = p.getStack();
        while(stack.hasPrevious()) {
            stack.previous();
        }
        List<Object> list = new ArrayList<>();
        while(stack.hasNext()) {
            list.add(stack.next());
        }
        for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i);
            State state = (State) o;
            ArrayList<Object> nodes = state.getNodes();
            if(nodes == null) {
                continue;
            }
            if(nodes.isEmpty()) {
                continue;
            }
            Object element = nodes.get(0);
            // if the node is not a PArgOrItVar --> skip
            PArgOrItVar pArgOrItVar;
            try {
                pArgOrItVar = (PArgOrItVar) element;
            }
            catch(Exception ex) {
                continue;
            }
            // if the element has parent --> the iterator has been parsed but not reduced --> those variables
            // are not visible (there are certain cases in which the iterator has not completely been reduced but its
            // cariables are not vivibles) --> skip
            if(pArgOrItVar.parent() != null) {
                continue;
            }
            if(pArgOrItVar instanceof AItsArgOrItVar) {
                AItsArgOrItVar aItsArgOrItVar = (AItsArgOrItVar)element;
                LinkedList<PVariableDeclarationCS> vars = aItsArgOrItVar.getVars();
                for (PVariableDeclarationCS pVariableDeclarationCS : vars) {
                    Variable variable = createVar(list, i, pVariableDeclarationCS, env);
                    if(variable == null) {
                        continue;
                    }
                    // if the variable has been previously added --> skip
                    if(env.lookup(variable.getName()) == null) {
                        env.addElement(variable);
                    }
                }
            }
            else if(pArgOrItVar instanceof AUntypedItAndOtherItArgOrItVar) {
                AUntypedItAndOtherItArgOrItVar aUntypedItAndOtherItArgOrItVar = (AUntypedItAndOtherItArgOrItVar)pArgOrItVar;
                POclExpressionCS undeclaredVariableExp = aUntypedItAndOtherItArgOrItVar.getUndeclaredVariableExp();
                if(!(undeclaredVariableExp instanceof ANameOclExpressionCS)) {
                    continue;
                }
                ANameOclExpressionCS aNameOclExpressionCS = (ANameOclExpressionCS)undeclaredVariableExp;
                String nameVar1 = aNameOclExpressionCS.getName().toString().trim();
                PVariableDeclarationCS var2 = aUntypedItAndOtherItArgOrItVar.getVar2();
                Variable variable2 = createVar(list, i, var2, env);
                if(variable2 == null) {
                    continue;
                }
                Variable variable1 = new Variable(nameVar1);
                variable1.setType(variable2.getType());
                // if the variable has been previously added --> skip
                if(env.lookup(variable1.getName()) == null) {
                    env.addElement(variable1);
                }
                // if the variable has been previously added --> skip
                if(env.lookup(variable2.getName()) == null) {
                    env.addElement(variable2);
                }
            }           
        }
    }
    
    private Variable createVar(List<Object> list, int i, PVariableDeclarationCS pVariableDeclarationCS, StaticEnvironment env) {
        // if variable has init expression --> skip
        if(pVariableDeclarationCS instanceof AInitVariableDeclarationCS || pVariableDeclarationCS instanceof ATypedAndInitVariableDeclarationCS) {
            return null;
        }
        // if variable has declared its type --> add the variable in case the type parses and has the proper type 
        if(pVariableDeclarationCS instanceof ATypedVariableDeclarationCS) {
            ATypedVariableDeclarationCS var = (ATypedVariableDeclarationCS)pVariableDeclarationCS;
            PTypeCS pTypeCS = var.getType();
            Object oclExpressionType;
            try {
                oclExpressionType = partialParse(pTypeCS, env);
            }
            // if expression type does not parse --> skip
            catch (OclParserException | OclLexerException | IOException | OclException ex) {
                return null;
            }
            // if expression type it not Classifier nor TypeExp --> skip
            Classifier typeVar;
            try {
                typeVar = (Classifier) oclExpressionType;
            }
            catch(Exception ex1) {
                try {
                    TypeExp typeExp = (TypeExp) oclExpressionType;
                    typeVar = typeExp.getType().getReferredClassifier();
                }
                catch(Exception ex2) {
                    return null;
                }
            }
            String nameVar = var.getName().toString().trim();
            // if the variable has been previously added --> skip
            if(env.lookup(nameVar) != null) {
                return null;
            }
            Variable variable = new Variable(nameVar);
            variable.setType(typeVar);
            return variable;
        }
        // if variable does not declare its type --> get the source expression and try to parse it
        if(pVariableDeclarationCS instanceof ANoTypedNoInitVariableDeclarationCS) {
            ANoTypedNoInitVariableDeclarationCS var = (ANoTypedNoInitVariableDeclarationCS) pVariableDeclarationCS;
            // 4 positions before, there should be the source expression of the iterator
            if(i < 4) {
                return null;
            }
            Object object = list.get(i-4);
            State st = (State) object;
            ArrayList<Object> nodesList = st.getNodes();
            if(nodesList == null) {
                return null;
            }
            if(nodesList.isEmpty()) {
                return null;
            }
            Object e = nodesList.get(0);
            if(!(e instanceof Node)) {
                return null;
            }
            Node sourceNode = (Node) e;
            Object srcExpr;
            try {
                srcExpr = partialParse(sourceNode, env);
            }
            // if source expression does not parse --> skip
            catch (OclParserException | OclLexerException | IOException | OclException ex) {
                return null;
            } 
            OclExpression sourceExpr;
            // if parser expression is not an ocl expression --> skip
            try {
                sourceExpr = (OclExpression) srcExpr;
            }
            catch(Exception ex) {
                return null;
            }
            Classifier type = sourceExpr.getType();
            CollectionType collectionType;
            // if type of source exp is not a collection --> skip
            try {
                collectionType = (CollectionType)type;
            }
            catch(Exception ex) {
                return null;
            }
            
            String nameVar = var.getName().toString().trim();           
            Classifier typeVar = collectionType.getElementType();
            Variable variable = new Variable(nameVar);
            variable.setType(typeVar);
            return variable;
        }
        return null;
    }
    
    private Object partialParse(Node node, StaticEnvironment env) throws OclParserException, OclLexerException, IOException, OclException {
        PartialOCLWalker walker = new PartialOCLWalker(env);
        
        int numberOfScopes = env.getNumberOfScopes();
        try {
            node.apply(walker);
            assert numberOfScopes == env.getNumberOfScopes();
        }
        finally {
            assert numberOfScopes <= env.getNumberOfScopes();
            while (numberOfScopes < env.getNumberOfScopes()) {
                env.removeScope();
            }
        }
        return walker.getResult(node);
    }

    /**
     * Add to the parser environment the needed widget variables, and creates the modified 
     * @param namespace
     * @param standardGtm
     * @param guiModelDocument
     * @param caretOffset
     * @param parser
     * @return 
     */
    private void addWidgetVariables(Namespace namespace, StandardGtm standardGtm, int caretOffset, OclParser parser) throws GTMOCLAutocompletionException {
        // 1) Find the gtm expression that corresponds to the caret position
        GtmExpressionFinderDFA finder = new GtmExpressionFinderDFA(standardGtm, caretOffset);
        standardGtm.getMergedGtmModel().apply(finder);
        AGtmExp gtmExpr = finder.getGtmExpr();
        // If the expression is not found --> this case happends when the inserted text does not correspond to
        // the obtained text, for example when keep pressing the delete key -->inconsistency --> skip
        if(gtmExpr == null) {
            return;
        }
        
        // 2) Collect all widget variables from the GUI model
        Map<String, Classifier> widgetVariables = UtilsGtmA.collectVariables(standardGtm, namespace);
        Map<org.modelinglab.actiongui.mm.gtm.node.Node, Map<String, Classifier>> temporalVariables = UtilsGtmA.collectTemporalVariables(standardGtm, namespace, widgetVariables, parser);
        
        // 3) Add to the parser environment, the needed widget variables and build the modified OCL expression
        LinkedList<PGtmExpBase> exprElements = gtmExpr.getGtmExpBase();
        int varIndex = 0;
        String pureOclExp = new String();
        Map<String, Classifier> tempVariables = temporalVariables.get(gtmExpr);
        StaticEnvironment env = parser.getEnv();
        env.addScope();
        WidgetVariableAnnotation annotation = WidgetVariableAnnotation.getInstance();
        for (PGtmExpBase expr : exprElements) {
            if (expr instanceof AOclGtmExpBase) {
                AOclGtmExpBase aOcl = (AOclGtmExpBase) expr;
                SourceSection sourceSection = standardGtm.getSourceSection(aOcl);
                int start = sourceSection.getStartPosition().getOffset();
                int end = sourceSection.getEndPosition().getOffset();
                if(start <= caretOffset && (end + 1) >= caretOffset) {
                    String text = aOcl.getExp().getText();
                    String subText = text.substring(0, caretOffset-start);
                    pureOclExp += subText;
                    break;
                }
                pureOclExp += aOcl.getExp().getText();
                continue;
            }
            if (expr instanceof AVarGtmExpBase) {
                AVarGtmExpBase aVar = (AVarGtmExpBase) expr;
                String id = UtilsGtm.translatePathToGlobalId(aVar.getVar());
                Classifier classifier;
                // is a temporal variable
                if (tempVariables.containsKey(id)) {
                    if(tempVariables.get(id) == null) {
                        throw new GTMOCLAutocompletionException("The type of the temporal variable '" + id + "' could not be resolved");
                    }
                    classifier = tempVariables.get(id);
                } 
                // is a widget variable
                else if (widgetVariables.containsKey(id)) {
                    if(widgetVariables.get(id) == null) {
                        throw new GTMOCLAutocompletionException("The type of the widget variable '" + id + "' could not be resolved");
                    }
                    classifier = widgetVariables.get(id);
                } 
                // The variable has not been declared. The expression cannot be parsed.
                else {
                    throw new GTMOCLAutocompletionException("The variable '" + id + "' has not been declared in the GUI model");
                }
                String oclVarName = UtilsGtm.translatePathToLocalId(aVar.getVar());
                oclVarName += varIndex++;
                // ensures a fresh name
                while(env.lookup(oclVarName) != null) {
                    oclVarName += varIndex++;
                }
                Variable oclVar = new Variable(oclVarName);
                oclVar.setType(classifier);
                oclVar.setAnnotation(annotation);
                env.addElement(oclVar, false);
                pureOclExp += oclVarName;
            }
        }
        modifiedExpr = pureOclExp;
    }
    
    private class PartialOCLWalker extends OclWalker {

        public PartialOCLWalker(StaticEnvironment env) {
            super(env);
            this.concreteNodeToAbstractNode = new ConcreteToAbstractMap();
        }
        
        public Object getResult(Node node) {
            return this.concreteNodeToAbstractNode.get(node);
        }
    }
    
    private class GtmExpressionFinderDFA extends DepthFirstAdapter {
        private AGtmExp gtmExpr;
        private final StandardGtm standardGtm;
        private final int gtmExprPosition;

        public GtmExpressionFinderDFA(StandardGtm standardGtm, int gtmExprPosition) {
            this.standardGtm = standardGtm;
            this.gtmExprPosition = gtmExprPosition;
        }

        public AGtmExp getGtmExpr() {
            return gtmExpr;
        }

        @Override
        public void inAGtmExp(AGtmExp node) {
            SourceSection sourceSection = standardGtm.getSourceSection(node);
            int startPosition = sourceSection.getStartPosition().getOffset();
            int endPosition = sourceSection.getEndPosition().getOffset();
            if(startPosition <= gtmExprPosition && endPosition >= gtmExprPosition) {
                gtmExpr = node;
            }
        }
        
    }
}
