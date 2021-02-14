package ru.kpfu.itis.hbaseloader.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DateUtil {

    @Value("${application.config.date-formats}")
    private String dateFormats;
    private Set<DateTimeFormatter> formatterSet;
    private static final String COMMA = ";";

    @PostConstruct
    public void init() {
        String[] formats = dateFormats.split(COMMA);
        formatterSet = Arrays.stream(formats)
                .map(DateTimeFormatter::ofPattern)
                .collect(Collectors.toSet());
    }


    public Long parseDate(String date) {
        if (date == null) return null;
        for (DateTimeFormatter dateTimeFormatter : formatterSet) {
            try {
                return LocalDateTime.parse(date, dateTimeFormatter).toInstant(ZoneOffset.UTC).toEpochMilli();
            } catch (Exception e) {
                continue;
            }
        }
        throw new IllegalArgumentException("Not a valid date format");
    }
}
