/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.modelinglab.actiongui.netbeans.gtm.language;

import org.netbeans.api.lexer.Language;
import org.netbeans.modules.csl.spi.DefaultLanguageConfig;
import org.netbeans.modules.csl.spi.LanguageRegistration;
/**
 *
 */
@LanguageRegistration(mimeType = "text/x-gtm")
public class GtmLanguage extends DefaultLanguageConfig {

    @Override
    public Language getLexerLanguage() {
        return GtmTokenId.getLanguage();
    }

    @Override
    public String getDisplayName() {
        return "GTM";
    }

}
