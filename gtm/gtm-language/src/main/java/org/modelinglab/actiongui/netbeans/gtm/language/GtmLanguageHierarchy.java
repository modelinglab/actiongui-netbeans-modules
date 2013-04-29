/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.gtm.language;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 */
public class GtmLanguageHierarchy extends LanguageHierarchy<GtmTokenId> {

    private List<GtmTokenId> tokenIds;
    
    @Override
    protected Collection<GtmTokenId> createTokenIds() {
        if (tokenIds == null) {
            tokenIds = Collections.unmodifiableList(Arrays.asList(GtmTokenId.values()));
        }
        return tokenIds;
    }

    @Override
    protected Lexer<GtmTokenId> createLexer(LexerRestartInfo<GtmTokenId> info) {
        return new GtmLexer(info);
    }

    @Override
    protected String mimeType() {
        return "text/x-gtm";
    }

}
