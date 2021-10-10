package com.mycompany.datamigrationapplication.exception;

public class DatabaseNotSupportedException extends Exception {
    
    public DatabaseNotSupportedException(String message){
        super(message);
    }
    
}
