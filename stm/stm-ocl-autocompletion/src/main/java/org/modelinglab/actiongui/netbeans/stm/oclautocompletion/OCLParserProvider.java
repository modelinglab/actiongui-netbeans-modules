/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.stm.oclautocompletion;

import com.meaningfulmodels.actiongui.vm.core.IsUserAnnotation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.modelinglab.actiongui.core.DefaultImplicitTypesProvider;
import org.modelinglab.actiongui.maven.tools.AGMavenInterface;
import org.modelinglab.actiongui.maven.tools.AGMavenInterfaceFactory;
import org.modelinglab.actiongui.mm.stm.Stm;
import org.modelinglab.actiongui.mm.stm.StmAction;
import org.modelinglab.actiongui.mm.stm.StmAction.KindOfAction;
import org.modelinglab.actiongui.mm.stm.StmAuthorizationConstraint;
import org.modelinglab.actiongui.mm.stm.StmPermission;
import org.modelinglab.actiongui.mm.stm.StmRole;
import org.modelinglab.actiongui.netbeans.stm.oclautocompletion.exceptions.STMAutocompletionException;
import org.modelinglab.actiongui.tasks.stmparser.StmParser;
import org.modelinglab.actiongui.tasks.stmparser.StmParserRequest;
import org.modelinglab.mm.source.SourceElement.ElementIdDeclaration;
import org.modelinglab.mm.source.SourceError;
import org.modelinglab.mm.source.SourceTaskResult;
import org.modelinglab.ocl.core.ast.AssociationEnd;
import org.modelinglab.ocl.core.ast.Attribute;
import org.modelinglab.ocl.core.ast.Element;
import org.modelinglab.ocl.core.ast.Namespace;
import org.modelinglab.ocl.core.ast.OperationsStore;
import org.modelinglab.ocl.core.ast.Property;
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
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.windows.TopComponent;

