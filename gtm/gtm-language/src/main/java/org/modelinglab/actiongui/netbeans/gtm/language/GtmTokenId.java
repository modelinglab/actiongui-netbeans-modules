/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.modelinglab.actiongui.netbeans.gtm.language;

import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenId;

/**
 *
 */
public enum GtmTokenId implements TokenId {

    ERROR,
    ID,
    KEYWORD,
    COMMENT,
    OPERATOR,
    SEPARATOR,
    EVENT,
    OCL,
    TYPE,
    WIDGET,
    ACTION,
    WHITESPACE;

    @Override
    public String primaryCategory() {
        return this.name();
    }

    public static Language<GtmTokenId> getLanguage() {
        return new GtmLanguageHierarchy().language();
    }

    public static GtmTokenId fromSableccToken(org.modelinglab.actiongui.mm.gtm.node.Token sableToken) {
        switch (sableToken.getClass().getSimpleName()) {
            case "TAssign":
            case "TMinusAssign":
            case "TPlusAssign":
                return OPERATOR;
            case "TBack":
            case "TCall":
            case "TCatchTk":
            case "TDelete":
            case "TElse":
            case "TExit":
            case "TForeach":
            case "TIf":
            case "TNotification":
            case "TNew":
            case "TOpen":
            case "TReevaluate":
            case "TTry":
                return ACTION;
            case "TBasicType":
            case "TCollectionType":
            case "TTupleType":
                return TYPE;
            case "TBlank":
            case "TEol":
                return WHITESPACE;
            case "TBooleanField":
            case "TButton":
            case "TComboBox":
            case "TDateField":
            case "TImage":
            case "TLabel":
            case "TPasswordField":
            case "TTable":
            case "TTextField":
            case "TUpload":
            case "TWindow":
                return WIDGET;
            case "TColon":
            case "TComma":
            case "TDollar":
            case "TDot":
            case "TLBrace":
            case "TLBracket":
            case "TLParen":
            case "TOr":
            case "TRBrace":
            case "TRBracket":
            case "TRParen":
            case "TSemi":
                return SEPARATOR;
            case "TColumns":
            case "TEventT":
            case "TIn":
            case "TIsMain":
                return KEYWORD;
            case "TCommentBody":
            case "TCommentEnd":
            case "TCommentStart":
            case "TInlineComment":
                return COMMENT;
            case "TIdentifier":
                return ID;
            case "TOcl":
                return OCL;
            case "TOnClick":
            case "TOnCreate":
            case "TOnView":
                return EVENT;

            case "TError":
            default:
                return ERROR;
        }
    }
}
