/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.gtm.autocompletion;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.modelinglab.actiongui.maven.tools.AGMavenInterface;
import org.modelinglab.actiongui.maven.tools.AGMavenInterfaceFactory;
import org.modelinglab.actiongui.mm.gtm.Gtm;
import org.modelinglab.actiongui.mm.gtm.StandardGtm;
import org.modelinglab.actiongui.mm.gtm.node.Node;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements.GTMStatementCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.variables.GTMTemporalVariableCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.variables.GTMWidgetVariableCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.exceptions.GTMAutocompletionException;
import org.modelinglab.actiongui.netbeans.gtm.autocompletion.utils.GTMAutocompletionUtils;
import org.modelinglab.actiongui.tasks.gtmanalyzer.analysis.utils.UtilsGtmA;
import org.modelinglab.actiongui.tasks.gtmmerge.GtmMerge;
import org.modelinglab.actiongui.tasks.gtmmerge.GtmMergeRequest;
import org.modelinglab.actiongui.tasks.gtmparser.GtmParser;
import org.modelinglab.actiongui.tasks.gtmparser.GtmParserRequest;
import org.modelinglab.mm.source.SourceError;
import org.modelinglab.mm.source.SourceTaskResult;
import org.modelinglab.ocl.core.ast.Namespace;
import org.modelinglab.ocl.core.ast.types.Classifier;
import org.modelinglab.ocl.parser.OclParser;
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
@MimeRegistration(mimeType = "text/x-gtm", service = CompletionProvider.class)
public class GTMCompletionProvider implements CompletionProvider{
    private final GTMAutocompletionUtils utils = GTMAutocompletionUtils.getInstance();
    private final InputOutput io = IOProvider.getDefault().getIO("GTM auto-completion", false);

