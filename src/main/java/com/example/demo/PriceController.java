package com.example.demo;

import com.example.demo.kafka.KafkaConsumer;
import com.example.demo.model.PriceTick;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PriceController {

    private final KafkaConsumer kafkaConsumer;

    @GetMapping(value = "/ticks", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<@NotNull PriceTick> demo() {
        log.info("accessing /ticks endpoint");
        return kafkaConsumer.getSink().asFlux();
    }
}
