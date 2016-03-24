package com.jetbi.postgresftsapp.spring.stereotype.service;

import com.google.gson.Gson;
import com.jetbi.postgresftsapp.spring.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author Sergei.Emelianov on 19.03.2016.
 */
@Service
public class PostgresFtsSearchService implements FtsSearchService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Gson gson;

    @Override
    public String createTsvectorIndex(final Map<String, Object> request) throws SQLException {
        StringBuilder sb = new StringBuilder();
        String ftsConfig = readFtsConfigurationParam(request);
        Map<String,Object> tableDescr = (Map<String,Object>)request.get(Field.TABLE);
        sb.append("CREATE INDEX ").append(request.get(Field.NAME)).append(" ON ")
                .append(tableDescr.get(Field.NAME)).append(" USING ").append(readTsvectorType(request))
                .append(" (to_tsvector('").append(readFtsConfigurationParam(request)).append("',")
                .append(makeToTsvectorStatement((List<Map<String, Object>>) tableDescr.get(Field.COLUMNS))).append("))");
        log.debug("creating index via SQL: {}", sb.toString());
        jdbcTemplate.execute(sb.toString());
        return sb.toString();
    }

    @Override
    @Async
    public Future<String> createTsvectorIndexAsync(Map<String, Object> request) throws SQLException {
        log.debug("async procedure for index begins");
        String result = createTsvectorIndex(request);
        log.debug("result returned");
        return new AsyncResult<String>(result);
    }

    /**
     *
     * @param columns
     * @return
     */
    private String makeToTsvectorStatement(final List<Map<String, Object>> columns) {
        String tsvect = columns.stream()
                .map(c -> "coalesce(" + (String) c.get(Field.NAME) + ",'')")
                .collect(Collectors.joining(" || ' ' || "));
        return tsvect;

    }

    /**
     * Reads ts vector type value
     *
     * @param request
     * @return string name of the type, or GIN
     */
    private String readTsvectorType(Map<String, Object> request) {
        return request.containsKey(Field.TYPE) ? (String)request.get(Field.TYPE) : "GIN";
    }

    @Override
    public String dropTsvectorIndex(String name) throws SQLException {
        StringBuilder sb = new StringBuilder("DROP INDEX ");
        sb.append(name);
        jdbcTemplate.execute(sb.toString());
        return sb.toString();
    }

    @Override
    public List<String> checkTsvectorIndex(String name) throws SQLException {
        StringBuilder sb = new StringBuilder("SELECT (tablename || '.' || indexname) AS location FROM pg_indexes WHERE indexname = '");
        sb.append(name).append("'");
        List result = jdbcTemplate.queryForList(sb.toString());
        return result;
    }

    @Override
    public Map<String,Object> search(final Map<String,Object> request) throws SQLException {
        StringBuilder sb = new StringBuilder();
        String ftsConfig = readFtsConfigurationParam(request);

        Map<String,Object> tableDescr = (Map<String,Object>)request.get(Field.TABLE);

        List<Map<String,Object>> columns = (List<Map<String,Object>>)tableDescr.get(Field.COLUMNS);

        String selectable = columns.stream()
                .filter(c -> ((Boolean) c.get(Field.SELECTABLE)).equals(Boolean.TRUE))
                .map(c -> (String) c.get(Field.NAME))
                .collect(Collectors.joining(", "));

        String tsvect = columns.stream()
                .filter(c -> ((Boolean) c.get(Field.TSV)).equals(Boolean.TRUE))
                .map(c -> "coalesce(" + (String) c.get(Field.NAME) + ",'')")
                .collect(Collectors.joining(" || ' ' || "));

        sb.append("SELECT ").append(selectable).append(" FROM ")
                .append(tableDescr.get(Field.NAME))
                .append(" WHERE to_tsvector('").append(ftsConfig).append("',").append(tsvect).append(") ")
                .append("@@ ")
                .append("plainto_tsquery('")
                .append(request.get(Field.QUERY))
                .append("')");

        log.debug("search SQL statement: {}", sb.toString());

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("fts search");
        //executing FTS search
        List result = jdbcTemplate.queryForList(sb.toString());
        stopWatch.stop();
        Map<String,Object> resultMap = new HashMap<String,Object>();
        resultMap.put(Field.STATEMENT, sb.toString());
        resultMap.put(Field.STAT, stopWatch.getTaskInfo()[0].getTimeMillis());
        resultMap.put(Field.RECORDS,result);

        return resultMap;
    }

    /**
     * reads FTS configuration parameter value from the request
     * @param request JSON deserialized into Map
     * @return string name of the configuration or <i>english</i>
     */
    private String readFtsConfigurationParam(Map<String, Object> request) {
        return request.containsKey(Field.CONFIGURATION) ? (String)request.get(Field.CONFIGURATION) : "english";
    }
}
