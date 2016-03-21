package com.jetbi.postgresftsapp.spring.stereotype.service.controller;

import com.google.gson.Gson;
import com.jetbi.postgresftsapp.spring.Field;
import com.jetbi.postgresftsapp.spring.stereotype.service.FtsSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Controller for all REST services exposed by the application.
 *
 * @author by Sergei.Emelianov on 19.03.2016.
 */
@Controller
@RequestMapping("srv")
public class RestService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Gson gson;

    @Autowired
    private FtsSearchService ftsSearchService;

    /**
     * Service to be used as a proxy to retrieve results of the FTS search on a single table
     * within the configured Postgres database.
     *
     * @param body - message to be used to create FTS search statement. <br>
     *             Format Sample:<br>
     * <pre>
     *   {"table”:{
 *           "name”:"products”,
 *           "columns":[
 *               {"name”:”product_name”, "selectable”:true, "tsvectorinclude”:true },
 *               {"name”:”description”, "selectable”:false, "tsvectorinclude”:true},
 *               {"name”:”product_id”, "selectable”:true, "tsvectorinclude”:false}
 *           ],
 *           "query”:”i am looking for good product which uses GSM”,
 *           "configuration”:”english”
 *       }
     * </pre>
     * @return - result of the FTS search or error. <br>
     * Sample of the successfull response:
     * <pre>
    {"result":{
        "sqlstatement":"SELECT id, product_name FROM products WHERE to_tsvector(\u0027english\u0027,coalesce(description,\u0027\u0027) || \u0027 \u0027 || co
    alesce(product_name,\u0027\u0027)) @@ plainto_tsquery(\u0027need to connect\u0027)",
        "records":[{  "id":33,"product_name":"Genesys Connect for Service Cloud" }],
        "javatimemls":1560
    },
    "error":false
    }
     * </pre>
     *
     * Sample of the failed request
     * <pre>
    {
    "error_message":"StatementCallback; bad SQL grammar [SELECT idb, product_name FROM products WHERE to_tsvec.....)]; nested exception is org.postgresql.util.PSQLException: ERROR: column \"idb\" does not exist\n  Position: 8",
    "error":true
    }
     * </pre>
     */
    @RequestMapping(value = "search", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String search(@RequestBody String body) {
        Map<String,Object> searchRequest = gson.fromJson(body, Map.class);
        Map<String,Object> responseMap = new HashMap<String,Object>();
        try {
            Map<String,Object> searchResult = ftsSearchService.search(searchRequest);
            responseMap.put(Field.RESULT, searchResult);
            responseMap.put(Field.ERROR,false);
        } catch (Exception e) {
            log.error("search error: {}",e.getMessage());
            responseMap.put(Field.ERROR,true);
            responseMap.put(Field.ERROR_MSG,e.getMessage());
        }
        return gson.toJson(responseMap);
    }


    /**
     * Service to create ts_vector index
     *
     * @param body JSON string with the instructions for index creation
     *             <pre>
     *             {"name":"idx_prddescr",
     *             "table":{
     *                     "name":"products",
     *                     "columns":[
     *                              {"name":"description"},
     *                              {"name":"product_name"}
     *                              ]}}
     *             </pre>
     * @return Status for current operation
     */
    @RequestMapping(value = "index", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String createIndex(@RequestBody String body) {
        Map<String,Object> request = gson.fromJson(body, Map.class);
        Map<String,Object> responseMap = new HashMap<>(0);
        try {
            String resp = ftsSearchService.createTsvectorIndex(request);
            responseMap.put(Field.RESULT, "created");
            responseMap.put(Field.STATEMENT, resp);
            responseMap.put(Field.ERROR,false);
        } catch (Exception e) {
            log.error("failed index creation {}",e);
            log.error("index create error: {}",e.getMessage());
            responseMap.put(Field.ERROR,true);
            responseMap.put(Field.ERROR_MSG,e.getMessage());
        }
        return gson.toJson(responseMap);
    }

    /**
     * Service to drop an index
     * @param body - JSON string with the name of index specified
     *             <pre>
     *             {"name":"idx_prddescr"}
     *             </pre>
     * @return JSON string with the status of current operation
     */
    @RequestMapping(value = "index", method = RequestMethod.DELETE, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String dropIndex(@RequestBody String body) {
        Map<String,Object> request = gson.fromJson(body, Map.class);
        Map<String,Object> responseMap = new HashMap<>(0);
        try {
            String resp = ftsSearchService.dropTsvectorIndex((String)request.get(Field.NAME));
            responseMap.put(Field.RESULT, "dropped");
            responseMap.put(Field.STATEMENT, resp);
            responseMap.put(Field.ERROR,false);
        } catch (Exception e) {
            log.error("failed index delete {}",e);
            log.error("index delete error: {}",e.getMessage());
            responseMap.put(Field.ERROR,true);
            responseMap.put(Field.ERROR_MSG,e.getMessage());
        }

        return gson.toJson(responseMap);
    }
}
