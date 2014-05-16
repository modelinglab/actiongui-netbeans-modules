/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.gtm.autocompletion.completionitems.statements;

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
public abstract class GTMStatementCompletionItem implements CompletionItem{
    private final String prefix;
    private final int caretOffset;
    protected final String nameAction;
    protected final String textAction;
    protected final ImageIcon fieldIcon;

    protected GTMStatementCompletionItem(String prefix, int caretOffset, String nameAction, String textAction) {
        assert nameAction.startsWith(prefix);
        this.prefix = prefix;
        this.caretOffset = caretOffset;
        this.nameAction = nameAction;
        this.textAction = textAction;
        this.fieldIcon = new ImageIcon(ImageUtilities.loadImage("org/modelinglab/actiongui/netbeans/gtm/autocompletion/action.png"));
    }

    @Override
    public void defaultAction(JTextComponent component) {
        try {
            StyledDocument doc = (StyledDocument) component.getDocument();
            //Here we remove the characters starting at the start offset
            //and ending at the point where the caret is currently found:
            doc.remove(caretOffset-prefix.length(), prefix.length());
            doc.insertString(caretOffset-prefix.length(), textAction, null);
            Completion.get().hideAll();
        } 
        catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void processKeyEvent(KeyEvent evt) {

    }

    @Override
    public int getPreferredWidth(Graphics g, Font defaultFont) {
        String leftText = adaptTextToHtml(nameAction);
        return CompletionUtilities.getPreferredWidth(leftText, "", g, defaultFont);
    }

    @Override
    public void render(Graphics g, Font defaultFont, Color defaultColor, Color backgroundColor, int width, int height, boolean selected) {
        String leftText = adaptTextToHtml(nameAction);
        CompletionUtilities.renderHtml(fieldIcon, leftText, "", g, defaultFont, getColor(), width, height, selected);
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
    public boolean instantSubstitution(JTextComponent component) {
        return false;
    }

    @Override
    public int getSortPriority() {
        return 1;
    }

    @Override
    public CharSequence getSortText() {
        return nameAction;
    }

    @Override
    public CharSequence getInsertPrefix() {
        return nameAction;
    }
    
    protected Color getColor() {
        return Color.RED;
    }
    
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
    
    public static boolean isValidStatementChar(char c) {
        return Character.isLetter(c) || c == '-';
    }
}
