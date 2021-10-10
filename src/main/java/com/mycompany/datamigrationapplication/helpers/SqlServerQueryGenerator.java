package com.mycompany.datamigrationapplication.helpers;

import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

public class SqlServerQueryGenerator implements QueryGenerator {
    
    public boolean checkValidColumnName(String columnName, String[] sqlKeywords){
        String name = columnName.toUpperCase();
        String[] keywords = new String[]{"USER", "ORDER"};
        for(String str : keywords){
            if(str.equals(name)){
                return false;
            }
        }
        for(String str : sqlKeywords){
            if(str.trim().equals(name)){
                return false;
            }
        }
        return true;
    }

    @Override
    public String generateCreateTableQuery(DatabaseMetaData dbmd, ResultSetMetaData rsmd, String tableName, String[] primaryKeyColumns) {
        boolean autoIncrementAdded = false;
        StringBuffer sql = new StringBuffer();
        try{
            if(checkValidColumnName(tableName, dbmd.getSQLKeywords().split(","))){
                sql.append("CREATE TABLE ").append(tableName).append(" (");
            }else{
                sql.append("CREATE TABLE ").append(tableName).append(EXTRA_CHAR).append(" (");
            }
            int columnCount = rsmd.getColumnCount();
            for(int i=1; i <= columnCount; i++){
                // Set Column name
                if(checkValidColumnName(rsmd.getColumnName(i), dbmd.getSQLKeywords().split(","))){
                    sql.append(rsmd.getColumnName(i)).append(" ");
                }else{
                    sql.append(rsmd.getColumnName(i)).append(EXTRA_CHAR).append(" ");
                }
                // Set Column Data type
                // Getting "TIMESTAMP" for "DATE" from Oracle ResultSet
                if(rsmd.getColumnTypeName(i).equals("DATE")){
                    sql.append(getDataType(Types.DATE, rsmd.getPrecision(i), rsmd.getScale(i)));
                }else{
                    sql.append(getDataType(rsmd.getColumnType(i), rsmd.getPrecision(i), rsmd.getScale(i)));
                }
                // Set Auto Increment
                if(rsmd.isAutoIncrement(i) && !autoIncrementAdded){
                    sql.append(" IDENTITY (1, 1)");
                    autoIncrementAdded = true;
                }
                // Set Constraint
                if(rsmd.isNullable(i) == ResultSetMetaData.columnNoNulls && !Arrays.asList(primaryKeyColumns).contains(rsmd.getColumnName(i)))
                    sql.append(" NOT NULL");
                if(i != columnCount || primaryKeyColumns.length > 0)
                    sql.append(",");
            }
            if(primaryKeyColumns.length > 0){
//                public static String join(CharSequence delimiter, CharSequence... elements)
                sql.append("CONSTRAINT PK_").append(tableName).append(" PRIMARY KEY (").append(String.join(",", primaryKeyColumns)).append(")");
            }
            sql.append(")");
            return sql.toString();
        }catch(SQLException ex){
            ex.printStackTrace();
            return null;
        }
    }
    
    private String getDataType(int columnType, int precision, int scale){
        int varcharSize = 200;
        int maxVarcharSize =  8000;
        int maxNVarcharSize =  4000;
        int maxBinarySize = 7500;
        int maxPrecision = 38;
        int maxScale = 15;
        if(precision < 1){
            precision = 5;
        }
        if(scale < 0){
            scale = 0;
        }
        switch(columnType){
            case Types.BIT:
                return "BIT";
            case Types.TINYINT:
                return "TINYINT";
            case Types.SMALLINT:
                return "SMALLINT";
            case Types.INTEGER:
                return "INT";
            case Types.BIGINT:
                return "BIGINT";
            case Types.FLOAT:
//                return "FLOAT";
                return "FLOAT(24)";
            case Types.REAL:
                return "REAL";
            case Types.DOUBLE:
//                return "FLOAT";
                return "FLOAT(53)";
            case Types.NUMERIC:
                return "NUMERIC(" + (precision < maxPrecision ? precision : maxPrecision) + "," + (scale < maxScale ? scale : maxScale) + ")";
            case Types.DECIMAL:
                return "DECIMAL(" + (precision < maxPrecision ? precision : maxPrecision) + "," + (scale < maxScale ? scale : maxScale) + ")";
            case Types.CHAR:
                return "CHAR(" + (precision < maxVarcharSize ? precision : maxVarcharSize) + ")";
            case Types.VARCHAR:
                return "VARCHAR(" + (precision < maxVarcharSize ? precision : maxVarcharSize) + ")";
            case Types.LONGVARCHAR:
                return "VARCHAR(MAX)";
            case Types.DATE:
                return "DATE";
            case Types.TIME:
                return "TIME";
            case Types.TIMESTAMP:
                // return "TIMESTAMP";  // Cannot insert an explicit value into a timestamp column (Timestamp also stored in binary)
                return "DATETIME";
            case Types.BINARY:
                return "BINARY(" + (precision < maxBinarySize ? precision : maxBinarySize) + ")";
            case Types.VARBINARY:
                return "VARBINARY(" + (precision < maxBinarySize ? precision : maxBinarySize) + ")";
            case Types.LONGVARBINARY:
                return "VARBINARY(MAX)";
            case Types.NULL:
                return "VARCHAR(" + varcharSize + ")";
            case Types.OTHER:
                return "VARCHAR(" + varcharSize + ")";
            case Types.JAVA_OBJECT:
                return "VARCHAR(" + varcharSize + ")";
            case Types.DISTINCT:
                return "VARCHAR(" + varcharSize + ")";
            case Types.STRUCT:
                return "VARCHAR(" + varcharSize + ")";
            case Types.ARRAY:
                return "VARCHAR(" + varcharSize + ")";
            case Types.BLOB:
//                return "IMAGE";
                return "VARBINARY(MAX)";
            case Types.CLOB:
//                return "IMAGE";
                return "VARBINARY(MAX)";
            case Types.REF:
                return "VARCHAR(" + varcharSize + ")";
            case Types.DATALINK:
                return "VARCHAR(" + varcharSize + ")";
            case Types.BOOLEAN:
                return "BIT";
            case Types.ROWID:
                return "VARCHAR(" + varcharSize + ")";
            case Types.NCHAR:
                return "NCHAR(" + (precision < maxNVarcharSize ? precision : maxNVarcharSize) + ")";
            case Types.NVARCHAR:
                return "NVARCHAR(" + (precision < maxNVarcharSize ? precision : maxNVarcharSize) + ")";
            case Types.LONGNVARCHAR:
                return "NVARCHAR(MAX)";
            case Types.NCLOB:
//                return "IMAGE";
                return "VARBINARY(MAX)";
            case Types.SQLXML:
                return "VARCHAR(" + varcharSize + ")";
            case Types.REF_CURSOR:
                return "VARCHAR(" + varcharSize + ")";
            case Types.TIME_WITH_TIMEZONE:
                return "TIME";
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return "TIMESTAMP";
            default:
                return "VARCHAR(8000)";
        }
    }

    @Override
    public String generateDropTableQuery(DatabaseMetaData dbmd, String tableName) {
//        Will only work in SQL Server 2016 and above
//        return String.format("DROP TABLE IF EXISTS %s", tableName);
        try{
            if(checkValidColumnName(tableName, dbmd.getSQLKeywords().split(","))){
                return String.format("DROP TABLE %s", tableName);
            }else{
                return String.format("DROP TABLE %s", (tableName + EXTRA_CHAR));
            }
        }
        catch(Exception ex){
            return String.format("DROP TABLE %s", tableName);
        }
    }
    
}