/**
 * This class contains an OCL parser (with its context), for each AG application in which the auto-completion is used.
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class OCLParserProvider {
    private static OCLParserProvider instance;
    private final Map<String,Date> lastModificationDates;
    private final Map<String,OclParser> parsers;
    private final String CALLER = "caller";
    private final String SELF = "self";
    private final String VALUE = "value";
    private final String TARGET = "target";
    
    private OCLParserProvider(){
        this.lastModificationDates = new HashMap<>();
        this.parsers = new HashMap<>();
    }
    
    public static OCLParserProvider getInstance() {
      if(instance == null) {
         instance = new OCLParserProvider();
      }
      return instance;
    }

    public OclParser getParser(Document document, int caretOffset) throws STMAutocompletionException {
        OclParser oclParser;
        
        // 1) Get current AG application name and data-model file
        Project p = TopComponent.getRegistry().getActivated().getLookup().lookup(Project.class);
        if (p == null) {
            DataObject dob = TopComponent.getRegistry().getActivated().getLookup().lookup(DataObject.class);
            if (dob != null) {
                FileObject fo = dob.getPrimaryFile();
                p = FileOwnerQuery.getOwner(fo);
            }
        }
        FileObject projectDirectory = p.getProjectDirectory();
        if(projectDirectory == null){
            throw new STMAutocompletionException("Error getting the project of the file.");
        }
        FileObject parent = projectDirectory.getParent();
        if(parent == null) {
            throw new STMAutocompletionException("Error getting the parent project of the file.");
        }
        String nameApp = parent.getName();
        FileObject datamodelFO = parent.getFileObject("dtm/target/classes/umlclasses.xml");
        if(datamodelFO == null) {
            
        }
        
        // 2) Parse the document to get a stm (secure text model)
        Stm stm = parseDocument(document);
        
        // 3) Get the ocl parser
        Date newModified = datamodelFO.lastModified();
        if(lastModificationDates.containsKey(nameApp)) {
            Date lastModified = lastModificationDates.get(nameApp);
            if(lastModified.before(newModified)) {
                lastModificationDates.put(nameApp, newModified);
                oclParser = createParser(datamodelFO, stm, caretOffset, document);
                parsers.put(nameApp, oclParser);
            }
            else {
                oclParser = parsers.get(nameApp);
                updateParser(oclParser, stm, caretOffset, document);
            }
        }
        else {
            oclParser = createParser(datamodelFO, stm, caretOffset, document);
            lastModificationDates.put(nameApp, newModified);            
            parsers.put(nameApp, oclParser);
        }
        return oclParser;
    }

    private OclParser createParser(FileObject datamodelFO, Stm stm, int caretOffset, Document document) throws STMAutocompletionException {
        OclParser oclParser = new OclParser();
        
        // 1) Load namespace
        AGMavenInterface agmi = AGMavenInterfaceFactory.getDefaultInterface();
        Namespace namespace;
        try {
            namespace = agmi.unserializeNamespace(datamodelFO.getInputStream());
        } 
        catch (FileNotFoundException | AGMavenInterface.AGMavenInterfaceException ex) {
            throw new STMAutocompletionException(ex.getMessage());
        }
        
        // 2) Build environment with datamodel and stm model information        
        OperationsStore os = new DefaultImplicitTypesProvider().createStore();
        StaticEnvironment env = new StaticEnvironment(os);
        
        // 2.1) Add all entities, enumerations, and user entity, in a first scope
        UmlClass callerEntity = null;
        Set<Element> entities = namespace.getOwnedMembers();
        for (Element element : entities) {
            if(element instanceof UmlClass){
                UmlClass entity = (UmlClass) element;
                env.addElement(entity, false);
                if(entity.getAnnotation(IsUserAnnotation.class) != null){
                    callerEntity = entity;
                }
            }
        }        
        if(callerEntity == null) {
            throw new STMAutocompletionException("There is no entity defined as user (caller) in the data-model.");
        }
        Variable callerVariable = new Variable(CALLER);
        callerVariable.setType(callerEntity);
        env.addElement(callerVariable, false);
        
        // 2.2) Add a new scope for the rest of possible variables ('self', 'value', and 'target')
        //      and possibly visble iterator and let variables
        env.addScope();
        
        // 3) Set the built environment to the parser
        oclParser.setEnv(env);
        
        // 4) Call update parser to update the rest of possible variables ('self', 'value', and 'target')
        //      and possibly visible iterator and let variables
        updateParser(oclParser, stm, caretOffset, document);
        
        // 5) Return the parser
        return oclParser;
    }

    /**
     * This method loads in the ocl parser environment, the possible variables that can be used in the ocl expression,
     * depending on the permission and actions associated to the ocl expression, and the iterator and let visible variables
     * @param oclParser
     * @param stm
     * @param caretOffset 
     */
    private void updateParser(OclParser oclParser, Stm stm, int caretOffset, Document document) throws STMAutocompletionException {
        // 1) Remove the last scope of the environment, since it is dedicated for old variables 'self', 'value', and 'target'
        StaticEnvironment env = oclParser.getEnv();
        env.removeScope();
        
        // 2) Add a new scope for new variables 'self', 'value', and 'target'
        //      and possibly visible iterator and let variables
        env.addScope();
        
        // 3) Create 'self', 'value', and 'target' variables
        StmPermission permission = null;
        
        for (StmRole stmRole : stm.getRoles().values()) {
            for (StmPermission stmPermission : stmRole.getPermissions().values()) {
                int start = stmPermission.getStartPosition().getOffset();
                int end = stmPermission.getEndPosition().getOffset();
                if(caretOffset > start && caretOffset < end) {
                    permission = stmPermission;
                    break;
                }
            }
        }
        if (permission == null) {
            throw new STMAutocompletionException("The authorization constraint is not declared within a permission.");
        }
        UmlClass selfType = addSelfVariable(permission, oclParser, caretOffset);
        addValueVariable(permission, selfType, oclParser, caretOffset);
        addTargetVariable(permission, selfType, oclParser, caretOffset);           
        
        // 4) Create iterator and let visible variables
        addVisibleExpVariables(document, caretOffset, oclParser);
    }

    private UmlClass addSelfVariable(StmPermission permission, OclParser oclParser, int caretOffset) throws STMAutocompletionException {
        // 1) Check the actions constrained do not include a create action
        Set<StmAuthorizationConstraint> authorizationConstraints = permission.getAuthorizationConstraints();
        StmAuthorizationConstraint authorizationConstraint = null;
        for (StmAuthorizationConstraint stmAuthorizationConstraint : authorizationConstraints) {
            int start = stmAuthorizationConstraint.getStartPosition().getOffset();
            int end = stmAuthorizationConstraint.getEndPosition().getOffset();
            if(caretOffset >= start && caretOffset <= end) {
                authorizationConstraint = stmAuthorizationConstraint;
                break;
            }
        }
        assert authorizationConstraint != null;
        Set<StmAction> constrainedActions = authorizationConstraint.getConstrainedActions();
        for (StmAction stmAction : constrainedActions) {
            KindOfAction kindOfAction = stmAction.getKindOfAction();
            if(kindOfAction == KindOfAction.CREATE) {
                return null;
            }
        }
        
        // 2) Get the type of the self variable and create the variable
        String name = permission.getIdDeclaration().getId();      
        Element element = oclParser.getEnv().lookup(name);
        if (element == null) {
            throw new STMAutocompletionException("The entity '" + name + "' does not exist in the current data model.");
        }        
        if(!(element instanceof UmlClass)) {
            throw new STMAutocompletionException("The entity '" + name + "' does not exist in the current data model.");
        }
        
        UmlClass selfType = (UmlClass)element;
        Variable selfVariable = new Variable(SELF);
        selfVariable.setType(selfType);
        StaticEnvironment env = oclParser.getEnv();
        env.addElement(selfVariable, false);
        return selfType;
    }

    /**
     * This method creates the 'value' variable when an action associated to the the ac, is an atomic update action
     * @param permission
     * @param entity
     * @param oclParser
     * @param caretOffset 
     */
    private void addValueVariable(StmPermission permission, UmlClass entity, OclParser oclParser, int caretOffset) {
        if(entity == null) {
            return;
        }
        
        StaticEnvironment env = oclParser.getEnv();
        Classifier valueType = null;
        Set<StmAuthorizationConstraint> authorizationConstraints = permission.getAuthorizationConstraints();
        StmAuthorizationConstraint authorizationConstraint = null;
        for (StmAuthorizationConstraint stmAuthorizationConstraint : authorizationConstraints) {
            int start = stmAuthorizationConstraint.getStartPosition().getOffset();
            int end = stmAuthorizationConstraint.getEndPosition().getOffset();
            if(caretOffset >= start && caretOffset <= end) {
                authorizationConstraint = stmAuthorizationConstraint;
                break;
            }
        }
        assert authorizationConstraint != null;
        
        Set<StmAction> constrainedActions = authorizationConstraint.getConstrainedActions();
        for (StmAction stmAction : constrainedActions) {
            KindOfAction kindOfAction = stmAction.getKindOfAction();
            if(kindOfAction != KindOfAction.UPDATE) {
                continue;
            }
            
            ElementIdDeclaration resource = stmAction.getResource();
            if(resource == null) {
                continue;
            }
            
            String resourceName = resource.getId();
            Property property = null;
            Iterable<Attribute> allAttributes = entity.getAllAttributes();
            for (Attribute attr : allAttributes) {
                if(resourceName.equals(attr.getName())) {
                    property = attr;
                    break;
                }
            }
            if(property == null) {
                Iterable<AssociationEnd> allAssociationEnds = entity.getAllAssociationEnds();
                for (AssociationEnd assocEnd : allAssociationEnds) {
                    if(resourceName.equals(assocEnd.getName())) {
                        property = assocEnd;
                        break;
                    }
                }
            }           
            if(property == null) {
                continue;
            }     
            
            // if value type is not yet defined, then set the type
            if(valueType == null) {
                valueType = property.getType();
            }
            // if value type is already defined, check that they are the same
            else {
                if(!valueType.classifierEquals(property.getType())) {
                    valueType = null;
                    break;
                } 
            }
        }
        if(valueType == null) {
            return;
        }
        
        Variable valueVariable = new Variable(VALUE);
        valueVariable.setType(valueType);
        env.addElement(valueVariable, false);
    }
    
    /**
     * This method creates the 'target' variable when an action associated to the the ac, is an add or remove action
     * @param permission
     * @param entity
     * @param oclParser
     * @param caretOffset 
     */ 
    private void addTargetVariable(StmPermission permission, UmlClass entity, OclParser oclParser, int caretOffset) {
        if (entity == null) {
            return;
        }
        
        StaticEnvironment env = oclParser.getEnv();
        Classifier targetType = null;
        Set<StmAuthorizationConstraint> authorizationConstraints = permission.getAuthorizationConstraints();
        StmAuthorizationConstraint authorizationConstraint = null;
        for (StmAuthorizationConstraint stmAuthorizationConstraint : authorizationConstraints) {
            int start = stmAuthorizationConstraint.getStartPosition().getOffset();
            int end = stmAuthorizationConstraint.getEndPosition().getOffset();
            if(caretOffset >= start && caretOffset <= end) {
                authorizationConstraint = stmAuthorizationConstraint;
                break;
            }
        }
        assert authorizationConstraint != null;
        
        Set<StmAction> constrainedActions = authorizationConstraint.getConstrainedActions();
        for (StmAction stmAction : constrainedActions) {
            KindOfAction kindOfAction = stmAction.getKindOfAction();
            if(kindOfAction != KindOfAction.ADD || kindOfAction != KindOfAction.REMOVE) {
                continue;
            }
            
            ElementIdDeclaration resource = stmAction.getResource();
            if(resource == null) {
                continue;
            }
            
            String resourceName = resource.getId();
            Iterable<AssociationEnd> allAssociationEnds = entity.getAllAssociationEnds();
            AssociationEnd associationEnd = null;
            for (AssociationEnd assocEnd : allAssociationEnds) {
                if(resourceName.equals(assocEnd.getName())) {
                    associationEnd = assocEnd;
                    break;
                }
            }
            if(associationEnd == null) {
                continue;
            }
            
            if(targetType == null) {
                targetType = associationEnd.getReferredType();
            }
            else {
                if(!targetType.classifierEquals(associationEnd.getReferredType())) {
                    targetType = null;
                    break;
                }
            }
        }
        if(targetType == null) {
            return;
        }
        
        Variable targetVariable = new Variable(TARGET);
        targetVariable.setType(targetType);
        env.addElement(targetVariable, false);
    }
    
    /* This method creates the 'value' variable when the resource of an associated action to the ac, is an attribute
    private void addValueVariable(StmPermission permission, UmlClass entity, OclParser oclParser, int caretOffset) {
        StaticEnvironment env = oclParser.getEnv();
        Classifier valueType = null;
        Set<StmAction> actions = permission.getActions();
        for (StmAction stmAction : actions) {
            ElementIdDeclaration resource = stmAction.getResource();
            if(resource == null) {
                continue;
            }
            
            StmAuthorizationConstraint authorizationConstraint = stmAction.getAuthorizationConstraint();
            int start = authorizationConstraint.getStartPosition().getOffset();
            int end = authorizationConstraint.getEndPosition().getOffset();
            if(caretOffset <= start || caretOffset >= end) {
                continue;
            }
            
            String resourceName = resource.getId();
            Iterable<Attribute> allAttributes = entity.getAllAttributes();
            Attribute attribute = null;
            for (Attribute attr : allAttributes) {
                if(resourceName.equals(attr.getName())) {
                    attribute = attr;
                    break;
                }
            }
            if(attribute == null) {
                continue;
            }
            
            if(valueType == null) {
                valueType = attribute.getType();
            }
            else {
                valueType = null;
                break;
            }
        }
        if(valueType == null) {
            return;
        }
        
        Variable valueVariable = new Variable(VALUE);
        valueVariable.setType(valueType);
        env.addElement(valueVariable, false);
    }
    */
    
    /* This method creates the 'target' variable when the resource of an associated action to the ac, is an association-end
    private void addTargetVariable(StmPermission permission, UmlClass entity, OclParser oclParser, int caretOffset) {
        StaticEnvironment env = oclParser.getEnv();
        Classifier targetType = null;
        Set<StmAction> actions = permission.getActions();
        for (StmAction stmAction : actions) {
            ElementIdDeclaration resource = stmAction.getResource();
            if(resource == null) {
                continue;
            }
            
            StmAuthorizationConstraint authorizationConstraint = stmAction.getAuthorizationConstraint();
            int start = authorizationConstraint.getStartPosition().getOffset();
            int end = authorizationConstraint.getEndPosition().getOffset();
            if(caretOffset <= start || caretOffset >= end) {
                continue;
            }
            
            String resourceName = resource.getId();
            Iterable<AssociationEnd> allAssociationEnds = entity.getAllAssociationEnds();
            AssociationEnd associationEnd = null;
            for (AssociationEnd assocEnd : allAssociationEnds) {
                if(resourceName.equals(assocEnd.getName())) {
                    associationEnd = assocEnd;
                    break;
                }
            }
            if(associationEnd == null) {
                continue;
            }
            
            if(targetType == null) {
                KindOfAction kindOfAction = stmAction.getKindOfAction();
                switch(kindOfAction) {
                    case READ:
                    case UPDATE:{
                        targetType = associationEnd.getType();
                        break;
                    }
                    case ADD:
                    case REMOVE:{
                        targetType = associationEnd.getReferredType();
                        break;
                    }
                }
            }
            else {
                targetType = null;
                break;
            }
        }
        if(targetType == null) {
            return;
        }
        
        Variable targetVariable = new Variable(TARGET);
        targetVariable.setType(targetType);
        env.addElement(targetVariable, false);
    }
    */
    
    private Stm parseDocument(Document document) throws STMAutocompletionException {
        // get URI from activated stm document
        DataObject dob = TopComponent.getRegistry().getActivated().getLookup().lookup(DataObject.class);
        FileObject fob = dob.getPrimaryFile();
        URI uri = fob.toURI();
        
        // get the text
        String text;
        try {
            // get the input stream
            text = document.getText(0, document.getLength());
        } 
        catch (BadLocationException ex) {
            throw new STMAutocompletionException(ex.getMessage());
        }
        
        // parse the document
        StmParserRequest stmParserRequest = new StmParserRequest(uri,text);
        StmParser stmParser = new StmParser(stmParserRequest);
        SourceTaskResult<Stm, Stm> stmParserResult;
        try {
            stmParserResult = stmParser.call();
        } 
        catch (IOException ex) {
            throw new STMAutocompletionException(ex.getMessage());
        }

        // If there are errors --> return an exception witht he first error information.
        Collection<SourceError<Stm>> stmParserErrors = stmParserResult.getErrors();
        if (!stmParserErrors.isEmpty()) {
            SourceError<Stm> sourceError = stmParserErrors.iterator().next();           
            StringBuilder sb = new StringBuilder("Error parsing the security model at ");
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
            throw new STMAutocompletionException(sb.toString());
        }
        
        Stm stm = stmParserResult.getOutput();
        return stm;
    }
    
    /**
     * This method obtains the visible variables (iterator variables) from the beginning 
     * of the OCL expression to the caret offset, and add them to the parser environment.
     * @param document
     * @param caretOffset
     * @param parser 
     */
    private void addVisibleExpVariables(Document document, int caretOffset, OclParser parser) {        
        StaticEnvironment env = parser.getEnv();
        
        // 1) Get the expression from the beginning to the caret position
        String expr;
        try {
            expr = STMOCLCompletionUtils.getTextFromCaretToStartSymbol(document, caretOffset).toString();
        }
        catch (BadLocationException ex) {
            return;
        }
        
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
    
    private class PartialOCLWalker extends OclWalker {

        public PartialOCLWalker(StaticEnvironment env) {
            super(env);
            this.concreteNodeToAbstractNode = new ConcreteToAbstractMap();
        }
        
        public Object getResult(Node node) {
            return this.concreteNodeToAbstractNode.get(node);
        }
    }
}
