package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class Parser {
    private ArrayList<Token> tokens;
    private int currentTokenIndex = 0;
    private static Database databaseInstance = Database.getInstance();

    Parser(ArrayList<Token> inTokens) {
        tokens = inTokens;
    }

    private Boolean ensureTokenType(Token token, Token.TokenType... types){
        if(token != null){
            for(Token.TokenType type: types){
                if(token.getTokenType() == type){
                    return true;
                }
            }
        }
        throw new RuntimeException("Token type mismatch");
    }

    private Boolean ensureTokenValue(Token token, String... values){
        if(token != null){
            for(String value: values){
                if(token.getTokenValue().equals(value)){
                    return true;
                }
            }
        }
        throw new RuntimeException("Token value mismatch");
    }

    private Boolean ensureCurrentTypeAndValue(Token.TokenType type, String... values) {
        return ensureCurrentType(type) && ensureCurrentValue(values);
    }

    private Boolean ensureCurrentType(Token.TokenType... types) throws RuntimeException {
        // ensure that current index is within the range
        if(currentTokenIndex < 0 || currentTokenIndex >= tokens.size()){
            throw new RuntimeException("Current token index out of bounds");
        }

        boolean found = false;
        for(Token.TokenType type : types){
            if(tokens.get(currentTokenIndex).getTokenType() == type){
                found = true;
                break;
            }
        }
        if(!found){
            throw new RuntimeException("Token type mismatch");
        }
        return found;
    }

    private boolean ensureCurrentValue(String... values) throws RuntimeException {
        // ensure that current index is within the range
        if(currentTokenIndex < 0 || currentTokenIndex >= tokens.size()){
            throw new RuntimeException("Current token index out of bounds");
        }

        boolean found = false;
        for(String value : values){
            if(tokens.get(currentTokenIndex).getTokenValue().equals(value)){
                found = true;
                break;
            }
        }
        if(!found){
            throw new RuntimeException("Token value mismatch ");
        }
        return found;
    }

    private boolean checkCurrentTypeAndValue(Token.TokenType type, String... values) {
        return checkCurrentType(type) && checkCurrentValue(values);
    }

    private Boolean checkCurrentType(Token.TokenType... types) {
        if(currentTokenIndex < 0 || currentTokenIndex >= tokens.size()){
            return false;
        }
        boolean found = false;
        for(Token.TokenType type : types){
            if(tokens.get(currentTokenIndex).getTokenType() == type){
                found = true;
                break;
            }
        }
        return found;
    }

    private Boolean checkCurrentValue(String... values) {
        if(currentTokenIndex < 0 || currentTokenIndex >= tokens.size()){
            return false;
        }
        boolean found = false;
        for(String value : values){
            if(tokens.get(currentTokenIndex).getTokenValue().equals(value)){
                found = true;
                break;
            }
        }
        return found;
    }

    private Token.TokenType getCurrentTokenType() {
        if(currentTokenIndex < 0 || currentTokenIndex >= tokens.size()){
            throw new RuntimeException("Current token index out of bounds");
        }
        return tokens.get(currentTokenIndex).getTokenType();
    }

    private String getCurrentTokenValue() {
        if(currentTokenIndex < 0 || currentTokenIndex >= tokens.size()){
            throw new RuntimeException("Current token index out of bounds");
        }
        return tokens.get(currentTokenIndex).getTokenValue();
    }

    private Token.TokenType getTypeAt(int index) {
        if(index < 0 || index >= tokens.size()){
            throw new RuntimeException("Current token index out of bounds");
        }
        return tokens.get(index).getTokenType();
    }

    private String getValueAt(int index) {
        if(index < 0 || index >= tokens.size()){
            throw new RuntimeException("Current token index out of bounds");
        }
        return tokens.get(index).getTokenValue();
    }

    public void moveToNextToken() {
        currentTokenIndex++;
    }

    public void setCurrentToken(int index) {
        currentTokenIndex = index;
    }

    public Token getCurrentToken() {
        return tokens.get(currentTokenIndex);
    }

    public String parseTokens() throws Exception {
        if(tokens.get(tokens.size()-1).getTokenType() != Token.TokenType.EOQ){
            throw new RuntimeException("Semicolon missing from query");
        }
        else if(tokens.size() < 2){
            throw new RuntimeException("Query is incomplete");
        }

        setCurrentToken(1);
        if(tokens.get(0).getTokenType() != Token.TokenType.KEYWORD) {
            throw new RuntimeException("Invalid keyword");
        }
        switch (tokens.get(0).getTokenValue()) {
            case "USE":
                parseQueryUse();
                break;
            case "CREATE":
                parseQueryCreate();
                break;
            case "DROP":
                parseQueryDrop();
                break;
            case "ALTER":
                parseQueryAlter();
                break;
            case "INSERT":
                parseQueryInsert();
                break;
            case "SELECT":
                return parseQuerySelect();
                //break;
            case "UPDATE":
                parseQueryUpdate();
                break;
            case "DELETE":
                parseQueryDelete();
                break;
            case "JOIN":
                return parseQueryJoin();
                //break;
            default:
                throw new RuntimeException("Malformed query");
        }
        return "";
    }

    private void parseQueryAlter() throws IOException {
        ensureCurrentTypeAndValue(Token.TokenType.KEYWORD, "TABLE");
        moveToNextToken();
        ensureCurrentType(Token.TokenType.IDENTIFIER);
        moveToNextToken();
        ensureCurrentTypeAndValue(Token.TokenType.KEYWORD, "ADD", "DROP");
        moveToNextToken();
        ensureCurrentType(Token.TokenType.IDENTIFIER);
        moveToNextToken();
        ensureCurrentType(Token.TokenType.EOQ);

        //Alter table
        if(getValueAt(3).equals("ADD")){
            databaseInstance.addInTable(getValueAt(2), getValueAt(4));
        }
        //Drop table
        else if(getValueAt(3).equals("DROP")){
            databaseInstance.dropInTable(getValueAt(2), getValueAt(4));
        }
        else{
            throw new RuntimeException("Malformed query");
        }
    }

    private void parseQueryUse() throws IOException {
        ensureCurrentType(Token.TokenType.IDENTIFIER);
        moveToNextToken();
        ensureCurrentType(Token.TokenType.EOQ);
        databaseInstance.setUseDatabase(getValueAt(1));
    }

    private void parseQueryCreate() throws Exception {
        ensureCurrentTypeAndValue(Token.TokenType.KEYWORD, "DATABASE", "TABLE");
        if(getCurrentTokenValue().equals("DATABASE"))
        {
            moveToNextToken();
            ensureCurrentType(Token.TokenType.IDENTIFIER);
            moveToNextToken();
            ensureCurrentType(Token.TokenType.EOQ);
            // Create database here
            databaseInstance.createDatabase(getValueAt(2));

        }else if(getCurrentTokenValue().equals("TABLE")){
            moveToNextToken();
            ensureCurrentType(Token.TokenType.IDENTIFIER);
            moveToNextToken();
            if(checkCurrentType(Token.TokenType.EOQ)){
                databaseInstance.createTable(getValueAt(2),  Optional.empty());
            } else if(checkCurrentTypeAndValue(Token.TokenType.PARENTHESIS,"(")){
                ArrayList<String> columnNames = new ArrayList<String>();
                moveToNextToken();
                ensureCurrentType(Token.TokenType.IDENTIFIER);
                columnNames.add(getCurrentTokenValue());
                moveToNextToken();
                while(!(checkCurrentTypeAndValue(Token.TokenType.PARENTHESIS,")"))){
                    ensureCurrentType(Token.TokenType.SEPARATOR);
                    moveToNextToken();
                    ensureCurrentType(Token.TokenType.IDENTIFIER);
                    columnNames.add(getCurrentTokenValue());
                    moveToNextToken();
                }
                ensureCurrentTypeAndValue(Token.TokenType.PARENTHESIS, ")");
                moveToNextToken();
                ensureCurrentType(Token.TokenType.EOQ);
                //create table with column names
                databaseInstance.createTable(getValueAt(2), Optional.of(columnNames));
            }
            else{
                throw new RuntimeException("Malformed query 'create table'");
            }
        }
    }

    private void parseQueryDrop() throws Exception {
        ensureCurrentTypeAndValue(Token.TokenType.KEYWORD, "DATABASE", "TABLE");
        moveToNextToken();
        ensureCurrentType(Token.TokenType.IDENTIFIER);
        moveToNextToken();
        ensureCurrentType(Token.TokenType.EOQ);

        if (getValueAt(1).equals("DATABASE")) {
            // Drop database
            databaseInstance.dropDatabase(getValueAt(2));
        } else if (getValueAt(1).equals("TABLE")) {
            // Drop table
            databaseInstance.dropTable(getValueAt(2));
        }
        else{
            throw new RuntimeException("Malformed query");
        }
    }

    private void parseQueryInsert() throws Exception {
        ensureCurrentTypeAndValue(Token.TokenType.KEYWORD, "INTO");
        moveToNextToken();
        ensureCurrentType(Token.TokenType.IDENTIFIER);
        moveToNextToken();
        ensureCurrentTypeAndValue(Token.TokenType.KEYWORD, "VALUES");
        moveToNextToken();
        ensureCurrentTypeAndValue(Token.TokenType.PARENTHESIS, "(");
        moveToNextToken();
        ensureCurrentType(Token.TokenType.STRING_LITERAL, Token.TokenType.INTEGER_LITERAL,Token.TokenType.FLOAT_LITERAL,Token.TokenType.BOOLEAN_LITERAL);
        ArrayList<String> values = new ArrayList<String>();
        ArrayList<Table.ColumnType>types = new ArrayList<>();
        types.add(getCurrentColumnType(getCurrentTokenType()));
        values.add(getCurrentTokenValue());
        moveToNextToken();

        while(!checkCurrentTypeAndValue(Token.TokenType.PARENTHESIS, ")")){
            ensureCurrentType(Token.TokenType.SEPARATOR);
            moveToNextToken();
            ensureCurrentType(Token.TokenType.STRING_LITERAL, Token.TokenType.INTEGER_LITERAL,
                    Token.TokenType.FLOAT_LITERAL,Token.TokenType.BOOLEAN_LITERAL);
            types.add(getCurrentColumnType(getCurrentTokenType()));
            values.add(getCurrentTokenValue());
            moveToNextToken();
        }
        ensureCurrentTypeAndValue(Token.TokenType.PARENTHESIS, ")");
        moveToNextToken();
        ensureCurrentType(Token.TokenType.EOQ);
        //Insert to table (Add row)
        databaseInstance.insertToTable(getValueAt(2), values, types);
    }

    private Table.ColumnType getCurrentColumnType(Token.TokenType tokenType){
        return switch (tokenType) {
            case STRING_LITERAL -> Table.ColumnType.STRING;
            case INTEGER_LITERAL -> Table.ColumnType.INTEGER;
            case FLOAT_LITERAL -> Table.ColumnType.FLOAT;
            case BOOLEAN_LITERAL -> Table.ColumnType.BOOLEAN;
            default -> null;
        };
    }

    private String parseQuerySelect() throws Exception {
        ArrayList<String> attributeNames = new ArrayList<String>();
        if(checkCurrentType(Token.TokenType.WILDCARD)){
            attributeNames.add("ALL");
            moveToNextToken();
        }
        else if(ensureCurrentType(Token.TokenType.IDENTIFIER)){
            attributeNames.add(getCurrentTokenValue());
            moveToNextToken();
            while(checkCurrentType(Token.TokenType.SEPARATOR)){
                moveToNextToken();
                ensureCurrentType(Token.TokenType.IDENTIFIER);
                attributeNames.add(getCurrentTokenValue());
                moveToNextToken();
            }
        }
        checkCurrentTypeAndValue(Token.TokenType.KEYWORD, "FROM");
        moveToNextToken();
        checkCurrentType(Token.TokenType.IDENTIFIER);
        String tableName = getCurrentTokenValue();
        moveToNextToken();
        if(checkCurrentType(Token.TokenType.EOQ)){
            //  query table
            return databaseInstance.queryTable(tableName, attributeNames, Optional.empty());
        } else {
            ensureCurrentTypeAndValue(Token.TokenType.KEYWORD, "WHERE");
            moveToNextToken();
            OuterCondition condition = parseConditional();
            return databaseInstance.queryTable(tableName, attributeNames, Optional.of(condition));
        }
    }

    private void parseQueryUpdate() throws Exception {
        ensureCurrentType(Token.TokenType.IDENTIFIER);
        moveToNextToken();
        ensureCurrentTypeAndValue(Token.TokenType.KEYWORD, "SET");
        moveToNextToken();
        ArrayList<Triplet<String, String, Token.TokenType>> triplets = new ArrayList<Triplet<String, String, Token.TokenType>>();
        String key = "";
        String value = "";
        Token.TokenType type;
        triplets.add(parseQueryNameValuePair());
        moveToNextToken();
        while(checkCurrentType(Token.TokenType.SEPARATOR)){
            moveToNextToken();
            triplets.add(parseQueryNameValuePair());
            moveToNextToken();
        }
        ensureCurrentTypeAndValue(Token.TokenType.KEYWORD, "WHERE");
        moveToNextToken();
        OuterCondition condition = parseConditional();

        //Update Table
        databaseInstance.updateTable(getValueAt(1), triplets, condition);
    }

    private Triplet<String, String, Token.TokenType> parseQueryNameValuePair() throws Exception {
        ensureCurrentType(Token.TokenType.IDENTIFIER);
        String key = getCurrentTokenValue();
        moveToNextToken();
        ensureCurrentTypeAndValue(Token.TokenType.SYMBOL,"=");
        moveToNextToken();
        ensureCurrentType(Token.TokenType.STRING_LITERAL, Token.TokenType.INTEGER_LITERAL,
                Token.TokenType.FLOAT_LITERAL, Token.TokenType.BOOLEAN_LITERAL);
        String value = getCurrentTokenValue();
        Token.TokenType type = getCurrentTokenType();
        return new Triplet<>(key, value, type);
    }

    private void parseQueryDelete() throws Exception {
        ensureCurrentTypeAndValue(Token.TokenType.KEYWORD, "FROM");
        moveToNextToken();
        ensureCurrentType(Token.TokenType.IDENTIFIER);
        moveToNextToken();
        ensureCurrentTypeAndValue(Token.TokenType.KEYWORD, "WHERE");
        moveToNextToken();
        OuterCondition condition = parseConditional();
        databaseInstance.deleteFromTable(getValueAt(2), condition);
    }

    private String parseQueryJoin() throws Exception {
        ensureCurrentType(Token.TokenType.IDENTIFIER);
        moveToNextToken();
        ensureCurrentTypeAndValue(Token.TokenType.KEYWORD, "AND");
        moveToNextToken();
        ensureCurrentType(Token.TokenType.IDENTIFIER);
        moveToNextToken();
        ensureCurrentTypeAndValue(Token.TokenType.KEYWORD, "ON");
        moveToNextToken();
        ensureCurrentType(Token.TokenType.IDENTIFIER);
        moveToNextToken();
        ensureCurrentTypeAndValue(Token.TokenType.KEYWORD, "AND");
        moveToNextToken();
        ensureCurrentType(Token.TokenType.IDENTIFIER);

        // Join Tables
        return databaseInstance.joinTable(getValueAt(1),getValueAt(3),getValueAt(5),getValueAt(7));
    }

    private OuterCondition parseConditional() {
        ArrayList<Token> tokenList = new ArrayList<Token>();
        InnerCondition condition1 = null;
        InnerCondition condition2 = null;
        String boolOperator = null;

        while(!(checkCurrentType(Token.TokenType.EOQ))){
            tokenList.add(getCurrentToken());
            moveToNextToken();
        }

        if(tokenList.get(0).getTokenValue().equals(")") || tokenList.get(tokenList.size()-1).getTokenValue().equals("(")){
            throw new RuntimeException("Malformed parenthesis in condition");
        }
        //If the first token is opening parenthesis brace then last must be closing parenthesis else throw error
        if(tokenList.get(0).getTokenValue().equals("(") && tokenList.get(1).getTokenValue().equals("(")){
            if(tokenList.get(tokenList.size()-1).getTokenValue().equals(")") && tokenList.get(tokenList.size()-2).getTokenValue().equals(")")){
                tokenList.remove(tokenList.size()-1);
                tokenList.remove(0);
            }
            else{
                throw new RuntimeException("Malformed condition: matching brace not found");
            }
        }

        int boolOperatorIndex = -1;
        for(int i=0; i<tokenList.size(); i++){
            if(tokenList.get(i).getTokenValue().equals("AND") || tokenList.get(i).getTokenValue().equals("OR")){
                boolOperatorIndex = i;
                boolOperator = tokenList.get(i).getTokenValue();
                break;
            }
        }

        if(boolOperatorIndex < 0){
            condition1 = parseCondition(tokenList);
            return new OuterCondition(condition1,null,null);
        }
        else {
            condition1 = parseCondition(new ArrayList<>(tokenList.subList(0, boolOperatorIndex)));
            condition2 = parseCondition(new ArrayList<>(tokenList.subList(boolOperatorIndex+1, tokenList.size())));
            return new OuterCondition(condition1, boolOperator, condition2);
        }
    }

    private InnerCondition parseCondition(ArrayList<Token> tokenList)
    {
        if(tokenList.get(0).getTokenValue().equals(")") || tokenList.get(tokenList.size()-1).getTokenValue().equals("(")){
            throw new RuntimeException("Malformed parenthesis in condition");
        }
        //If the first token is opening parenthesis brace then last must be closing parenthesis else throw error
        if(tokenList.get(0).getTokenValue().equals("(")){
            if(tokenList.get(tokenList.size()-1).getTokenValue().equals(")")){
                tokenList.remove(tokenList.size()-1);
                tokenList.remove(0);
            }
            else{
                throw new RuntimeException("Malformed condition: matching brace not found");
            }
        }

        // the array list must have three token at this point. first should
        // be identifier, second comparator and third should be literal else throw error
        if(tokenList.size()!=3){
            throw new RuntimeException("Malformed condition");
        }
        ensureTokenType(tokenList.get(0), Token.TokenType.IDENTIFIER);
        ensureTokenType(tokenList.get(1), Token.TokenType.COMPARATOR);
        ensureTokenType(tokenList.get(2), Token.TokenType.STRING_LITERAL, Token.TokenType.INTEGER_LITERAL,
                Token.TokenType.FLOAT_LITERAL, Token.TokenType.BOOLEAN_LITERAL);
        return new InnerCondition(tokenList.get(0).getTokenValue(), tokenList.get(1).getTokenValue(), tokenList.get(2).getTokenValue());
    }
}
