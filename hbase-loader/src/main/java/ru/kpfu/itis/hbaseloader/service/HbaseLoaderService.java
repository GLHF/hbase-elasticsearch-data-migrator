package ru.kpfu.itis.hbaseloader.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.kpfu.itis.hbaseloader.config.PhoenixConnectionManager;
import ru.kpfu.itis.hbaseloader.util.DateUtil;

import javax.annotation.PostConstruct;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class HbaseLoaderService {
    private final PhoenixConnectionManager phoenixConnectionManager;
    private final QueryFormatterService queryFormatterService;
    private final KafkaProducerService kafkaProducerService;
    private final DateUtil dateUtil;

    @Value("${application.config.id-field}")
    private String id;
    @Value("${application.config.batch-size}")
    private int batchSize;
    @Value("${application.config.types}")
    private List<String> types;
    @Value("${application.config.fields}")
    private List<String> fields;

    private Map<String, String> fieldsToTypes;

    @PostConstruct
    public void init() {
        if (types.size() != fields.size())
            throw new IllegalArgumentException("Number of types not equal number of fields");
        fieldsToTypes = new HashMap<>(types.size() * 2);
        for (int i = 0; i < types.size(); i++) {
            fieldsToTypes.put(fields.get(i), types.get(i));
        }
    }

    @PostConstruct
    public void loadEntries() throws SQLException {
        String select = queryFormatterService.formSelect();

        try (Connection connection = phoenixConnectionManager.getConnection()) {
            Statement statement = connection.createStatement();
            //на случай если была аварийная ситуация и не удалили курсор
            statement.execute("CLOSE entries_cursor");
            statement.execute(select);
            try {
                statement.execute("OPEN entries_cursor");

                int it = 0;
                while (true) {
                    log.info(String.format("Batch loading start, iteration %d", it));
                    boolean empty = true;
                    try (ResultSet resultSet =
                                 statement.executeQuery(String.format("FETCH NEXT %d ROWS FROM entries_cursor", batchSize))) {
                        while (resultSet.next()) {
                            if (empty) empty = false;
                            try {
                                JSONObject obj = readRow(resultSet);
                                kafkaProducerService.sendCisesMessage(obj.getString(id), obj.toString());
                            } catch (Exception e) {
                                log.error(String.format("Id: %s", resultSet.getString(id)), e);
                                continue;
                            }
                        }
                        if (empty) break;
                    } catch (Exception e) {
                        log.error("Error", e);
                        continue;
                    }
                    it++;
                }
                statement.execute("CLOSE entries_cursor");
            } catch (Exception e) {
                log.error("Execution exception", e);
                statement.execute("CLOSE entries_cursor");
                throw new SQLException(e);
            }
        }
    }

    private JSONObject readRow(ResultSet rs) throws SQLException{
        int numColumns = rs.getMetaData().getColumnCount();
        ResultSetMetaData rsmd = rs.getMetaData();
        JSONObject obj = new JSONObject();

        for(int i = 1; i <= numColumns; i++) {
            String column_name = rsmd.getColumnName(i);
            String type = fieldsToTypes.get(column_name);

            switch(type) {
                case "decimal":
                    obj.put(column_name, rs.getInt(column_name));       break;
                case "boolean":
                    obj.put(column_name, rs.getBoolean(column_name));   break;
                case "double":
                    obj.put(column_name, rs.getDouble(column_name));    break;
                case "varchar":
                    obj.put(column_name, rs.getString(column_name));    break;
                case "date":
                    obj.put(column_name, dateUtil.parseDate(rs.getString(column_name)));      break;
                case "timestamp":
                    obj.put(column_name, rs.getTimestamp(column_name)); break;
                default:
                    obj.put(column_name, rs.getObject(column_name));    break;
            }
        }
        return obj;
    }
}
