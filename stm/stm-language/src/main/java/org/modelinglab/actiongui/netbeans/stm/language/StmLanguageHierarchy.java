/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.stm.language;

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
public class StmLanguageHierarchy extends LanguageHierarchy<StmTokenId> {

    private List<StmTokenId> tokenIds;
    
    @Override
    protected Collection<StmTokenId> createTokenIds() {
        if (tokenIds == null) {
            tokenIds = Collections.unmodifiableList(Arrays.asList(StmTokenId.values()));
        }
        return tokenIds;
    }

    @Override
    protected Lexer<StmTokenId> createLexer(LexerRestartInfo<StmTokenId> info) {
        return new StmLexer(info);
    }

    @Override
    protected String mimeType() {
        return "text/x-stm";
    }

}
