/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.stm.oclautocompletion;

import com.meaningfulmodels.actiongui.vm.core.IsUserAnnotation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
import org.modelinglab.mm.source.SourceElement;
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
import org.modelinglab.ocl.core.ast.expressions.Variable;
import org.modelinglab.ocl.core.ast.types.Classifier;
import org.modelinglab.ocl.parser.OclParser;
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
        Stm stm;
        try {
            stm = parseDocument();
        } 
        catch (STMAutocompletionException ex) {
            throw new STMAutocompletionException("Error parsing the current security file:" + ex.getMessage());
        }
        
        // 3) Get the ocl parser
        Date newModified = datamodelFO.lastModified();
        if(lastModificationDates.containsKey(nameApp)) {
            Date lastModified = lastModificationDates.get(nameApp);
            if(lastModified.before(newModified)) {
                lastModificationDates.put(nameApp, newModified);
                oclParser = createParser(datamodelFO, stm, caretOffset);
                parsers.put(nameApp, oclParser);
            }
            else {
                oclParser = parsers.get(nameApp);
                updateParser(oclParser, stm, caretOffset);
            }
        }
        else {
            oclParser = createParser(datamodelFO, stm, caretOffset);
            lastModificationDates.put(nameApp, newModified);            
            parsers.put(nameApp, oclParser);
        }
        return oclParser;
    }

    private OclParser createParser(FileObject datamodelFO, Stm stm, int caretOffset) throws STMAutocompletionException {
        OclParser oclParser = new OclParser();
        
        // 1) Load namespace
        AGMavenInterface agmi = AGMavenInterfaceFactory.getDefaultInterface();
        Namespace namespace;
        try {
            namespace = agmi.unserializeNamespace(datamodelFO.getInputStream());
        } 
        catch (FileNotFoundException ex) {
            throw new STMAutocompletionException(ex.getMessage());
        } 
        catch (AGMavenInterface.AGMavenInterfaceException ex) {
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
        env.addScope();
        
        // 3) Set the built environment to the parser
        oclParser.setEnv(env);
        
        // 4) Call update parser to update the rest of possible variables ('self', 'value', and 'target')
        updateParser(oclParser, stm, caretOffset);
        
        // 5) Return the parser
        return oclParser;
    }

    /**
     * This method loads in the ocl parser environment, the possible variables that can be used in the ocl expression,
     * depending on the permission and actions associated to the ocl expression
     * @param oclParser
     * @param stm
     * @param caretOffset 
     */
    private void updateParser(OclParser oclParser, Stm stm, int caretOffset) throws STMAutocompletionException {
        // 1) Remove the last scope of the environment, since it is dedicated for old variables 'self', 'value', and 'target'
        StaticEnvironment env = oclParser.getEnv();
        env.removeScope();
        
        // 2) Add a new scope for new variables 'self', 'value', and 'target'
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
        UmlClass selfType = addSelfVariable(permission, oclParser);
        addValueVariable(permission, selfType, oclParser, caretOffset);
        addTargetVariable(permission, selfType, oclParser, caretOffset);    
    }

    private UmlClass addSelfVariable(StmPermission permission, OclParser oclParser) throws STMAutocompletionException {
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
        StaticEnvironment env = oclParser.getEnv();
        Classifier valueType = null;
        Set<StmAction> actions = permission.getActions();
        for (StmAction stmAction : actions) {
            
            KindOfAction kindOfAction = stmAction.getKindOfAction();
            if(kindOfAction != KindOfAction.UPDATE) {
                continue;
            }
            
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
        StaticEnvironment env = oclParser.getEnv();
        Classifier targetType = null;
        Set<StmAction> actions = permission.getActions();
        for (StmAction stmAction : actions) {
            
            KindOfAction kindOfAction = stmAction.getKindOfAction();
            if(kindOfAction != KindOfAction.ADD || kindOfAction != KindOfAction.REMOVE) {
                continue;
            }
            
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
    
    private Stm parseDocument() throws STMAutocompletionException {
        // get URI from activated stm document
        DataObject dob = TopComponent.getRegistry().getActivated().getLookup().lookup(DataObject.class);
        FileObject fob = dob.getPrimaryFile();
        URI uri = fob.toURI();
        
        // parse the document
        StmParserRequest stmParserRequest = new StmParserRequest(uri);
        StmParser stmParser = new StmParser(stmParserRequest);
        SourceTaskResult<Stm, Stm> stmParserResult;
        try {
            stmParserResult = stmParser.call();
        } 
        catch (IOException ex) {
            throw new STMAutocompletionException(ex.getMessage());
        }

        Collection<SourceError<Stm>> stmParserErrors = stmParserResult.getErrors();
        if (!stmParserErrors.isEmpty()) {
            throw new STMAutocompletionException("There are errors parsing the security model file '" + uri.toString() + "'");
        }

        Stm stm = stmParserResult.getOutput();
        return stm;
    }
}
