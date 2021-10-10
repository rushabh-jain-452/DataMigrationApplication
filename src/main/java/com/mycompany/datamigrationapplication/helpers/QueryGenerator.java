package com.mycompany.datamigrationapplication.helpers;

import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;

public interface QueryGenerator {
    
    String EXTRA_CHAR = "A";
    
    String generateCreateTableQuery(DatabaseMetaData dbmd, ResultSetMetaData rsmd, String tableName, String[] primaryKeyColumns);
    
    String generateDropTableQuery(DatabaseMetaData dbmd, String tableName);
    
    boolean checkValidColumnName(String columnName, String[] sqlKeywords);
    
}
