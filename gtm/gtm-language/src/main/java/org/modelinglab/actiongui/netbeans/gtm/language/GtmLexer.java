/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.gtm.language;

import java.io.IOException;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.openide.util.Exceptions;

/**
 *
 */
class GtmLexer implements Lexer<GtmTokenId> {

    private LexerRestartInfo<GtmTokenId> info;
    private org.modelinglab.actiongui.mm.gtm.lexer.Lexer gtmLexer;

    public GtmLexer(final LexerRestartInfo<GtmTokenId> info) {
        this.info = info;
        gtmLexer = null;
    }

    @Override
    public Token<GtmTokenId> nextToken() {
        if (gtmLexer == null) {
            gtmLexer = new org.modelinglab.actiongui.mm.gtm.lexer.Lexer(new GtmLexerInputIPushbackReader(info.input()));
        }
        try {
            org.modelinglab.actiongui.mm.gtm.node.Token sableToken = gtmLexer.next();
            
            if (sableToken instanceof org.modelinglab.actiongui.mm.gtm.node.EOF) {
                return null;
            }
            else {
                return info.tokenFactory().createToken(GtmTokenId.fromSableccToken(sableToken));
            }
        } catch (org.modelinglab.actiongui.mm.gtm.lexer.LexerException | IOException ex) {
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
