package ru.kpfu.itis.elasticloader.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class KafkaConsumer {
    private final RestHighLevelClient elasticsearchRestClient;

    @Value("${application.config.index-name}")
    private String index;

    @KafkaListener(topics = "${spring.kafka.topic.topic-name}")
    public void consume(List<ConsumerRecord<String, String>> records, Acknowledgment acknowledgment) {
        try {
            log.info("Offset: " + records.get(0).offset());
            BulkRequest request = new BulkRequest();
            records.forEach(record -> {
                request.add(new IndexRequest(index)
                        .id(record.key())
                        .source(record.value(), XContentType.JSON));
            });
            log.info("Bulk start");
            elasticsearchRestClient.bulk(request, RequestOptions.DEFAULT);
            log.info("Bulk end");
            acknowledgment.acknowledge();
        } catch (IOException e) {
            log.error("Error on processing: ", e);
            acknowledgment.nack(0, 1000);
        }
    }
}
