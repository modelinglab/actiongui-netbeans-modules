/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.dtm.invariants.oclautocompletion;

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
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.modelinglab.actiongui.core.DefaultImplicitTypesProvider;
import org.modelinglab.actiongui.maven.tools.AGMavenInterface;
import org.modelinglab.actiongui.maven.tools.AGMavenInterfaceFactory;
import org.modelinglab.actiongui.netbeans.autocompletion.ocl.OCLCompletionItemsProvider;
import org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems.OCLCompletionItem;
import org.modelinglab.actiongui.netbeans.dtm.invariants.oclautocompletion.exception.DTMInvariantsOCLAutocompletionException;
import org.modelinglab.actiongui.tasks.dtminvariants.parser.ContextType;
import org.modelinglab.actiongui.tasks.dtminvariants.parser.InvariantType;
import org.modelinglab.actiongui.tasks.dtminvariants.parser.ItmParser;
import org.modelinglab.actiongui.tasks.dtminvariants.parser.ItmParserRequest;
import org.modelinglab.actiongui.tasks.dtminvariants.textual.model.Itm;
import org.modelinglab.mm.source.SourceError;
import org.modelinglab.mm.source.SourcePosition;
import org.modelinglab.mm.source.SourceSection;
import org.modelinglab.mm.source.SourceTaskResult;
import org.modelinglab.ocl.core.ast.Element;
import org.modelinglab.ocl.core.ast.Namespace;
import org.modelinglab.ocl.core.ast.OperationsStore;
import org.modelinglab.ocl.core.ast.StaticEnvironment;
import org.modelinglab.ocl.core.ast.UmlClass;
import org.modelinglab.ocl.core.ast.annotations.EntityAnnotation;
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
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Utilities;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.TopComponent;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
@MimeRegistration(mimeType = "text/xml", service = CompletionProvider.class)
public class DTMInvariantsOCLCompletionProvider implements CompletionProvider{
    private final InputOutput io = IOProvider.getDefault().getIO("DTM Invariants OCL auto-completion", false);
    private final String CONTEXT_VAR = "self";
    private final OCLCompletionItemsProvider itemsProvider = new OCLCompletionItemsProvider(true);

    @Override
    public CompletionTask createTask(int queryType, final JTextComponent jtc) {
        if(queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return null;
        }
        return new AsyncCompletionTask(new AsyncCompletionQuery() {

            @Override
            protected void query(CompletionResultSet completionResultSet, Document document, int caretOffset) {                 
                // 1) Get the (possibly) XML Invariants file
                DataObject dob = TopComponent.getRegistry().getActivated().getLookup().lookup(DataObject.class);
                FileObject fob = dob.getPrimaryFile();
                String nameFile = fob.getName();
                if(!nameFile.endsWith(".inv")) {
                    completionResultSet.finish();
                    return;
                }
                
                // 2) Build the XML Invariants model
                URI dtmInvariantsModelURI = fob.toURI();
                Itm itm;
                try {
                    itm = buildDTMInvariantsModel(dtmInvariantsModelURI, document);
                } 
                catch (DTMInvariantsOCLAutocompletionException ex) {
                    // If the XML file is not an XML invariant file or contains errors --> hide errors
                    String errorMessage = "Error building the invariants model: " + ex.getMessage();
                    printError(errorMessage);
                    completionResultSet.finish();
                    return;
                }
                
                // 3) In case the caret is within the expression of an invariant, get the invariant
                InvariantType invariantType = getInvariant(itm, caretOffset);
                if(invariantType == null) {
                    completionResultSet.finish();
                    return;
                }
                
                // 4) Get the data model URI
                Project p = TopComponent.getRegistry().getActivated().getLookup().lookup(Project.class);
                if (p == null) {
                    dob = TopComponent.getRegistry().getActivated().getLookup().lookup(DataObject.class);
                    if (dob != null) {
                        FileObject fo = dob.getPrimaryFile();
                        p = FileOwnerQuery.getOwner(fo);
                    }
                }
                if(p == null) {
                    completionResultSet.finish();
                    return;
                }
                FileObject projectDirectory = p.getProjectDirectory();
                if(projectDirectory == null){
                    String errorMessage = "Error getting the current project";
                    printError(errorMessage);
                    completionResultSet.finish();
                    return;
                }
                FileObject parent = projectDirectory.getParent();
                if(parent == null) {
                    String errorMessage = "Error getting the parent project of the current project";
                    printError(errorMessage);
                    completionResultSet.finish();
                    return;
                }
                FileObject datamodelFO = parent.getFileObject("dtm/target/classes/umlclasses.xml");
                if(datamodelFO == null) {
                    String errorMessage = "Error getting the application data model. "
                            + "Please, compile the DTm project, in order to build the application data model.";
                    printError(errorMessage);
                    completionResultSet.finish();
                    return;
                }
                URI datamodelURI = datamodelFO.toURI();

                // 5) Build OCL parser
                OclParser parser;
                try {
                    parser = getParser(datamodelURI, document, itm, invariantType, caretOffset);
                } 
                catch (DTMInvariantsOCLAutocompletionException ex) {
                    String errorMessage = "Error getting the OCl parser: " + ex.getMessage();
                    printError(errorMessage);
                    completionResultSet.finish();
                    return;
                }
                
                // 6) Build completion items
                Collection<CompletionItem> completionItems;
                try {
                    completionItems = buildCompletionItems(parser, itm, invariantType, document, caretOffset);
                    completionResultSet.addAllItems(completionItems);
                } 
                catch (DTMInvariantsOCLAutocompletionException ex) {
                    printError(ex.getMessage());
                }
                
                // 7) Finish the completion item list
                completionResultSet.finish();
            }      
        }, jtc);
    }

