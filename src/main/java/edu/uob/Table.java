package edu.uob;

import javafx.util.Pair;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.*;

public class Table {
    private String name;
    private ArrayList<String> columnNames = new ArrayList<String>();
    private ArrayList<ColumnType>columnTypes = new ArrayList<ColumnType>();
    private ArrayList<ArrayList<String>> entries = new ArrayList<ArrayList<String>>();
    private String tablePath;

    public enum ColumnType{
        STRING, INTEGER, FLOAT, BOOLEAN
    }

    public String getName(){
        return name;
    }

    Table(String pathToFile, Optional<ArrayList<String>> colNames) throws Exception {
        try {
            File file = new File(pathToFile);
            if(file.exists()) {
                name = file.getName();
                tablePath = pathToFile;
            }
            else{
                throw new FileNotFoundException();
            }
            Scanner myReader = new Scanner(file);
            // read attribute names
            if(myReader.hasNextLine()){
                String columns = myReader.nextLine();
                if(!columns.isEmpty()){
                    columnNames = new ArrayList<>(Arrays.asList(columns.split("\t")));
                }
            }
            // If we have don't have column name it means this is new table
            if(!columnNames.isEmpty()) {
                // read attribute types
                if (myReader.hasNextLine()) {
                    String dataTypes = myReader.nextLine();
                    if (!dataTypes.isEmpty()) {
                        ArrayList<String> types = new ArrayList<>(Arrays.asList(dataTypes.split("\t")));
                        for (String type : types) {
                            switch (type.toUpperCase()) {
                                case "STRING":
                                    columnTypes.add(ColumnType.STRING);
                                    break;
                                case "INTEGER":
                                    columnTypes.add(ColumnType.INTEGER);
                                    break;
                                case "FLOAT":
                                    columnTypes.add(ColumnType.FLOAT);
                                    break;
                                case "BOOLEAN":
                                    columnTypes.add(ColumnType.BOOLEAN);
                            }
                        }
                    }
                }
                while (myReader.hasNextLine()) {
                    entries.add(new ArrayList<>(Arrays.asList(myReader.nextLine().split("\t"))));
                }
            }
            else{
                if(colNames.isPresent()){
                    colNames.get().add(0, "id");
                    columnNames.addAll(colNames.get());
                    writeToFile();
                }
            }
            myReader.close();
        } catch (Exception e) {
            throw e;
        }
    }

    public Integer checkColumnName(String colName){
        for (int i = 0; i < columnNames.size(); i++) {
            if(columnNames.get(i).equalsIgnoreCase(colName.toLowerCase())){
                return i;
            }
        }
        return -1;
    }

    public ColumnType checkColumnType(String colName){
        for (int i = 0; i < columnNames.size(); i++) {
            if(columnNames.get(i).equalsIgnoreCase(colName.toLowerCase())){
                return columnTypes.get(i);
            }
        }
        return null;
    }

    public ArrayList<ArrayList<String>> getEntries(){
        return entries;
    }

    public ArrayList<String> getColumnNames() {
        return columnNames;
    }

    public ArrayList<ColumnType> getColumnTypes() {
        return columnTypes;
    }

    public void insertColumn(String columnName) throws IOException {
        if(checkColumnName(columnName) < 0) {
            columnNames.add(columnName);
            for (ArrayList<String> entry : entries) {
                entry.add("NULL");
            }
            writeToFile();
        }
        else {
            throw new IOException("Column " + columnName + "already exists");
        }
    }

    public void removeColumn(String columnName) throws IOException {
        int index = checkColumnName(columnName);
        if(index > 0 && index < columnNames.size()) {
            columnNames.remove(index);
            columnTypes.remove(index);
            for (ArrayList<String> entry : entries) {
                entry.remove(index);
            }
            writeToFile();
        }
        else {
            throw new IOException("Column " + columnName + "does not exist or cannot be removed");
        }
    }

    public void insertEntry(ArrayList<String> entry, ArrayList<ColumnType> types) throws IOException {
        // If table is newly created then we the first insert entry must set the column types
        if(entries.isEmpty()) {
            types.add(0, ColumnType.INTEGER); // first column is id of type int
            columnTypes.addAll(types); // get types directly from first entry
            entry.add(0, String.valueOf(1)); // the first id is always 1
            entries.add(entry);
        }
        else {
            // else check if the incoming types match the existing types except the first column
            ArrayList<ColumnType> entryTypes = new ArrayList<ColumnType>(columnTypes);
            entryTypes.remove(0);
            if (!entryTypes.equals(types)) {
                throw new IOException("Column type mismatch");
            } else {
                int lastIndex = Integer.parseInt(entries.get(entries.size() - 1).get(0));
                entry.add(0, String.valueOf(lastIndex+1));
                entries.add(entry);
            }
        }
        writeToFile();
    }

