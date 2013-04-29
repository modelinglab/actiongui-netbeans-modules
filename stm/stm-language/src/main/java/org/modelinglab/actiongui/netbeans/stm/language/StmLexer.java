/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.stm.language;

import java.io.IOException;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.openide.util.Exceptions;

/**
 *
 */
class StmLexer implements Lexer<StmTokenId> {

    private LexerRestartInfo<StmTokenId> info;
    private org.modelinglab.actiongui.tasks.stmparser.sablecc.lexer.Lexer stmLexer;

    public StmLexer(final LexerRestartInfo<StmTokenId> info) {
        this.info = info;
        stmLexer = null;
    }

    @Override
    public Token<StmTokenId> nextToken() {
        if (stmLexer == null) {
            stmLexer = new org.modelinglab.actiongui.tasks.stmparser.sablecc.lexer.Lexer(new StmLexerInputIPushbackReader(info.input()));
        }
        try {
            org.modelinglab.actiongui.tasks.stmparser.sablecc.node.Token sableToken = stmLexer.next();
            
            if (sableToken instanceof org.modelinglab.actiongui.tasks.stmparser.sablecc.node.EOF) {
                return null;
            }
            else {
                return info.tokenFactory().createToken(StmTokenId.fromSableccToken(sableToken));
            }
        } catch (org.modelinglab.actiongui.tasks.stmparser.sablecc.lexer.LexerException | IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }        
    }

    @Override
    public Object state() {
        return null;
    }

    @Override
    public void release() {
        stmLexer = null;
    }

}
