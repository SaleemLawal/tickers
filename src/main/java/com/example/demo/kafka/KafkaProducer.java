package com.example.demo.kafka;

import com.example.demo.model.PriceTick;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
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
@Slf4j
public class KafkaProducer {

  private final String topic;

  private final KafkaSender<Integer, PriceTick> sender;
  private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public KafkaProducer(
      @Value("${kafka.bootstrapServer}") String bootstrapServer,
      @Value("${kafka.topicName}") String topic) {
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

  public Flux<@NotNull SenderResult<PriceTick>> sendMessage(PriceTick priceTick) {
    return sender
        .send(Mono.just(SenderRecord.create(new ProducerRecord<>(topic, 0, priceTick), priceTick)))
        .doOnNext(func)
        .onErrorContinue((e, _) -> log.error("Send failed", e));
  }

  private final Consumer<SenderResult<PriceTick>> func =
      r -> {
        RecordMetadata metadata = r.recordMetadata();
        Instant timestamp = Instant.ofEpochMilli(metadata.timestamp());
        log.debug(
            "Message {} sent successfully, topic-partition={}-{} offset={} timestamp={}",
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