    @Override
    public int getAutoQueryTypes(JTextComponent jtc, String string) {
        // a dot '.' operator has been typed
        if(string.equals(".")){
            return COMPLETION_QUERY_TYPE;
        }
        // an arrow '->' operator has been typed
        StyledDocument doc = (StyledDocument) jtc.getDocument();
        try {
            String text = doc.getText(jtc.getCaretPosition() - 2, 2);
            if(text.equals("->")) {
                return COMPLETION_QUERY_TYPE;
            }
        } 
        catch (BadLocationException ex) {
            return 0;
        }
        return 0;
    }
    
    private Itm buildDTMInvariantsModel(URI dtmInvariantsModelURI, Document document) throws DTMInvariantsOCLAutocompletionException {

        // 1) Get the text to parse
        String textToParse;       
        try {
            textToParse = document.getText(document.getStartPosition().getOffset(), document.getLength());
        } 
        catch (BadLocationException ex) {
            throw new DTMInvariantsOCLAutocompletionException(ex.getMessage());
        }
        
        // 2) Parse the text
        ItmParserRequest request = new ItmParserRequest(dtmInvariantsModelURI, textToParse);
        ItmParser parser = new ItmParser(request);
        SourceTaskResult<Itm, Itm> result = parser.call();
        
        // If there are errors --> return an exception with he first error information.
        Collection<SourceError<Itm>> itmParserErrors = result.getErrors();
        if (!itmParserErrors.isEmpty()) {
            SourceError<Itm> sourceError = itmParserErrors.iterator().next();           
            StringBuilder sb = new StringBuilder(" at ");
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
            throw new DTMInvariantsOCLAutocompletionException(sb.toString());
        }
        
        // 3) Buld the invariants model
        Itm itm = result.getOutput();
        return itm;
    }
    
    private InvariantType getInvariant(Itm itm, int caretOffset) {
        Map<InvariantType, SourceSection> expressionPositions = itm.getExpressionPositions();
        for (InvariantType invariantType : expressionPositions.keySet()) {
            SourceSection sourceSection = expressionPositions.get(invariantType);
            int start = sourceSection.getStartPosition().getOffset();
            int end = sourceSection.getEndPosition().getOffset();
            if(start <= caretOffset && end >= caretOffset) {
                return invariantType;
            }
            
        }
        return null;
    }
    