    @Override
    public CompletionTask createTask(int queryType, final JTextComponent jtc) {
        if(queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return null;
        }
        return new AsyncCompletionTask(new AsyncCompletionQuery() {

            @Override
            protected void query(CompletionResultSet completionResultSet, Document document, int caretOffset) {                 
                // 1) Get the data model URI
                Project p = TopComponent.getRegistry().getActivated().getLookup().lookup(Project.class);
                if (p == null) {
                    DataObject dob = TopComponent.getRegistry().getActivated().getLookup().lookup(DataObject.class);
                    if (dob != null) {
                        FileObject fo = dob.getPrimaryFile();
                        p = FileOwnerQuery.getOwner(fo);
                    }
                }
                if(p == null) {
                    return;
                }
                FileObject projectDirectory = p.getProjectDirectory();
                if(projectDirectory == null){
                    /*
                    String errorMessage = "Error getting the current project";
                    GTMErrorCompletionItem item = new GTMErrorCompletionItem(null, caretOffset, errorMessage);
                    completionResultSet.addItem(item);
                    */
                    String errorMessage = "Error getting the current project";
                    printError(errorMessage);
                    completionResultSet.finish();
                    return;
                }
                FileObject parent = projectDirectory.getParent();
                if(parent == null) {
                    /*
                    String errorMessage = "Error getting the parent project of the current project";
                    GTMErrorCompletionItem item = new GTMErrorCompletionItem(null, caretOffset, errorMessage);
                    completionResultSet.addItem(item);
                    */
                    String errorMessage = "Error getting the parent project of the current project";
                    printError(errorMessage);
                    completionResultSet.finish();
                    return;
                }
                FileObject datamodelFO = parent.getFileObject("dtm/target/classes/umlclasses.xml");
                if(datamodelFO == null) {
                    /*
                    String errorMessage = "Error getting the application data model";
                    GTMErrorCompletionItem item = new GTMErrorCompletionItem(null, caretOffset, errorMessage);
                    completionResultSet.addItem(item);
                    */
                    String errorMessage = "Error getting the application data model";
                    printError(errorMessage);
                    completionResultSet.finish();
                    return;
                }
                URI datamodelURI = datamodelFO.toURI();
                
                // 2) Load the data model
                AGMavenInterface agmi = AGMavenInterfaceFactory.getDefaultInterface();  
                Namespace namespace;
                try {
                    namespace = agmi.unserializeNamespace(Utilities.toFile(datamodelURI));
                } 
                catch (AGMavenInterface.AGMavenInterfaceException ex) {
                    /*
                    String errorMessage = "Error loading the application data model: " + ex.getMessage();
                    GTMErrorCompletionItem item = new GTMErrorCompletionItem(null, caretOffset, errorMessage);
                    completionResultSet.addItem(item);
                    */
                    String errorMessage = "Error loading the application data model: " + ex.getMessage();
                    printError(errorMessage);
                    completionResultSet.finish();
                    return;
                }

                // 3) Get the gtm model URI
                DataObject dob = TopComponent.getRegistry().getActivated().getLookup().lookup(DataObject.class);
                FileObject fob = dob.getPrimaryFile();
                URI guimodelURI = fob.toURI();
                
                // 4) Build the gtm model, hidding errors
                StandardGtm standardGtm;
                try {
                    standardGtm = buildGUIMLModel(guimodelURI, document, caretOffset, namespace);
                } 
                catch (GTMAutocompletionException ex) {
                    /*
                    String errorMessage = "Error building the GUIML model: " + ex.getMessage();
                    GTMErrorCompletionItem item = new GTMErrorCompletionItem(null, caretOffset, errorMessage);
                    completionResultSet.addItem(item);
                    */
                    String errorMessage = "Error building the GUIML model: " + ex.getMessage();
                    printError(errorMessage);
                    completionResultSet.finish();
                    return;
                }
                
                // 5) Get the auto-completion cases taking into account the caret position
                Collection<GTMAutocompletionCase> autocompletionCases = utils.getAutocompletionCases(standardGtm, caretOffset);
                for (GTMAutocompletionCase autocompletionCase : autocompletionCases) {
                    Collection<CompletionItem> completionItems;
                    switch(autocompletionCase) {
                        case VARIABLES: {
                            try {
                                completionItems = buildVariableItems(namespace, standardGtm, document, caretOffset);
                                completionResultSet.addAllItems(completionItems);
                            } 
                            catch (GTMAutocompletionException ex) {
                                /*
                                String errorMessage = "Error building the completion items for variables: " + ex.getMessage();
                                GTMErrorCompletionItem item = new GTMErrorCompletionItem(null, caretOffset, errorMessage);
                                completionResultSet.addItem(item);
                                */
                                String errorMessage = "Error building the completion items for variables: " + ex.getMessage();
                                printError(errorMessage);
                            }
                            break;
                        }
                        case ACTIONS: {
                            try {
                                completionItems = buildActionItems(document, caretOffset);
                                completionResultSet.addAllItems(completionItems);
                            } 
                            catch (GTMAutocompletionException ex) {
                                /*
                                String errorMessage = "Error building the completion items for actions: " + ex.getMessage();
                                GTMErrorCompletionItem item = new GTMErrorCompletionItem(null, caretOffset, errorMessage);
                                completionResultSet.addItem(item);
                                */
                                String errorMessage = "Error building the completion items for actions: " + ex.getMessage();
                                printError(errorMessage);
                            }
                            break;
                        }
                    }
                }
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
    
    private StandardGtm buildGUIMLModel(URI guiModelURI, Document document, int caretOffset, Namespace namespace) throws GTMAutocompletionException {
        // 1) Get the text to parse
        String textToParse;
        try {
            textToParse = utils.textToParse(document, caretOffset);
        } 
        catch (BadLocationException ex) {
            throw new GTMAutocompletionException(ex.getMessage());
        }
        
        // 2) Parse the text
        GtmParserRequest request = new GtmParserRequest(guiModelURI, textToParse);
        GtmParser parser = new GtmParser(request);
        SourceTaskResult<Gtm, Gtm> result;
        try {
            result = parser.call();
        } 
        catch (IOException ex) {
            throw new GTMAutocompletionException(ex.getMessage());
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
            throw new GTMAutocompletionException(sb.toString());
        }
        
        // 3) Buld the GUIML model
        Gtm gtm = result.getOutput();
        List<Gtm> gtms = new ArrayList<>();
        gtms.add(gtm);
        GtmMergeRequest mergeRequest = new GtmMergeRequest(gtms, namespace);
        GtmMerge merge = new GtmMerge(mergeRequest);
        SourceTaskResult<Gtm, StandardGtm> mergeResult = merge.call();
        Collection<SourceError<Gtm>> mergeErrors = mergeResult.getErrors();
        // If there are errors --> return an exception with he first error information.
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
            throw new GTMAutocompletionException(sb.toString());
        }
        StandardGtm standardGtm = mergeResult.getOutput();
        return standardGtm;
    }
    
    private Collection<CompletionItem> buildVariableItems(Namespace namespace, StandardGtm standardGtm, Document document, int caretOffset) throws GTMAutocompletionException {
        Collection<CompletionItem> items = new ArrayList<>();
        
        // 1) Calculate the accumulator
        StringBuilder accumulator = new StringBuilder();
        StringBuilder sb;
        try {
            String textBefore = document.getText(document.getStartPosition().getOffset(), caretOffset);
            sb = new StringBuilder(textBefore);
        } 
        catch (BadLocationException ex) {
            throw new GTMAutocompletionException("Error getting the text before the cursor.");
        }
        while(sb.length() > 0) {
            char charAt = sb.charAt(sb.length()-1);
            if(Character.isLetterOrDigit(charAt) || charAt == '.' || charAt == '_') {
                accumulator.append(charAt);
            }
            else {
                break;
            }
            sb.deleteCharAt(sb.length()-1);
        }
        accumulator.reverse();
        
        // 2) Generate the variable items
        OclParser oclParser = UtilsGtmA.buildParser(namespace);
        Map<String, Classifier> widgetVariables = UtilsGtmA.collectVariables(standardGtm, namespace);
        Map<Node, Map<String, Classifier>> temporalVariables = UtilsGtmA.collectTemporalVariables(standardGtm, namespace, widgetVariables, oclParser);
        Map<String, Classifier> visibleTemporalVariables = utils.getVisibleTemporalVariables(temporalVariables, standardGtm, caretOffset);
        String widgetGlobalId = utils.getCurrentWidgetGlobalId(standardGtm, caretOffset);
        String prefix = accumulator.toString();
        Set<String> wigetVars = widgetVariables.keySet();
        // 2.1) Generate visible widget variables
        for (String globalId : wigetVars) {
            if(globalId.contains(prefix)) {
                GTMWidgetVariableCompletionItem item = new GTMWidgetVariableCompletionItem(prefix, caretOffset, globalId, widgetGlobalId, widgetVariables.get(globalId), wigetVars);
                items.add(item);
            }
        }
        // 2.2) Generate visible temporal variables
        for (String tempVar : visibleTemporalVariables.keySet()) {
            if(tempVar.startsWith(prefix)) {
                GTMTemporalVariableCompletionItem item = new GTMTemporalVariableCompletionItem(prefix, caretOffset, tempVar, visibleTemporalVariables.get(tempVar));
                items.add(item);
            }
        }
        
        return items;
    }

    private Collection<CompletionItem> buildActionItems(Document document, int caretOffset) throws GTMAutocompletionException{
        // 1) Calculate the accumulator
        StringBuilder accumulator = new StringBuilder();
        StringBuilder sb;
        try {
            String textBefore = document.getText(document.getStartPosition().getOffset(), caretOffset);
            sb = new StringBuilder(textBefore);
        } 
        catch (BadLocationException ex) {
            throw new GTMAutocompletionException("Error getting the text before the cursor.");
        }
        while(sb.length() > 0) {
            char charAt = sb.charAt(sb.length()-1);
            if(GTMStatementCompletionItem.isValidStatementChar(charAt)) {
                accumulator.append(charAt);
            }
            else {
                break;
            }
            sb.deleteCharAt(sb.length()-1);
        }
        accumulator.reverse();
        
        return utils.getAllStatements(accumulator.toString(), caretOffset);
    }
    
    private void printError(String errorMessage) {
        io.select();
        io.getErr().println (errorMessage);  //this text should appear in red
        io.getErr().close();
    }
}
        
    