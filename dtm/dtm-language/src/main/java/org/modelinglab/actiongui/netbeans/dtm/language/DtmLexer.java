/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.dtm.language;

import java.io.IOException;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.openide.util.Exceptions;

/**
 *
 */
class DtmLexer implements Lexer<DtmTokenId> {

    private LexerRestartInfo<DtmTokenId> info;
    private org.modelinglab.actiongui.tasks.dtmparser.sablecc.lexer.Lexer gtmLexer;

    public DtmLexer(final LexerRestartInfo<DtmTokenId> info) {
        this.info = info;
        gtmLexer = null;
    }

    @Override
    public Token<DtmTokenId> nextToken() {
        if (gtmLexer == null) {
            gtmLexer = new org.modelinglab.actiongui.tasks.dtmparser.sablecc.lexer.Lexer(new DtmLexerInputIPushbackReader(info.input()));
        }
        try {
            org.modelinglab.actiongui.tasks.dtmparser.sablecc.node.Token sableToken = gtmLexer.next();
            
            if (sableToken instanceof org.modelinglab.actiongui.tasks.dtmparser.sablecc.node.EOF) {
                return null;
            }
            else {
                return info.tokenFactory().createToken(DtmTokenId.fromSableccToken(sableToken));
            }
        } catch (org.modelinglab.actiongui.tasks.dtmparser.sablecc.lexer.LexerException | IOException ex) {
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
        gtmLexer = null;
    }

}
