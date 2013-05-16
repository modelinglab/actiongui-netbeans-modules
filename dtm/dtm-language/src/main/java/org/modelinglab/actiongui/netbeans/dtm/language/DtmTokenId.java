/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.modelinglab.actiongui.netbeans.dtm.language;

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;

/**
 *
 */
public enum DtmTokenId implements TokenId {

    ERROR,
    ID,
    KEYWORD,
    COMMENT,
    SEPARATOR,
    TYPE,
    WHITESPACE;

    @Override
    public String primaryCategory() {
        return this.name();
    }

    public static Language<DtmTokenId> getLanguage() {
        return new DtmLanguageHierarchy().language();
    }

    public static DtmTokenId fromSableccToken(org.modelinglab.actiongui.tasks.dtmparser.sablecc.node.Token sableToken) {
        switch (sableToken.getClass().getSimpleName()) {
            case "TBlank":
            case "TEol":
                return WHITESPACE;
            case "TColon":
            case "TComma":
            case "TLBrace":
            case "TLParen":
            case "TRBrace":
            case "TRParen":
            case "TSemi":
                return SEPARATOR;
            case "TCommentBody":
            case "TCommentStart":
            case "TInlineComment":
                return COMMENT;
            case "TEntity":
            case "TEnum":
            case "TExtends":
            case "TIsUser":
            case "TOppositeTo":
                return KEYWORD;
            case "TPrimitiveType":
            case "TCollectionType":
            case "TTupleType":
                return TYPE;
            case "TId":
                return ID;
            
            case "TError":
            default:
                return ERROR;
        }
    }
}
