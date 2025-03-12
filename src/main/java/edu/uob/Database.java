package edu.uob;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

public class Database {
    String databaseName;
    ArrayList<Table> tables;

    private Database() {
        databaseName = null;
        tables = new ArrayList<Table>();
    }

    private static final Database db = new Database();

    public static Database getInstance(){
        return db;
    }

    public void setUseDatabase(String dbName) throws IOException {
        // check if there is folder name database in the directory of folder
        String databasePath = Paths.get("databases"+ File.separator+dbName).toAbsolutePath().toString();
        File dbfolder = new File(databasePath);
        if(!dbfolder.exists()){
            throw new IOException("Database: '" +databaseName+ "' does not exist");
        }
        this.databaseName = dbName;

        // Go this directory get all the database files and load them
        Path path = Paths.get(databasePath);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
            for (Path filePath : stream) {
                tables.add(new Table(filePath.toString(), Optional.empty()));
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void  createDatabase(String dbName) throws IOException {
        String databasePath = Paths.get("databases"+File.separator+dbName).toAbsolutePath().toString();
        File dbfolder = new File(databasePath);
        if(dbfolder.exists()) {
            throw new IOException("Database: '" + databaseName + "' already exists");
        }
        Path path = Paths.get(databasePath);
        Files.createDirectory(path);
    }

    public void createTable(String tableName, Optional<ArrayList<String>> columnNames) throws Exception {
        // go to database path and create file there with name provided in table name parameter
        String databasePath = Paths.get("databases"+ File.separator+databaseName).toAbsolutePath().toString();
        File dbfolder = new File(databasePath);
        if(!dbfolder.exists()){
            throw new IOException("Database: '" +databaseName+ "' does not exist");
        }
        Path path = Paths.get(databasePath+File.separator+tableName);
        File file = new File(path.toString());
        if(file.exists()){
            throw new IOException("Table: '" + tableName + "' already exists");
        }
        file.createNewFile();
        tables.add(new Table(file.getAbsolutePath(), columnNames));
    }

    public void dropDatabase(String dbName) throws IOException {
        // first check if the database exists if not then throw exception
        String databasePath = Paths.get("databases"+ File.separator+dbName).toAbsolutePath().toString();
        File dbfolder = new File(databasePath);
        if(!dbfolder.exists()){
            throw new IOException("Invalid database name. Directory does not exist");
        }
        // If the current database is the one to be deleted. empty the current member objects and then delete the database
        if(dbName.equals(databaseName)){
            this.databaseName = null;
            this.tables.clear();
        }
        // if current database is not the one to be deleted, delete it from the databases folder after deleting all its contents
        String[] files = dbfolder.list();
        if(files != null){
            for (String file : files) {
                File currentFile = new File(dbfolder.getPath(), file);
                currentFile.delete();
            }
        }
        if(dbfolder.delete()){
            System.out.println("Database: '" + databaseName + "' deleted successfully");
        }
    }

    public void addInTable(String tableName, String columnName) throws IOException {
        // find the table first in the list of table then let it add the column
        int index = -1;

        for(int i=0; i<tables.size(); i++){
            if(tables.get(i).getName().equals(tableName)){
                index = i;
                break;
            }
        }

        if(index == -1){
            throw new IOException("Table: '" + tableName + "' does not exist");
        }

        tables.get(index).insertColumn(columnName);
    }

    public void dropInTable(String tableName,  String columnName) throws IOException {
        // find the table first in the list of table then let it delete the column
        int index = -1;

        for(int i=0; i<tables.size(); i++){
            if(tables.get(i).getName().equals(tableName)){
                index = i;
                break;
            }
        }

        if(index == -1){
            throw new IOException("Table: '" + tableName + "' does not exist");
        }

        tables.get(index).removeColumn(columnName);
    }

    public void dropTable(String tableName) throws IOException {
        // find the table name in tables
        String tablePath = Paths.get("databases"+ File.separator+databaseName+File.separator+tableName).toAbsolutePath().toString();
        File table = new File(tablePath);
        if(!table.exists()){
            throw new IOException("Database: '" +tableName+ "' does not exist");
        }

        int index = -1;
        for(int i=0; i<tables.size(); i++){
            Table t = tables.get(i);
            if(t.getName().equals(tableName)){
                index = i;
                t = null;
                tables.remove(index);
                break;
            }
        }
    }

    public void insertToTable(String tableName, ArrayList<String> values, ArrayList<Table.ColumnType>types) throws IOException {
        // check if the table exists in tables. If it does not, throw error
        int index = -1;
        for(int i = 0; i < tables.size(); i++){
            if(tables.get(i).getName().equals(tableName)){
                index = i;
                break;
            }
        }
        if(index == -1){
            throw new IOException("Table: '" + tableName + "' does not exist");
        }
        // Call the insert method of table in with values and types
        tables.get(index).insertEntry(values, types);
    }

    public String queryTable(String tableName, ArrayList<String> attributeNames, Optional<OuterCondition> condition) throws IOException {
        // check if the table exists in tables. If it does not, throw error
        int index = -1;
        for(int i = 0; i < tables.size(); i++){
            if(tables.get(i).getName().equals(tableName)){
                index = i;
                break;
            }
        }
        if(index == -1){
            throw new IOException("Table: '" + tableName + "' does not exist");
        }
        // call the query method on this table
        // if condition is not present then we should not filter based on condition
        return tables.get(index).queryTable(attributeNames, condition);
    }

    public void updateTable(String tableName, ArrayList<Triplet<String,String,Token.TokenType>>values, OuterCondition condition) throws IOException {
        // see if the table exists.
        int index = -1;
        for(int i = 0; i < tables.size(); i++){
            if(tables.get(i).getName().equals(tableName)){
                index = i;
                break;
            }
        }
        if(index == -1){
            throw new IOException("Table: '" + tableName + "' does not exist");
        }

        tables.get(index).updateEntries(values, condition);
    }

    public void deleteFromTable(String tableName, OuterCondition condition) throws IOException {
        // see if the table exists.
        int index = -1;
        for(int i = 0; i < tables.size(); i++){
            if(tables.get(i).getName().equals(tableName)){
                index = i;
                break;
            }
        }
        if(index == -1){
            throw new IOException("Table: '" + tableName + "' does not exist");
        }

        tables.get(index).deleteEntries(condition);
    }

    public String joinTable(String firstTableName, String secondTableName, String firstAttributeName, String secondAttributeName) throws IOException {
        // does first and second table exist
        Table firstTableObj = null;
        Table secondTableObj = null;
        for(Table table : tables){
            if(firstTableObj == null && table.getName().equals(firstTableName)){
                firstTableObj = table;
            }
            if(secondTableObj == null && table.getName().equals(secondTableName)){
                secondTableObj = table;
            }
            if(firstTableObj != null && secondTableObj != null){
                break;
            }
        }

        if(firstTableObj == null){
            throw new IOException("Table :'"+ firstTableName+ "does not exist");
        }
        if(secondTableObj == null){
            throw new IOException("Table :'"+ secondTableName+ "does not exist");
        }

        // does first  and second attribute exist in first table
        int firstAttributeIndex = firstTableObj.checkColumnName(firstAttributeName);
        int secondAttributeIndex = secondTableObj.checkColumnName(secondAttributeName);

        if(firstAttributeIndex < 0){
            throw new IOException("Column :'"+ firstAttributeName+ "'does not exist in table :'" +firstTableName);
        }
        if(secondAttributeIndex < 0){
            throw new IOException("Column :'"+ secondAttributeName+ "'does not exist in table :'" +secondTableName);
        }

        // does their types match
        Table.ColumnType firstColumntype = firstTableObj.checkColumnType(firstAttributeName);
        Table.ColumnType secondColumntype = secondTableObj.checkColumnType(secondAttributeName);

        if(firstColumntype != secondColumntype){
            throw new IOException("Attribute type mismatch");
        }

        int newId = 0;
        StringBuilder output = new StringBuilder();
        // Add column names in the output which are not common
        output.append("id").append("\t");
        // Add all from first table column except first and common
        for(int i = 1; i<firstTableObj.getColumnNames().size(); i++){
            if(i != firstAttributeIndex){
                output.append(firstTableName).append(".").append(firstTableObj.getColumnNames().get(i)).append("\t");
            }
        }
        // Add all from first table column except first and common
        for(int i = 1; i<secondTableObj.getColumnNames().size(); i++){
            if(i != secondAttributeIndex){
                output.append(secondTableName).append(".").append(secondTableObj.getColumnNames().get(i)).append("\t");
            }
        }

        output.append("\n");

        // iterate over each entry in first table
        for(ArrayList<String> firstTableEntry : firstTableObj.getEntries()){
            // find this entry in second table
            for(ArrayList<String> secondTableEntry : secondTableObj.getEntries()){
                String commonValue = firstTableEntry.get(firstAttributeIndex);
                String secondValue = secondTableEntry.get(secondAttributeIndex);
                if(commonValue.equals(secondValue)){
                    // Add columns from first table without the id and the common column
                    output.append(String.valueOf(++newId)).append("\t");
                    for(int i = 1; i < firstTableEntry.size(); i++){
                        if(i != firstAttributeIndex){
                            output.append(firstTableEntry.get(i)).append("\t");
                        }
                    }
                    // Add columns from the second table without the id and common column
                    for(int i = 1; i < secondTableEntry.size(); i++){
                        if(i != secondAttributeIndex){
                            output.append(secondTableEntry.get(i)).append("\t");
                        }
                    }
                    output.append("\n");
                }
            }
        }
        return output.toString();
    }
}
