package com.jetbi.postgresftsapp.spring.stereotype.service;

import java.sql.SQLException;
import java.util.Map;

/**
 * Service to provide FTS search, index creation and re-indexing.
 *
 * @author Sergei.Emelianov on 19.03.2016.
 */
public interface FtsSearchService {

    String createTsvectorIndex(final Map<String,Object> request) throws SQLException;

    String dropTsvectorIndex(final String name) throws SQLException;

    /**
     * Assembles a SQL statement for FTS search and runs it to obtain the result records
     *
     * @param request map created as the resutl of JSON message deserialization
     * @return returns result of the search, it time and the SQL statement generated based on the request map
     * @throws SQLException
     */
    Map<String,Object> search(final Map<String,Object> request) throws SQLException;
}
