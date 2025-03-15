package edu.uob;

import java.util.ArrayList;
import java.util.Arrays;

public class Tokenizer {
    private String command = "";
    private ArrayList<Token> tokens = new ArrayList<>();
    //private int currentIndex = -1;
    public Tokenizer(String str) {
        command = str;
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }

    public void Tokenize() {
        int idx = 0;
        while (idx < command.length()) {
            int start = idx;
            StringBuilder tokenVal = new StringBuilder();
            // Check semicolon
            if(command.charAt(idx) == ';') {
                // Do not check anything beyond semicolon
                tokens.add(new Token(Token.TokenType.EOQ, ";"));
                break;
            }
            // Check whitespace
            else if(command.charAt(idx) == ' ') {
                idx++;
            }
            //Check separator
            else if(command.charAt(idx) == ',') {
                tokens.add(new Token(Token.TokenType.SEPARATOR, ","));
                idx++;
            }
            // Check parenthesis
            else if(command.charAt(idx) == '(' || command.charAt(idx) == ')') {
                tokens.add(new Token(Token.TokenType.PARENTHESIS, String.valueOf(command.charAt(idx))));
                idx++;
            }
            // Check symbol
            else if(symbols.contains(String.valueOf(command.charAt(idx)))) {
                if(command.charAt(idx) == '*'){
                    tokens.add(new Token(Token.TokenType.WILDCARD, String.valueOf(command.charAt(idx))));
                    idx++;
                }
                else{
                    while(idx < command.length() && symbols.contains(String.valueOf(command.charAt(idx)))) {
                        tokenVal.append(command.charAt(idx));
                        idx++;
                    }
                    if(comparators.contains(tokenVal.toString().toLowerCase())) {
                        tokens.add(new Token(Token.TokenType.COMPARATOR, tokenVal.toString()));
                    }
                    else{
                        tokens.add(new Token(Token.TokenType.SYMBOL, tokenVal.toString()));
                    }
                }
            }
            // Check letter or number
            else if(Character.isLetter(command.charAt(idx)) || Character.isDigit(command.charAt(idx)) ) {
                boolean digitFound = false;
                boolean letterFound = false;
                while(idx < command.length() && (Character.isLetter(command.charAt(idx)) || Character.isDigit(command.charAt(idx)))) {
                    if(!digitFound && Character.isDigit(command.charAt(idx))){
                        digitFound = true;
                    }
                    if(!letterFound && Character.isLetter(command.charAt(idx))){
                        letterFound = true;
                    }
                    tokenVal.append(command.charAt(idx));
                    idx++;
                }

                // if the current character we have is a decimal point, and we have not found any letter
                // we might have a float literal
                if(idx < command.length() && command.charAt(idx) == '.' && !letterFound && digitFound) {
                    do {
                        tokenVal.append(command.charAt(idx));
                        idx++;
                    } while (Character.isDigit(command.charAt(idx)));
                }

                if(letterFound && !digitFound) {
                    // it could be keyword or identifier
                    if(keywords.contains(tokenVal.toString().toUpperCase())) {

                        if(trailingSpaceKeyWords.contains(tokenVal.toString().toUpperCase())
                        && command.charAt(idx) != ' ') {
                            // current character must be a space
                            throw new RuntimeException("Keyword: '" + tokenVal.toString().toUpperCase() + "' must have space after it");
                        }
                        else if(leadingSpaceKeywords.contains(tokenVal.toString().toUpperCase()) && start > 0 && command.charAt(start-1) != ' ') {
                            throw new RuntimeException("Keyword: '" + tokenVal.toString().toUpperCase() + "' must have space before it");
                        }
                        else if(spacePaddedKeywords.contains(tokenVal.toString().toUpperCase())
                        && (command.charAt(idx) != ' ' || (start > 0 && command.charAt(start-1) != ' '))) {
                            throw new RuntimeException("Keyword: '" + tokenVal.toString().toUpperCase() + "' must have space before and after it");
                        }

                        tokens.add(new Token(Token.TokenType.KEYWORD, tokenVal.toString().toUpperCase()));
                    }
                    else if(booleanLiterals.contains(tokenVal.toString().toUpperCase())) {
                        tokens.add(new Token(Token.TokenType.BOOLEAN_LITERAL, tokenVal.toString().toUpperCase()));
                    }
                    else if(tokenVal.toString().equalsIgnoreCase("LIKE")){
                        tokens.add(new Token(Token.TokenType.COMPARATOR, tokenVal.toString().toUpperCase()));
                    }
                    else {
                        tokens.add(new Token(Token.TokenType.IDENTIFIER, tokenVal.toString()));
                    }
                }
                else if(digitFound && !letterFound) {
                    // it is a number, either integer or float
                    if(tokenVal.toString().indexOf('.') >= 0){
                        tokens.add(new Token(Token.TokenType.FLOAT_LITERAL, tokenVal.toString()));
                    }
                    else {
                        tokens.add(new Token(Token.TokenType.INTEGER_LITERAL, tokenVal.toString()));
                    }
                }
                else if(digitFound && letterFound) {
                    // it is an identifier
                    tokens.add(new Token(Token.TokenType.IDENTIFIER, tokenVal.toString()));
                }
            }
            // Check + or -
            else if(command.charAt(idx) == '+' || command.charAt(idx) == '-') {
                boolean decimalFound = false;
                boolean negative = command.charAt(idx) == '-';
                if(negative){
                    tokenVal.append(command.charAt(idx));
                }
                idx++;
                while (idx < command.length() && Character.isDigit(command.charAt(idx))){
                    tokenVal.append(command.charAt(idx));
                    idx++;
                }
                if(command.charAt(idx) == '.') {
                    decimalFound = true;
                    tokenVal.append(command.charAt(idx));
                    idx++;
                    while (idx < command.length() && Character.isDigit(command.charAt(idx))){
                        tokenVal.append(command.charAt(idx));
                        idx++;
                    }
                }

                if(decimalFound) {
                    tokens.add(new Token(Token.TokenType.FLOAT_LITERAL, tokenVal.toString()));
                }
                else {
                    tokens.add(new Token(Token.TokenType.INTEGER_LITERAL, tokenVal.toString()));
                }
            }
            //Check string literal enclosed by ''
            else if(command.charAt(idx) == '\'') {
                idx++;
                do {
                    tokenVal.append(command.charAt(idx));
                    idx++;
                }while (idx < command.length() && command.charAt(idx) != '\'');

                tokens.add(new Token(Token.TokenType.STRING_LITERAL, tokenVal.toString()));
                idx++;
            }
        }
    }

