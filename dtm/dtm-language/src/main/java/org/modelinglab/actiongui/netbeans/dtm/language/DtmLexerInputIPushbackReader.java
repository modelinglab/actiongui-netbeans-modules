/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.dtm.language;

import java.io.IOException;
import org.modelinglab.actiongui.tasks.dtmparser.sablecc.lexer.IPushbackReader;
import org.netbeans.spi.lexer.LexerInput;

/**
 *
 */
public class DtmLexerInputIPushbackReader implements IPushbackReader {

    private final LexerInput lexerInput;

    public DtmLexerInputIPushbackReader(LexerInput lexerInput) {
        this.lexerInput = lexerInput;
    }

    @Override
    public int read() throws IOException {
        int i = lexerInput.read();
        if (i == LexerInput.EOF) {
            return -1;
        }
        return i;
    }

    @Override
    public void unread(int c) throws IOException {
        lexerInput.backup(1);
    }
}
