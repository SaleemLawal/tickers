package com.example.demo.kafka;

import com.example.demo.model.PriceTick;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
@Slf4j
public class KafkaConsumer {

    private final String topic;

    private final ReceiverOptions<Integer, PriceTick> receiverOptions;

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Getter
    private final Sinks.Many<@NotNull PriceTick> sink = Sinks.many().multicast().directBestEffort();

    public KafkaConsumer(
            @Value("${kafka.bootstrapServer}") String bootstrapServer,
            @Value("${kafka.topicName}") String topic) {
        this.topic = topic;
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, "sample-consumer");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "sample-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JacksonJsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(JacksonJsonDeserializer.VALUE_DEFAULT_TYPE, PriceTick.class.getName());
        receiverOptions = ReceiverOptions.create(props);
    }

    // @PostConstruct
    public void startConsuming() {
        log.info("Starting consumer");
        consumeMessages().subscribe();
    }

    public Flux<@NotNull ReceiverRecord<Integer, PriceTick>> consumeMessages() {

        ReceiverOptions<Integer, PriceTick> options = receiverOptions.subscription(Collections.singleton(topic))
                .addAssignListener(partitions -> log.debug("onPartitionsAssigned {}", partitions))
                .addRevokeListener(partitions -> log.debug("onPartitionsRevoked {}", partitions));
        Flux<@NotNull ReceiverRecord<Integer, PriceTick>> kafkaFlux = KafkaReceiver.create(options).receive();
        return kafkaFlux
                .doOnNext(v -> sink.tryEmitNext(v.value()))
                .doOnNext(func)
                .onErrorContinue((e, _) -> log.error("Send failed", e));
    }

    private final Consumer<ReceiverRecord<Integer, PriceTick>> func = record -> {
        ReceiverOffset offset = record.receiverOffset();
        Instant timestamp = Instant.ofEpochMilli(record.timestamp());
        log.debug("Received message: topic-partition={} offset={} timestamp={} key={} value={}",
                offset.topicPartition(),
                offset.offset(),
                dateFormat.format(timestamp.atZone(ZoneId.systemDefault())),
                record.key(),
                record.value());
        offset.acknowledge();
    };

}