    public OclParser getParser(URI dataModelURI, Document document, Itm itm, InvariantType invariantType, int caretOffset) throws DTMInvariantsOCLAutocompletionException {           
        // 1) Load namespace
        AGMavenInterface agmi = AGMavenInterfaceFactory.getDefaultInterface();  
        Namespace namespace;
        try {
            namespace = agmi.unserializeNamespace(Utilities.toFile(dataModelURI));
        } 
        catch (AGMavenInterface.AGMavenInterfaceException ex) {
            throw new DTMInvariantsOCLAutocompletionException(ex.getMessage());
        }
        
        // 2) Build the ocl parser
        OperationsStore os = new DefaultImplicitTypesProvider().createStore();
        StaticEnvironment env = new StaticEnvironment(os);    
        OclParser oclParser = new OclParser(env);
           
        // 3) Add all entities and enumerations in a first scope
        Set<Element> entities = namespace.getOwnedMembers();
        for (Element element : entities) {
            if(element instanceof UmlClass){
                UmlClass entity = (UmlClass) element;
                env.addElement(entity, false);
            }
        }        
        
        // 4) Add a new scope for variables
        env.addScope();
         
        // 5) Add self context variable (if exists)
        ContextType invariantContext = itm.getInvariantContext(invariantType);
        if(invariantContext != null) {
            String entity = invariantContext.getEntity();
            if(entity == null) {
                SourcePosition contextPosition = itm.getElementPosition(invariantContext).getStartPosition();
                StringBuilder errorSb = new StringBuilder("Error at ");
                errorSb.append("[");
                errorSb.append(contextPosition.getLine());
                errorSb.append(',');
                errorSb.append(contextPosition.getColumn());
                errorSb.append("]");
                errorSb.append(": ").append("the context entity is missing.");
                throw new DTMInvariantsOCLAutocompletionException(errorSb.toString());
            }
            Element element = env.lookup(entity);
            if(element == null) {
                SourcePosition contextPosition = itm.getElementPosition(invariantContext).getStartPosition();
                StringBuilder errorSb = new StringBuilder("Error at ");
                errorSb.append("[");
                errorSb.append(contextPosition.getLine());
                errorSb.append(',');
                errorSb.append(contextPosition.getColumn());
                errorSb.append("]");
                errorSb.append(": ").append("the context entity '").append(entity).append("' does not exist in the data model.");
                throw new DTMInvariantsOCLAutocompletionException(errorSb.toString());
            }
            if(!(element instanceof UmlClass)) {
                SourcePosition contextPosition = itm.getElementPosition(invariantContext).getStartPosition();
                StringBuilder errorSb = new StringBuilder("Error at ");
                errorSb.append("[");
                errorSb.append(contextPosition.getLine());
                errorSb.append(',');
                errorSb.append(contextPosition.getColumn());
                errorSb.append("]");
                errorSb.append(": ").append("the context entity '").append(entity).append("' does not exist in the data model.");
                throw new DTMInvariantsOCLAutocompletionException(errorSb.toString());
            }
            UmlClass umlClass = (UmlClass) element;
            EntityAnnotation annotation = umlClass.getAnnotation(EntityAnnotation.class);
            if(annotation == null) {
                SourcePosition contextPosition = itm.getElementPosition(invariantContext).getStartPosition();
                StringBuilder errorSb = new StringBuilder("Error at ");
                errorSb.append("[");
                errorSb.append(contextPosition.getLine());
                errorSb.append(',');
                errorSb.append(contextPosition.getColumn());
                errorSb.append("]");
                errorSb.append(": ").append("the context entity '").append(entity).append("' does not exist in the data model.");
                throw new DTMInvariantsOCLAutocompletionException(errorSb.toString());
            }
            Variable variable = new Variable(CONTEXT_VAR);
            variable.setType(umlClass);
            env.addElement(variable);
        }
        
        // 6) Add visible iterator and let variables
        addVisibleExpVariables(document, itm, invariantType, caretOffset, oclParser);
        
        // 7) Return the parser
        return oclParser;
    }
    
    private Collection<CompletionItem> buildCompletionItems(OclParser parser, Itm itm, InvariantType invariantType, Document document, int caretOffset) throws DTMInvariantsOCLAutocompletionException {
        Collection<CompletionItem> items = new ArrayList<>();
        
        // 1) Calculate the accumulator
        StringBuilder accumulator = new StringBuilder();
        int startOffset = itm.getExpressionPosition(invariantType).getStartPosition().getOffset();
        try {
            String textBefore = document.getText(startOffset, caretOffset-startOffset);
            accumulator = new StringBuilder(textBefore);
        } 
        catch (BadLocationException ex) {
            throw new DTMInvariantsOCLAutocompletionException("Error getting the text before the cursor: " + ex.getMessage());
        }
 
        // 2) Replace characters '<' and '>' in the accumulator
        accumulator = replaceSpecialChars(accumulator);
        
        // 2) Build items
        Collection<OCLCompletionItem> buildOCLCompletionItems = itemsProvider.buildOCLCompletionItems(accumulator, parser, caretOffset);
        items.addAll(buildOCLCompletionItems);
        return items;
    }
    
    /**
     * This method obtains the visible variables (iterator variables) from the beginning 
     * of the OCL expression to the caret offset, and add them to the parser environment.
     * @param document
     * @param caretOffset
     * @param parser 
     */
    private void addVisibleExpVariables(Document document, Itm itm, InvariantType invariantType, int caretOffset, OclParser parser) {        
        StaticEnvironment env = parser.getEnv();
        
        // 1) Get the expression from the beginning to the caret position
        String expr;
        int startOffset = itm.getExpressionPosition(invariantType).getStartPosition().getOffset();
        try {
            expr = document.getText(startOffset, caretOffset-startOffset);
        }
        catch (BadLocationException ex) {
            return;
        }
        if(expr.isEmpty()) {
            return;
        }
        expr = replaceSpecialChars(expr);
        
        // 2) Parse (at grammar level) the expression
        PushbackReader reader = new PushbackReader(new StringReader(expr), expr.length());
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

    private StringBuilder replaceSpecialChars(StringBuilder input) {
        String toString = input.toString();
        toString = toString.replace("&lt;", "<");
        toString = toString.replace("&gt;", ">");
        return new StringBuilder(toString);
    }
    
    private String replaceSpecialChars(String input) {
        String output = input.replace("&lt;", "<");
        output = output.replace("&gt;", ">");
        return output;
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
    
    private void printError(String errorMessage) {
        io.select();
        io.getErr().println (errorMessage);  //this text should appear in red
        io.getErr().close();
        Completion.get().hideAll();
    }
}
        
    