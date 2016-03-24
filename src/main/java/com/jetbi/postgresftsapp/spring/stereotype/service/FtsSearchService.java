package com.jetbi.postgresftsapp.spring.stereotype.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Service to provide FTS search, index creation and re-indexing.
 *
 * @author Sergei.Emelianov on 19.03.2016.
 */
public interface FtsSearchService {

    /**
     * Assembles SQL statement to create index and executes the statement
     *
     * @param request
     * @return
     * @throws SQLException
     */
    String createTsvectorIndex(final Map<String,Object> request) throws SQLException;

    /**
     * Executes index create statement asynchronously
     *
     * @param request
     * @return
     * @throws SQLException
     */
    Future<String> createTsvectorIndexAsync(Map<String, Object> request) throws SQLException;

    /**
     * Drops index by name
     *
     * @param name
     * @return
     * @throws SQLException
     */
    String dropTsvectorIndex(final String name) throws SQLException;

    /**
     * Checks if index exists
     *
     * @param name - name of the index to check on
     * @return
     * @throws SQLException
     */
    List<String> checkTsvectorIndex(final String name) throws SQLException;

    /**
     * Assembles a SQL statement for FTS search and runs it to obtain the result records
     *
     * @param request map created as the resutl of JSON message deserialization
     * @return returns result of the search, it time and the SQL statement generated based on the request map
     * @throws SQLException
     */
    Map<String,Object> search(final Map<String,Object> request) throws SQLException;

}
