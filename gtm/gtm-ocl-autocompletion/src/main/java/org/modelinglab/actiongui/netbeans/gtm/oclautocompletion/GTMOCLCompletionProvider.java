/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.gtm.oclautocompletion;

import java.net.URI;
import java.util.Collection;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.modelinglab.actiongui.netbeans.autocompletion.ocl.OCLCompletionItemsProvider;
import org.modelinglab.actiongui.netbeans.autocompletion.ocl.completionitems.OCLCompletionItem;
import org.modelinglab.actiongui.netbeans.gtm.oclautocompletion.exceptions.GTMOCLAutocompletionException;
import org.modelinglab.actiongui.netbeans.gtm.oclautocompletion.utils.GTMOCLAutocompletionUtils;
import org.modelinglab.ocl.parser.OclParser;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.TopComponent;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
@MimeRegistration(mimeType = "text/x-gtm", service = CompletionProvider.class)
public class GTMOCLCompletionProvider implements CompletionProvider{
    private final InputOutput io = IOProvider.getDefault().getIO("GTM auto-completion", false);

    @Override
    public CompletionTask createTask(int queryType, final JTextComponent jtc) {
        if(queryType != CompletionProvider.COMPLETION_QUERY_TYPE) {
            return null;
        }
        return new AsyncCompletionTask(new AsyncCompletionQuery() {

            @Override
            protected void query(CompletionResultSet completionResultSet, Document document, int caretOffset) {                 
                
                // 1) Check the cursor is in a valid position, hidding the errors because of the other
                // auto-completion providers for the same language
                int caretPosition;
                try {
                    caretPosition = GTMOCLAutocompletionUtils.getCaretPosition(document, caretOffset);
                } 
                catch (BadLocationException ex) {
                    //String errorMessage = "OCL auto-completion is disabled: " + ex.getMessage();
                    //OCLErrorCompletionItem item = new OCLErrorCompletionItem(null, caretOffset, errorMessage);
                    //completionResultSet.addItem(item);
                    
                    //String errorMessage = "OCL auto-completion is disabled: " + ex.getMessage();
                    //printError(errorMessage);
                    
                    completionResultSet.finish();
                    return;
                }
                // if position is not within square brakets --> finish
                if (caretPosition == 0 || caretPosition == 1) {
                    //String errorMessage = "OCL auto-completion is disabled: the caret position must be within square brackets '[' and ']'.";
                    //OCLErrorCompletionItem item = new OCLErrorCompletionItem(null, caretOffset, errorMessage);
                    //completionResultSet.addItem(item);
                    
                    //String errorMessage = "OCL auto-completion is disabled: the caret position must be within square brackets '[' and ']'";
                    //printError(errorMessage);
                    completionResultSet.finish();
                    return;
                }   

                // 2) Get the data model URI
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
                    OCLErrorCompletionItem item = new OCLErrorCompletionItem(null, caretOffset, errorMessage);
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
                    OCLErrorCompletionItem item = new OCLErrorCompletionItem(null, caretOffset, errorMessage);
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
                    OCLErrorCompletionItem item = new OCLErrorCompletionItem(null, caretOffset, errorMessage);
                    completionResultSet.addItem(item);
                    */
                    String errorMessage = "Error getting the application data model";
                    printError(errorMessage);
                    completionResultSet.finish();
                    return;
                }
                URI datamodelURI = datamodelFO.toURI();
                
                // 3) Get the GUI model URI
                DataObject dob = TopComponent.getRegistry().getActivated().getLookup().lookup(DataObject.class);
                FileObject fob = dob.getPrimaryFile();
                URI guimodelURI = fob.toURI();

                // 4) Obtain the OCL parser
                GTMOCLParserProvider parserProvider = GTMOCLParserProvider.getInstance();
                OclParser parser;
                try {
                    parser = parserProvider.getParser(datamodelURI, guimodelURI, document, caretOffset);
                } 
                catch (GTMOCLAutocompletionException ex) {
                    /*
                    OCLErrorCompletionItem item = new OCLErrorCompletionItem(null, caretOffset, ex.getMessage());
                    completionResultSet.addItem(item);
                    */
                    printError(ex.getMessage());
                    completionResultSet.finish();
                    return;
                }

                // 5) Get the collection of available items for autocompletion
                StringBuilder expr = new StringBuilder(parserProvider.getModifiedExpr());
                OCLCompletionItemsProvider itemsProvider = new OCLCompletionItemsProvider();
                Collection<OCLCompletionItem> completionItems = itemsProvider.buildOCLCompletionItems(expr, parser, caretOffset);
                completionResultSet.addAllItems(completionItems);
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
    
    private void printError(String errorMessage) {
        io.select();
        io.getErr().println (errorMessage);  //this text should appear in red
        io.getErr().close();
    }
}
        
    