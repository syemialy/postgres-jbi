package com.jetbi.postgresftsapp.spring;

/**
 * This interface describes communication protocol for the application REST services
 *
 * @author Sergei.Emelianov on 20.03.2016.
 */
public interface Field {
    //COMMON
    //Search Request message format
    String TABLE = "table";
    String COLUMNS = "columns";
    String NAME = "name";
    String SELECTABLE = "selectable";
    String TSV = "tsinclude";
    String QUERY = "query";
    String CONFIGURATION = "configuration";
    String LIMIT = "limit";
    String OFFSET = "offset";

    //Search Response message format
    String STATEMENT = "sqlstatement" ;
    String STAT = "javatimemls";
    String RECORDS = "records";
    String RESULT = "result";
    String ERROR = "error";
    String ERROR_MSG = "error_message";

    //Create index message format
    String TYPE = "type";

    //Async create index response
    String MESSAGE = "message";
}
