package ru.kpfu.itis.hbaseloader.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class KafkaProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    @Value("${spring.kafka.topic.entries-topic}")
    private String entriesTopic;

    public void sendCisesMessage(String id, String entry) {
        kafkaTemplate.send(entriesTopic, id, entry);
    }
}