    private static final ArrayList<String> keywords = new ArrayList<String>(Arrays.asList(
            "USE", "CREATE", "DROP", "ALTER", "INSERT", "SELECT", "UPDATE", "DELETE", "JOIN", "DATABASE",
            "TABLE", "ADD", "INTO", "VALUES", "FROM", "WHERE", "SET", "AND", "ON", "OR"
    ));

    private static final ArrayList<String> trailingSpaceKeyWords = new ArrayList<>(Arrays.asList(
            "USE", "CREATE", "DATABASE", "TABLE", "DROP", "ALTER", "INSERT", "INTO", "SELECT", "UPDATE", "DELETE", "JOIN"
    ));

    private static final ArrayList<String> leadingSpaceKeywords = new ArrayList<>(Arrays.asList(
            "VALUES"
    ));

    private static final ArrayList<String> spacePaddedKeywords = new ArrayList<>(Arrays.asList(
            "FROM", "WHERE", "SET", "AND", "OR", "ON", "ADD"
    ));

    private static final ArrayList<String> comparators = new ArrayList<String>(Arrays.asList(
            "==", ">", "<", ">=", "<=", "!=", "LIKE"
    ));

    private static final ArrayList<String> symbols = new ArrayList<String>(Arrays.asList(
            "!", "#" , "$" , "%" , "&" , "(" , ")" , "*" , "," , "." , "/" , ":" , ";" ,
            ">" , "=" , "<" , "?" , "@" , "[" , "\\" , "]" , "^" , "_" , "`" , "{" , "}" , "~"
    ));

    private static final ArrayList<String> booleanLiterals = new ArrayList<String>(Arrays.asList(
            "TRUE", "FALSE"
    ));
}


