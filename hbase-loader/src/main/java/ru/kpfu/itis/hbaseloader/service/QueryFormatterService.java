package ru.kpfu.itis.hbaseloader.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@RequiredArgsConstructor
@Service
@Slf4j
public class QueryFormatterService {
    @Value("${application.config.table-name}")
    private String tableName;
    @Value("${application.config.fields}")
    private String fieldNames;
    @Value("${application.config.query}")
    private String query;

    private static final String SELECT = "DECLARE entries_cursor CURSOR FOR SELECT %s FROM %s %s";
    public String formSelect() {
        String fields = Arrays.stream(fieldNames.split(","))
                .map(field -> "\"" + field + "\"")
                .reduce((field1, field2) -> field1.concat(",".concat(field2))).get();
        return String.format(SELECT, fields, tableName, query);
    }
}
