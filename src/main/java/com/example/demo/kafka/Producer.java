package com.example.demo.kafka;

import com.example.demo.model.PriceTick;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;


import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class Producer {
    private static final Logger log = LoggerFactory.getLogger(Producer.class.getName());

    private final String topic;

    private final KafkaSender<Integer, PriceTick> sender;
    private final DateTimeFormatter dateFormat =  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Producer(
            @Value("${kafka.producer.bootstrapServer}") String bootstrapServer,
            @Value("${kafka.producer.topicName}") String topic
    ) {
        this.topic = topic;
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "sample-producer");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        SenderOptions<Integer, PriceTick> senderOptions = SenderOptions.create(props);

        sender = KafkaSender.create(senderOptions);
    }

    public void sendMessage(PriceTick priceTick) {
        sender.send(Mono.just(SenderRecord.create(new ProducerRecord<>(topic, 0, priceTick), priceTick)))
                .doOnError(e -> log.error("Send failed", e))
                .onErrorContinue((_, _) -> {})
                .subscribe();
    }

    private final Consumer<SenderResult<PriceTick>> func = r ->  {
        RecordMetadata metadata = r.recordMetadata();
        Instant timestamp = Instant.ofEpochMilli(metadata.timestamp());
        System.out.printf("Message %s sent successfully, topic-partition=%s-%d offset=%d timestamp=%s\n",
                r.correlationMetadata(),
                metadata.topic(),
                metadata.partition(),
                metadata.offset(),
                dateFormat.format(timestamp.atZone(ZoneId.systemDefault())));
    };

    public void close() {
        sender.close();
    }
}
