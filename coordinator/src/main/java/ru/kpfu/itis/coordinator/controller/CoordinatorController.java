package ru.kpfu.itis.coordinator.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@Slf4j
public class CoordinatorController {
    @Value("${application.config.ids-range}")
    private String range;
    @Value("${application.config.id-field}")
    private String idField;
    private static final String COMMA = ",";

    private List<String> wheres = new ArrayList<>();

    @PostConstruct
    public void init() {
        List<String> rangeString = Arrays.asList(range.split(COMMA));
        for (int i = 0; i < rangeString.size(); i++) {
            if (i == 0) {
                wheres.add(String.format(" WHERE \"%s\" < \'%s\' ",
                        idField, rangeString.get(i)));
                continue;
            }
            wheres.add(String.format(" WHERE \"%s\" >= \'%s\' AND \"%s\" < \'%s\' ",
                    idField, rangeString.get(i-1), idField, rangeString.get(i)));
            if (i == rangeString.size()-1) {
                wheres.add(String.format(" WHERE \"%s\" >= \'%s\' ",
                        idField, rangeString.get(i), idField, rangeString.get(i)));
            }
        }
        log.info(Arrays.toString(wheres.toArray()));
    }

    @GetMapping("/coordinate")
    public String coordinate() {
        synchronized (wheres) {
            if (wheres.size() == 0) return "null";
            String where = wheres.get(0);
            wheres.remove(0);
            return where;
        }
    }
}