    public String queryTable(ArrayList<String> columns, Optional<OuterCondition> condition) throws IOException {
        //  if a single column is not valid throw error
        ArrayList<Integer>indexes = new ArrayList<Integer>();
        StringBuilder output = new StringBuilder();
        if(columns.get(0).equalsIgnoreCase("ALL")) {
            columns = columnNames;
        }
        for(String column : columns){
            if(checkColumnName(column) > -1){
                indexes.add(checkColumnName(column));
            }
            else{
                throw new IOException("Invalid column name "+ column);
            }
        }
        // first get list of result of entries based on condition provided
        ArrayList<ArrayList<String>> result;
        if(condition.isPresent()) {
            result = filterEntries(condition.get());
        }
        else {
            result = entries;
        }

        for(int i = 0; i < indexes.size(); i++){
            output.append(columnNames.get(indexes.get(i)));
            if(i < indexes.size()-1){
                output.append("\t");
            }
        }
        output.append("\n");
        // then use column names to show only what is needed based on column
        for(ArrayList<String> entry : result){
            for(int i = 0; i < indexes.size(); i++){
                if(!entry.get(indexes.get(i)).equals("NULL")){
                    output.append(entry.get(indexes.get(i)));
                }
                if(i < indexes.size()-1){
                    output.append("\t");
                }
            }
            output.append("\n");
        }
        return output.toString();
    }

    public ArrayList<ArrayList<String>> filterEntries(OuterCondition condition) {
        ArrayList<ArrayList<String>> filteredEntries = new ArrayList<ArrayList<String>>(entries);

        if(condition.firstCondition == null){
            throw new RuntimeException("empty condition");
        }
        // if we have only one condition
        if(condition.booleanOperator == null) {
            filterEntries(filteredEntries, condition.firstCondition);
        }
        else {
            switch (condition.booleanOperator) {
                case "AND":
                    filterEntries(filteredEntries, condition.firstCondition);
                    filterEntries(filteredEntries, condition.secondCondition);
                    break;
                case "OR":
                    ArrayList<ArrayList<String>> copyEntries = new ArrayList<ArrayList<String>>(entries);
                    filterEntries(filteredEntries, condition.firstCondition);
                    filterEntries(copyEntries, condition.secondCondition);
                    // Use a Set to remove duplicates
                    HashSet<ArrayList<String>> set = new HashSet<ArrayList<String>>(filteredEntries);
                    set.addAll(copyEntries);
                    copyEntries.clear();
                    filteredEntries.clear();
                    filteredEntries = new ArrayList<ArrayList<String>>(set);
                    set.clear();
                    break;
            }
        }
        return filteredEntries;
    }

