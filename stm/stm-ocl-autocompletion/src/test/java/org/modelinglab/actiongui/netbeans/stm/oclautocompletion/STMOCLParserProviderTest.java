/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.stm.oclautocompletion;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import javax.swing.text.AbstractDocument.Content;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.GapContent;
import javax.swing.text.PlainDocument;
import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.modelinglab.ocl.core.ast.Element;
import org.modelinglab.ocl.parser.OclParser;
import org.modelinglab.utils.doc.MemoryLineBasedDocTool;
import org.openide.util.Utilities;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public class STMOCLParserProviderTest {
    
    public STMOCLParserProviderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getInstance method, of class STMOCLParserProvider.
     */
    @Test
    public void testGetInstance() {
        STMOCLParserProvider instance = STMOCLParserProvider.getInstance();
        assertTrue(instance != null);
    }

    /**
     * Test of getParser method, of class STMOCLParserProvider.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetParser() throws Exception {
        System.out.println("getParser");
        URL dataModelURL = getClass().getResource("umlclasses.xml");
        URI dataModelURI = dataModelURL.toURI();
        URL securityModelURL = getClass().getResource("Default.stm");
        URI securityModelURI = securityModelURL.toURI();
        Document securityModelDocument = createDocument(securityModelURI);
        STMOCLParserProvider instance = STMOCLParserProvider.getInstance();
        
        // 1) TESTING CALLER, SELF, VALUE & TARGET VARIABLES
        // check constraint 1
        int line = 13;
        int column = 31;
        int caretOffset = calculateOffset(securityModelURI, line, column);
        OclParser oclParser = instance.getParser(dataModelURI, securityModelURI, securityModelDocument, caretOffset);
        Element caller = oclParser.getEnv().lookup("caller");
        Element self = oclParser.getEnv().lookup("self");
        Element value = oclParser.getEnv().lookup("value");
        Element target = oclParser.getEnv().lookup("target");
        assertTrue(caller != null && self == null && value == null && target == null);
        
        // check constraint 2
        line = 14;
        column = 29;
        caretOffset = calculateOffset(securityModelURI, line, column);
        oclParser = instance.getParser(dataModelURI, securityModelURI, securityModelDocument, caretOffset);
        caller = oclParser.getEnv().lookup("caller");
        self = oclParser.getEnv().lookup("self");
        value = oclParser.getEnv().lookup("value");
        target = oclParser.getEnv().lookup("target");
        assertTrue(caller != null && self != null && value == null && target == null);
        
        // check constraint 3
        line = 15;
        column = 31;
        caretOffset = calculateOffset(securityModelURI, line, column);
        oclParser = instance.getParser(dataModelURI, securityModelURI, securityModelDocument, caretOffset);
        caller = oclParser.getEnv().lookup("caller");
        self = oclParser.getEnv().lookup("self");
        value = oclParser.getEnv().lookup("value");
        target = oclParser.getEnv().lookup("target");
        assertTrue(caller != null && self != null && value == null && target == null);
        
        // check constraint 4
        line = 16;
        column = 31;
        caretOffset = calculateOffset(securityModelURI, line, column);
        oclParser = instance.getParser(dataModelURI, securityModelURI, securityModelDocument, caretOffset);
        caller = oclParser.getEnv().lookup("caller");
        self = oclParser.getEnv().lookup("self");
        value = oclParser.getEnv().lookup("value");
        target = oclParser.getEnv().lookup("target");
        assertTrue(caller != null && self != null && value == null && target == null);
        
        // check constraint 5
        line = 18;
        column = 51;
        caretOffset = calculateOffset(securityModelURI, line, column);
        oclParser = instance.getParser(dataModelURI, securityModelURI, securityModelDocument, caretOffset);
        caller = oclParser.getEnv().lookup("caller");
        self = oclParser.getEnv().lookup("self");
        value = oclParser.getEnv().lookup("value");
        target = oclParser.getEnv().lookup("target");
        assertTrue(caller != null && self != null && value != null && target == null);
        
        // check constraint 6
        line = 19;
        column = 51;
        caretOffset = calculateOffset(securityModelURI, line, column);
        oclParser = instance.getParser(dataModelURI, securityModelURI, securityModelDocument, caretOffset);
        caller = oclParser.getEnv().lookup("caller");
        self = oclParser.getEnv().lookup("self");
        value = oclParser.getEnv().lookup("value");
        target = oclParser.getEnv().lookup("target");
        assertTrue(caller != null && self != null && value == null && target == null);
        
        // check constraint 7
        line = 20;
        column = 37;
        caretOffset = calculateOffset(securityModelURI, line, column);
        oclParser = instance.getParser(dataModelURI, securityModelURI, securityModelDocument, caretOffset);
        caller = oclParser.getEnv().lookup("caller");
        self = oclParser.getEnv().lookup("self");
        value = oclParser.getEnv().lookup("value");
        target = oclParser.getEnv().lookup("target");
        assertTrue(caller != null && self != null && value == null && target != null);
        
        // check constraint 8
        line = 21;
        column = 54;
        caretOffset = calculateOffset(securityModelURI, line, column);
        oclParser = instance.getParser(dataModelURI, securityModelURI, securityModelDocument, caretOffset);
        caller = oclParser.getEnv().lookup("caller");
        self = oclParser.getEnv().lookup("self");
        value = oclParser.getEnv().lookup("value");
        target = oclParser.getEnv().lookup("target");
        assertTrue(caller != null && self != null && value == null && target != null);
        
        // check constraint 9
        line = 22;
        column = 58;
        caretOffset = calculateOffset(securityModelURI, line, column);
        oclParser = instance.getParser(dataModelURI, securityModelURI, securityModelDocument, caretOffset);
        caller = oclParser.getEnv().lookup("caller");
        self = oclParser.getEnv().lookup("self");
        value = oclParser.getEnv().lookup("value");
        target = oclParser.getEnv().lookup("target");
        assertTrue(caller != null && self != null && value == null && target == null);
        
        // check constraint 10
        line = 23;
        column = 54;
        caretOffset = calculateOffset(securityModelURI, line, column);
        oclParser = instance.getParser(dataModelURI, securityModelURI, securityModelDocument, caretOffset);
        caller = oclParser.getEnv().lookup("caller");
        self = oclParser.getEnv().lookup("self");
        value = oclParser.getEnv().lookup("value");
        target = oclParser.getEnv().lookup("target");
        assertTrue(caller != null && self != null && value != null && target != null);
        
        // TESTING VISIBLE ITERATOR VARIABLES
        // check constraint 11
        line = 26;
        column = 86;
        caretOffset = calculateOffset(securityModelURI, line, column);
        oclParser = instance.getParser(dataModelURI, securityModelURI, securityModelDocument, caretOffset);
        Element v1 = oclParser.getEnv().lookup("v1");
        Element v2 = oclParser.getEnv().lookup("v2");
        assertTrue(v1 == null && v2 == null);
        
        // check constraint 12
        line = 27;
        column = 150;
        caretOffset = calculateOffset(securityModelURI, line, column);
        oclParser = instance.getParser(dataModelURI, securityModelURI, securityModelDocument, caretOffset);
        v1 = oclParser.getEnv().lookup("v1");
        v2 = oclParser.getEnv().lookup("v2");
        assertTrue(v1 != null && v2 != null);
        
        // check constraint 13
        line = 28;
        column = 151;
        caretOffset = calculateOffset(securityModelURI, line, column);
        oclParser = instance.getParser(dataModelURI, securityModelURI, securityModelDocument, caretOffset);
        v1 = oclParser.getEnv().lookup("v1");
        v2 = oclParser.getEnv().lookup("v2");
        assertTrue(v1 != null && v2 == null);
        
        // check constraint 14
        line = 29;
        column = 153;
        caretOffset = calculateOffset(securityModelURI, line, column);
        oclParser = instance.getParser(dataModelURI, securityModelURI, securityModelDocument, caretOffset);
        v1 = oclParser.getEnv().lookup("v1");
        v2 = oclParser.getEnv().lookup("v2");
        assertTrue(v1 == null && v2 == null);
    }

    private Document createDocument(URI securityModelURI) throws IOException, BadLocationException {
        Content content = new GapContent();
        File file = Utilities.toFile(securityModelURI);        
        String text = FileUtils.fileRead(file);
        content.insertString(0, text);
        Document document = new PlainDocument(content);
        return document;
    }
    
    private int calculateOffset(URI securityModelURI, int line, int column) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Utilities.toFile(securityModelURI)));
        MemoryLineBasedDocTool mlbdt = new MemoryLineBasedDocTool(reader);
        int offset = mlbdt.getOffset(line, column);
        return offset;
    }
}
