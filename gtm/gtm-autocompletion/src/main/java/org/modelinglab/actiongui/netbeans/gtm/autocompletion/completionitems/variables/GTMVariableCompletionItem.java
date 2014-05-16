/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.variables;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Miguel Angel Garcia de Dios <miguelangel.garcia at imdea.org>
 */
public abstract class GTMVariableCompletionItem implements CompletionItem{
    protected final String prefix;
    protected final int caretOffset;
    protected final ImageIcon fieldIcon;
    
    public GTMVariableCompletionItem(String prefix, int caretOffset) {
        this.prefix = prefix;
        this.caretOffset = caretOffset;
        this.fieldIcon = new ImageIcon(ImageUtilities.loadImage("org/modelinglab/actiongui/netbeans/gtm/autocompletion/variable.png"));
    }
    
    @Override
    public void defaultAction(JTextComponent component) {
        try {
            StyledDocument doc = (StyledDocument) component.getDocument();
            //Here we remove the characters starting at the start offset
            //and ending at the point where the caret is currently found:
            doc.remove(caretOffset-prefix.length(), prefix.length());
            String textToInsert = getTextToInsert();
            doc.insertString(caretOffset-prefix.length(), textToInsert, null);
            setCaretOffset(component);
            Completion.get().hideAll();
        } 
        catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void processKeyEvent(KeyEvent ke) {
        
    }

    @Override
    public int getPreferredWidth(Graphics grphcs, Font font) {
        String leftText = adaptTextToHtml(getLeftText());
        String rightText = adaptTextToHtml(getRightText());
        return CompletionUtilities.getPreferredWidth(leftText, rightText, grphcs, font);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        String leftText = adaptTextToHtml(getLeftText());
        String rightText = adaptTextToHtml(getRightText());
        CompletionUtilities.renderHtml(fieldIcon, leftText, rightText, g, defaultFont, getColor(), width, height, selected);
    }

    @Override
    public CompletionTask createDocumentationTask() {
        return null;
    }

    @Override
    public CompletionTask createToolTipTask() {
        return null;
    }

    @Override
    public boolean instantSubstitution(JTextComponent jtc) {
        return false;
    }

    @Override
    public abstract int getSortPriority();
    
    @Override
    public abstract CharSequence getSortText();

    @Override
    public abstract CharSequence getInsertPrefix();

    protected abstract String getTextToInsert();
    
    protected abstract String getLeftText();
    
    protected abstract String getRightText();
    
    protected abstract void setCaretOffset(JTextComponent component);

    protected abstract Color getColor();
    
    private String adaptTextToHtml(String text) {
        StringBuilder adaptedText = new StringBuilder("");
        for(int i = 0; i < text.length(); i++) {
            char charAt = text.charAt(i);
            if(charAt == '<') {
                adaptedText.append("&lt;");
            }
            else if (charAt == '>') {
                adaptedText.append("&gt;");
            }
            else{
                adaptedText.append(charAt);
            }
        }
        return adaptedText.toString();
    }
}