    public void filterEntries(ArrayList<ArrayList<String>> list, InnerCondition condition) {
        // iterate through all the entries in the first parameter and remove entries that do not match
        int index = checkColumnName(condition.attribute);
        ColumnType type = columnTypes.get(index);
        if(index < 0 || index >= entries.get(0).size()) {
            throw new IndexOutOfBoundsException("Column name in condition does not exist");
        }
        switch (condition.comparator){
            case "==":
                // works with all data types string, integer and float
                for(int i = 0; i < list.size(); i++){
                    ArrayList<String> entry = list.get(i);
                    if(!entry.get(index).equals(condition.value)){
                        list.remove(i);
                        i--;
                    }
                }
                break;
            case ">":
                // works with all
                for(int i = 0; i < list.size(); i++){
                    ArrayList<String> entry = list.get(i);
                    if((type == ColumnType.INTEGER && Integer.parseInt(entry.get(index)) <= Integer.parseInt(condition.value))
                            || (type == ColumnType.FLOAT && Float.parseFloat(entry.get(index)) <= Float.parseFloat(condition.value))
                            || (type == ColumnType.STRING && entry.get(index).compareTo(condition.value) <= 0)){
                        list.remove(i);
                        i--;
                    }
                }
                break;
            case "<":
                for(int i = 0; i < list.size(); i++){
                    ArrayList<String> entry = list.get(i);
                    if((type == ColumnType.INTEGER && Integer.parseInt(entry.get(index)) >= Integer.parseInt(condition.value))
                            || (type == ColumnType.FLOAT && Float.parseFloat(entry.get(index)) >= Float.parseFloat(condition.value))
                            || (type == ColumnType.STRING && entry.get(index).compareTo(condition.value) >= 0)){
                        list.remove(i);
                        i--;
                    }
                }
                break;
            case ">=":
                for(int i = 0; i < list.size(); i++){
                    ArrayList<String> entry = list.get(i);
                    if((type == ColumnType.INTEGER && Integer.parseInt(entry.get(index)) < Integer.parseInt(condition.value))
                            || (type == ColumnType.FLOAT && Float.parseFloat(entry.get(index)) < Float.parseFloat(condition.value))
                            || (type == ColumnType.STRING && entry.get(index).compareTo(condition.value) < 0)){
                        list.remove(i);
                        i--;
                    }
                }
                break;
            case "<=":
                for(int i = 0; i < list.size(); i++){
                    ArrayList<String> entry = list.get(i);
                    if((type == ColumnType.INTEGER && Integer.parseInt(entry.get(index)) > Integer.parseInt(condition.value))
                            || (type == ColumnType.FLOAT && Float.parseFloat(entry.get(index)) > Float.parseFloat(condition.value))
                            || (type == ColumnType.STRING && entry.get(index).compareTo(condition.value) > 0)){
                        list.remove(i);
                        i--;
                    }
                }
                break;
            case "!=":
                // Works for all data types
                for(int i = 0; i < list.size(); i++){
                    ArrayList<String> entry = list.get(i);
                    if(entry.get(index).equals(condition.value)){
                        list.remove(i);
                        i--;
                    }
                }
                break;
            case "LIKE":
                // it works with all data types
                for(int i = 0; i < list.size(); i++){
                    ArrayList<String> entry = list.get(i);
                    if(!entry.get(index).contains(condition.value)){
                        list.remove(i);
                        i--;
                    }
                }
                break;
        }
    }

    private Boolean isCompatibleType(Token.TokenType tokenType, Table.ColumnType colType){
        return (tokenType == Token.TokenType.STRING_LITERAL && colType == ColumnType.STRING)
                || (tokenType == Token.TokenType.INTEGER_LITERAL && colType == ColumnType.INTEGER)
                || (tokenType == Token.TokenType.FLOAT_LITERAL && colType == ColumnType.FLOAT)
                || (tokenType == Token.TokenType.BOOLEAN_LITERAL && colType == ColumnType.BOOLEAN);
    }

    public void updateEntries(ArrayList<Triplet<String,String,Token.TokenType>>values, OuterCondition condition) throws IOException {
        // Filter the entries based on condition then apply the values to columns names of these entries
        // iterate through column names and column types of the input arguments see
        ArrayList<Integer>indexes = new ArrayList<>(); // keep track of index of columns
        for(Triplet<String,String,Token.TokenType> value : values){
            // check if the column name exists and if the corresponding type is compatible
            int index = -1;
            index = checkColumnName(value.first);
            indexes.add(index);
            if(index == -1){
                throw new IOException("Incorrect column name :" + value.first);
            }
            if(index < columnTypes.size() && !isCompatibleType(value.third, columnTypes.get(index))){
                throw new IOException("Incompatible column type at index: " + index);
            }
            else if(index < columnNames.size() && index >= columnTypes.size()){
                // then this new column is yet to be assigned a type. Assign it incoming type
                switch (value.third){
                    case STRING_LITERAL:
                        columnTypes.add(ColumnType.STRING);
                        break;
                    case INTEGER_LITERAL:
                        columnTypes.add(ColumnType.INTEGER);
                        break;
                    case FLOAT_LITERAL:
                        columnTypes.add(ColumnType.FLOAT);
                        break;
                    case BOOLEAN_LITERAL:
                        columnTypes.add(ColumnType.BOOLEAN);
                        break;
                }
            }
        }

        // Iterate through all the entries and if they match the condition update them
        for(int i = 0; i < entries.size(); i++) {
            // if the entry matches condition
            if(entryMatches(i, condition)){
                ArrayList<String> entry = entries.get(i);
                //Update the entry
                for(int j = 0; j < indexes.size(); j++){
                    entry.set(indexes.get(j), values.get(j).second);
                }
            }
        }
        writeToFile();
    }

