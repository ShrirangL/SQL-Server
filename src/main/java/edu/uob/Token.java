package edu.uob;

public class Token {

    public enum TokenType{
        KEYWORD, IDENTIFIER, STRING_LITERAL, INTEGER_LITERAL, FLOAT_LITERAL, BOOLEAN_LITERAL, SEPARATOR, COMPARATOR, PARENTHESIS, WILDCARD, SYMBOL, EOQ //End of query
    }

    private final TokenType tokenType;
    private final String tokenValue;

    Token(TokenType type, String value){
        this.tokenType = type;
        this.tokenValue = value;
    }

    public TokenType getTokenType() {
        return this.tokenType;
    }

    public String getTokenValue() {
        return this.tokenValue;
    }
}
