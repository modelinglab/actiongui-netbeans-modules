/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.dtm.language;

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
public class DtmLanguageHierarchy extends LanguageHierarchy<DtmTokenId> {

    private List<DtmTokenId> tokenIds;
    
    @Override
    protected Collection<DtmTokenId> createTokenIds() {
        if (tokenIds == null) {
            tokenIds = Collections.unmodifiableList(Arrays.asList(DtmTokenId.values()));
        }
        return tokenIds;
    }

    @Override
    protected Lexer<DtmTokenId> createLexer(LexerRestartInfo<DtmTokenId> info) {
        return new DtmLexer(info);
    }

    @Override
    protected String mimeType() {
        return "text/x-dtm";
    }

}