    public void deleteEntries(OuterCondition condition) throws IOException {
        for(int i = 0; i < entries.size(); i++) {
            if(entryMatches(i, condition)){
                entries.remove(i);
                i--;
            }
        }
        writeToFile();
    }

    public Boolean entryMatches(Integer index, OuterCondition condition) {
        // see it an entry matched inner condition
        // If the outer condition has only one val
        if(condition.booleanOperator == null && condition.firstCondition != null) {
            return entryMatches(index, condition.firstCondition);
        }
        // if the outer condition has two values -> check if we have AND or OR
        else if(condition.booleanOperator != null && condition.firstCondition != null && condition.secondCondition != null) {
            switch (condition.booleanOperator) {
                case "AND":
                    return entryMatches(index, condition.firstCondition) && entryMatches(index, condition.secondCondition);
                case "OR":
                    return entryMatches(index, condition.firstCondition) || entryMatches(index, condition.secondCondition);
            }
        }
        return false;
    }

    public Boolean entryMatches(Integer index, InnerCondition condition) {
        // see it an entry matched inner condition
        ArrayList<String> entry = entries.get(index);
        int columnIndex = checkColumnName(condition.attribute);
        ColumnType type = columnTypes.get(columnIndex);

        if(entry == null || columnIndex < 0 || type == null) {
            throw new RuntimeException("Incorrect attribute in condition");
        }

        switch(condition.comparator){
            case "==":
                if(entry.get(columnIndex).equals(condition.value)){
                    return true;
                }
                break;
            case ">":
                if((type == ColumnType.INTEGER && Integer.parseInt(entry.get(columnIndex)) > Integer.parseInt(condition.value))
                        || (type == ColumnType.FLOAT && Float.parseFloat(entry.get(columnIndex)) > Float.parseFloat(condition.value))
                        || (type == ColumnType.STRING && entry.get(columnIndex).compareTo(condition.value) > 0)){
                    return true;
                }
                break;
            case "<":
                if((type == ColumnType.INTEGER && Integer.parseInt(entry.get(columnIndex)) < Integer.parseInt(condition.value))
                        || (type == ColumnType.FLOAT && Float.parseFloat(entry.get(columnIndex)) < Float.parseFloat(condition.value))
                        || (type == ColumnType.STRING && entry.get(columnIndex).compareTo(condition.value) < 0)){
                    return true;
                }
                break;
            case ">=":
                if((type == ColumnType.INTEGER && Integer.parseInt(entry.get(columnIndex)) >= Integer.parseInt(condition.value))
                        || (type == ColumnType.FLOAT && Float.parseFloat(entry.get(columnIndex)) >= Float.parseFloat(condition.value))
                        || (type == ColumnType.STRING && entry.get(columnIndex).compareTo(condition.value) >= 0)){
                    return true;
                }
                break;
            case "<=":
                if((type == ColumnType.INTEGER && Integer.parseInt(entry.get(columnIndex)) <= Integer.parseInt(condition.value))
                        || (type == ColumnType.FLOAT && Float.parseFloat(entry.get(columnIndex)) <= Float.parseFloat(condition.value))
                        || (type == ColumnType.STRING && entry.get(columnIndex).compareTo(condition.value) <= 0)){
                    return true;
                }
                break;
            case "!=":
                if(!entry.get(columnIndex).equals(condition.value)){
                    return true;
                }
                break;
            case "LIKE":
                if(entry.get(columnIndex).contains(condition.value)){
                    return true;
                }
                break;
        }
        return false;
    }

    public void writeToFile() throws IOException {
        File file = new File(tablePath);
        if(!file.exists()){
            throw new IOException(tablePath+" file does not exist");
        }

        BufferedWriter writer = Files.newBufferedWriter(file.toPath());

        StringBuilder contents = new StringBuilder();
        for(int i = 0; i < columnNames.size(); i++){
            contents.append(columnNames.get(i));
            if(i < columnNames.size()-1){
                contents.append("\t");
            }
        }
        if(!columnTypes.isEmpty()){
            contents.append("\n");
        }

        for(int i = 0; i < columnTypes.size(); i++){
            contents.append(columnTypes.get(i).toString());
            if(i < columnTypes.size()-1){
                contents.append("\t");
            }
        }

        for(ArrayList<String> entry : entries){
            contents.append("\n");
            for(int i = 0; i < entry.size(); i++){
                contents.append(entry.get(i));
                if(i < entry.size()-1){
                    contents.append("\t");
                }
            }
        }

        writer.write(contents.toString());
        writer.close();
    }
}
