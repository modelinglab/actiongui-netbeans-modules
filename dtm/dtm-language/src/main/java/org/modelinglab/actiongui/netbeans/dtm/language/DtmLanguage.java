/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.dtm.language;

import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
/**
 *
 */
@LanguageRegistration(mimeType = "text/x-dtm")
public class DtmLanguage extends DefaultLanguageConfig {

    @Override
    public Language getLexerLanguage() {
        return DtmTokenId.getLanguage();
    }

    @Override
    public String getDisplayName() {
        return "DTM";
    }

}
