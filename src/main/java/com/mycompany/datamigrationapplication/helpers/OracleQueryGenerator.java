package com.mycompany.datamigrationapplication.helpers;

import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;

public class OracleQueryGenerator implements QueryGenerator {
    
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
        StringBuffer sql = new StringBuffer();
        try{
            if(checkValidColumnName(tableName, dbmd.getSQLKeywords().split(","))){
                sql.append("CREATE TABLE ").append(tableName).append(" (");
            }else{
                sql.append("CREATE TABLE ").append(tableName).append(EXTRA_CHAR).append(" (");
            }
            int columnCount = rsmd.getColumnCount();
            for(int i=1; i <= columnCount; i++){
//                rsmd.isAutoIncrement(i);  // Oracle doesn't support Auto Increment column, as it uses the concept of sequence
                // Set Column name
                if(checkValidColumnName(rsmd.getColumnName(i), dbmd.getSQLKeywords().split(","))){
                    sql.append(rsmd.getColumnName(i)).append(" ");
                }else{
                    System.out.println("Column name Same as Data Type : " + rsmd.getColumnName(i));
                    sql.append(rsmd.getColumnName(i)).append(EXTRA_CHAR).append(" ");
                }
                // Set Column Data type
                sql.append(getDataType(rsmd.getColumnType(i), rsmd.getPrecision(i), rsmd.getScale(i)));
                // Set Constraint
                if(rsmd.isNullable(i) == ResultSetMetaData.columnNoNulls && !Arrays.asList(primaryKeyColumns).contains(rsmd.getColumnName(i)))
                    sql.append(" NOT NULL");
                if(i != columnCount || primaryKeyColumns.length > 0)
                    sql.append(",");
            }
            if(primaryKeyColumns.length > 0){
//                public static String join(CharSequence delimiter, CharSequence... elements)
                sql.append("CONSTRAINT PK_").append(tableName).append(" PRIMARY KEY(").append(String.join(",", primaryKeyColumns)).append(")");
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
        int maxCharSize = 2000;
        int maxVarcharSize = 4000;
        int maxPrecision = 38;
        int maxScale = 127;
        if(precision < 1){
            precision = 5;
        }
        if(scale < 0){
            scale = 0;
        }
        switch(columnType){
            case Types.BIT:
                return "CHAR(1)";
            case Types.TINYINT:
                return "NUMBER(" + (precision < maxPrecision ? precision : maxPrecision) + ")";
            case Types.SMALLINT:
                return "NUMBER(" + (precision < maxPrecision ? precision : maxPrecision) + ")";
            case Types.INTEGER:
                return "NUMBER(" + (precision < maxPrecision ? precision : maxPrecision) + ")";
            case Types.BIGINT:
                return "NUMBER(" + (precision < maxPrecision ? precision : maxPrecision) + ")";
            case Types.FLOAT:
//                return "NUMBER(" + (precision < maxPrecision ? precision : maxPrecision) + "," + (scale < maxScale ? scale : maxScale) + ")";
                return "FLOAT";
            case Types.REAL:
//                return "NUMBER(" + (precision < maxPrecision ? precision : maxPrecision) + "," + (scale < maxScale ? scale : maxScale) + ")";
                return "FLOAT";
            case Types.DOUBLE:
//                return "DOUBLE";  // Not Supported by Oracle
//                return "NUMBER(" + (precision < maxPrecision ? precision : maxPrecision) + "," + (scale < maxScale ? scale : maxScale) + ")";
                return "FLOAT";
            case Types.NUMERIC:
                return "NUMBER(" + (precision < maxPrecision ? precision : maxPrecision) + "," + (scale < maxScale ? scale : maxScale) + ")";
            case Types.DECIMAL:
                return "NUMBER(" + (precision < maxPrecision ? precision : maxPrecision) + "," + (scale < maxScale ? scale : maxScale) + ")";
            case Types.CHAR:
                return "CHAR(" + (precision < maxCharSize ? precision : maxCharSize) + ")";
            case Types.VARCHAR:
                return "VARCHAR2(" + (precision < maxVarcharSize ? precision : maxVarcharSize) + ")";
            case Types.LONGVARCHAR:
                return "LONG";
            case Types.DATE:
                return "DATE";
            case Types.TIME:
                return "DATE";
            case Types.TIMESTAMP:
                return "TIMESTAMP";
            case Types.BINARY:
                return "BLOB";
            case Types.VARBINARY:
                return "BLOB";
            case Types.LONGVARBINARY:
                return "BLOB";
            case Types.NULL:
                return "VARCHAR2(" + varcharSize + ")";
            case Types.OTHER:
                return "VARCHAR2(" + varcharSize + ")";
            case Types.JAVA_OBJECT:
                return "VARCHAR2(" + varcharSize + ")";
            case Types.DISTINCT:
                return "VARCHAR2(" + varcharSize + ")";
            case Types.STRUCT:
                return "VARCHAR2(" + varcharSize + ")";
            case Types.ARRAY:
                return "VARCHAR2(" + varcharSize + ")";
            case Types.BLOB:
                return "BLOB";
            case Types.CLOB:
                return "CLOB";
            case Types.REF:
                return "VARCHAR2(" + varcharSize + ")";
            case Types.DATALINK:
                return "VARCHAR2(" + varcharSize + ")";
            case Types.BOOLEAN:
                return "CHAR(1)";
            case Types.ROWID:
                return "VARCHAR2(" + varcharSize + ")";
            case Types.NCHAR:
                return "NCHAR(" + (precision < maxCharSize ? precision : maxCharSize) + ")";
            case Types.NVARCHAR:
//                return "VARCHAR(" + precision + ") CHARACTER SET utf8";
//                return "NATIONAL VARCHAR(" + precision + ")";
//                return "NVARCHAR(" + precision + ")";
//                return "NATIONAL CHARACTER VARYING(" + precision + ")";
//                return "NVARCHAR2(" + precision + ")";
                return "NVARCHAR2(" + (precision < maxVarcharSize ? precision : maxVarcharSize) + ")";
            case Types.LONGNVARCHAR:
                return "LONG";
            case Types.NCLOB:
                return "NCLOB";
            case Types.SQLXML:
                return "VARCHAR2(" + varcharSize + ")";
            case Types.REF_CURSOR:
                return "VARCHAR2(" + varcharSize + ")";
            case Types.TIME_WITH_TIMEZONE:
                return "TIMESTAMP";
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return "TIMESTAMP WITH TIME ZONE";                   
            default:
                return "VARCHAR2(" + varcharSize + ")";
        }
    }

    @Override
    public String generateDropTableQuery(DatabaseMetaData dbmd, String tableName) {
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
