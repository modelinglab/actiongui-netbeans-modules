/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.modelinglab.actiongui.netbeans.stm.language;

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;

/**
 *
 */
public enum StmTokenId implements TokenId {

    ERROR,
    ID,
    ACTION,
    COMMENT,
    SEPARATOR,
    OCL,
    KEYWORD,
    WHITESPACE;

    @Override
    public String primaryCategory() {
        return this.name();
    }

    public static Language<StmTokenId> getLanguage() {
        return new StmLanguageHierarchy().language();
    }

    public static StmTokenId fromSableccToken(org.modelinglab.actiongui.tasks.stmparser.sablecc.node.Token sableToken) {
        switch (sableToken.getClass().getSimpleName()) {
            case "TFullAccess":
            case "TCreate":
            case "TDelete":
            case "TRead":
            case "TUpdate":
            case "TAdd":
            case "TRemove":
                return ACTION;
            case "TBlank":
            case "TEol":
                return WHITESPACE;
            case "TComma":
            case "TLBrace":
            case "TLBracket":
            case "TRBrace":
            case "TRBracket":
                return SEPARATOR;
            case "TExtends":
            case "TRoleTk":
                return KEYWORD;
            case "TCommentBody":
            case "TCommentEnd":
            case "TCommentStart":
            case "TInlineComment":
                return COMMENT;
            case "TId":
                return ID;
            case "TOclExp":
                return OCL;
            
            case "TError":
            default:
                return ERROR;
        }
    }
}
