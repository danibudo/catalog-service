package com.dani.catalogservice.messaging;

import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;

public class RabbitMQConfig {
    static final String EXCHANGE = "catalog-service.events";
    static final String DLX = "dlx.catalog-service";

    static final String QUEUE_TITLE_CREATED="catalog.title_created";
    static final String QUEUE_TITLE_UPDATED="catalog.title_updated";
    static final String QUEUE_TITLE_DELETED="catalog.title_deleted";
    static final String QUEUE_COPY_REGISTERED="catalog.copy_registered";

    static final String ROUTING_KEY_TITLE_CREATED ="title.created";
    static final String ROUTING_KEY_TITLE_UPDATED ="title.updated";
    static final String ROUTING_KEY_TITLE_DELETED ="title.deleted";
    static final String ROUTING_KEY_COPY_REGISTERED ="copy.registered";

    // --- Exchanges ---
    @Bean
    public TopicExchange catalogServiceExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    // --- Queues ---
    @Bean
    public Queue titleCreatedQueue() {
        return buildQueue(QUEUE_TITLE_CREATED);
    }

    @Bean
    public Queue titleUpdatedQueue() {
        return buildQueue(QUEUE_TITLE_UPDATED);
    }

    @Bean
    public Queue titleDeletedQueue() {
        return buildQueue(QUEUE_TITLE_DELETED);
    }

    @Bean
    public Queue copyRegisteredQueue() {
        return buildQueue(QUEUE_COPY_REGISTERED);
    }

    // --- Dead-letter queues ---
    @Bean
    public Queue titleCreatedDlq() {
        return QueueBuilder.durable(QUEUE_TITLE_CREATED + ".dlq").build();
    }

    @Bean
    public Queue titleUpdatedDlq() {
        return QueueBuilder.durable(QUEUE_TITLE_UPDATED + ".dlq").build();
    }

    @Bean
    public Queue titleDeletedDlq() {
        return QueueBuilder.durable(QUEUE_TITLE_DELETED + ".dlq").build();
    }

    @Bean
    public Queue copyRegisteredDlq() {
        return QueueBuilder.durable(QUEUE_COPY_REGISTERED + ".dlq").build();
    }

    // --- Bindings: main queues → topic exchange ---
    @Bean
    public void queueTitleCreatedBinding() {
        BindingBuilder.bind(titleCreatedQueue()).to(catalogServiceExchange()).with(ROUTING_KEY_TITLE_CREATED);
    }

    @Bean
    public void queueTitleUpdatedBinding() {
        BindingBuilder.bind(titleUpdatedQueue()).to(catalogServiceExchange()).with(ROUTING_KEY_TITLE_UPDATED);
    }

    @Bean
    public void queueTitleDeletedBinding() {
        BindingBuilder.bind(titleDeletedQueue()).to(catalogServiceExchange()).with(ROUTING_KEY_TITLE_DELETED);
    }

    @Bean
    public void queueCopyRegisteredBinding() {
        BindingBuilder.bind(copyRegisteredQueue()).to(catalogServiceExchange()).with(ROUTING_KEY_COPY_REGISTERED);
    }

    // --- Bindings: DLQs → DLX ---
    @Bean
    public void queueTitleCreatedDqlBinding() {
        BindingBuilder.bind(titleCreatedDlq()).to(deadLetterExchange()).with(QUEUE_TITLE_CREATED);
    }

    @Bean
    public void queueTitleUpdatedDqlBinding() {
        BindingBuilder.bind(titleUpdatedDlq()).to(deadLetterExchange()).with(QUEUE_TITLE_UPDATED);
    }

    @Bean
    public void queueTitleDeletedDqlBinding() {
        BindingBuilder.bind(titleDeletedDlq()).to(deadLetterExchange()).with(QUEUE_TITLE_DELETED);
    }

    @Bean
    public void queueCopyRegisteredDqlBinding() {
        BindingBuilder.bind(copyRegisteredDlq()).to(deadLetterExchange()).with(QUEUE_COPY_REGISTERED);
    }

    // --- Message converter ---
    @Bean
    public MessageConverter messageConverter(JsonMapper jsonMapper) {
        return new Jackson2JsonMessageConverter(jsonMapper);
    }

    // --- Helpers ---
    private Queue buildQueue(String name) {
        return QueueBuilder.durable(name).deadLetterExchange(DLX).deadLetterRoutingKey(name).build();
    }
}
