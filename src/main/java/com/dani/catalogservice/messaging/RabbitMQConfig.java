package com.dani.catalogservice.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // --- Exchanges ---
    static final String EXCHANGE          = "catalog-service.events";
    static final String LOAN_EXCHANGE     = "loan-service.events";
    static final String DLX               = "dlx.catalog-service";

    // --- Catalog event queues ---
    static final String QUEUE_TITLE_CREATED   = "catalog-service.catalog.title_created";
    static final String QUEUE_TITLE_UPDATED   = "catalog-service.catalog.title_updated";
    static final String QUEUE_TITLE_DELETED   = "catalog-service.catalog.title_deleted";
    static final String QUEUE_COPY_REGISTERED = "catalog-service.catalog.copy_registered";

    static final String ROUTING_KEY_TITLE_CREATED   = "catalog.title_created";
    static final String ROUTING_KEY_TITLE_UPDATED   = "catalog.title_updated";
    static final String ROUTING_KEY_TITLE_DELETED   = "catalog.title_deleted";
    static final String ROUTING_KEY_COPY_REGISTERED = "catalog.copy_registered";

    // --- Saga: loan events consumed ---
    static final String QUEUE_LOAN_COPY_RESERVATION_REQUESTED = "catalog-service.loan.copy_reservation_requested";
    static final String QUEUE_LOAN_COPY_RELEASE_REQUESTED     = "catalog-service.loan.copy_release_requested";

    static final String ROUTING_KEY_COPY_RESERVATION_REQUESTED = "loan.copy_reservation_requested";
    static final String ROUTING_KEY_COPY_RELEASE_REQUESTED     = "loan.copy_release_requested";

    // --- Saga: catalog response events published ---
    static final String QUEUE_COPY_RESERVED            = "catalog-service.catalog.copy_reserved";
    static final String QUEUE_COPY_RESERVATION_FAILED  = "catalog-service.catalog.copy_reservation_failed";
    static final String QUEUE_COPY_RELEASED            = "catalog-service.catalog.copy_released";

    static final String ROUTING_KEY_COPY_RESERVED           = "catalog.copy_reserved";
    static final String ROUTING_KEY_COPY_RESERVATION_FAILED = "catalog.copy_reservation_failed";
    static final String ROUTING_KEY_COPY_RELEASED           = "catalog.copy_released";

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
    public Binding queueTitleCreatedBinding() {
        return BindingBuilder.bind(titleCreatedQueue()).to(catalogServiceExchange()).with(ROUTING_KEY_TITLE_CREATED);
    }

    @Bean
    public Binding queueTitleUpdatedBinding() {
        return BindingBuilder.bind(titleUpdatedQueue()).to(catalogServiceExchange()).with(ROUTING_KEY_TITLE_UPDATED);
    }

    @Bean
    public Binding queueTitleDeletedBinding() {
        return BindingBuilder.bind(titleDeletedQueue()).to(catalogServiceExchange()).with(ROUTING_KEY_TITLE_DELETED);
    }

    @Bean
    public Binding queueCopyRegisteredBinding() {
        return BindingBuilder.bind(copyRegisteredQueue()).to(catalogServiceExchange()).with(ROUTING_KEY_COPY_REGISTERED);
    }

    // --- Bindings: DLQs → DLX ---
    @Bean
    public Binding queueTitleCreatedDlqBinding() {
        return BindingBuilder.bind(titleCreatedDlq()).to(deadLetterExchange()).with(QUEUE_TITLE_CREATED);
    }

    @Bean
    public Binding queueTitleUpdatedDlqBinding() {
        return BindingBuilder.bind(titleUpdatedDlq()).to(deadLetterExchange()).with(QUEUE_TITLE_UPDATED);
    }

    @Bean
    public Binding queueTitleDeletedDlqBinding() {
        return BindingBuilder.bind(titleDeletedDlq()).to(deadLetterExchange()).with(QUEUE_TITLE_DELETED);
    }

    @Bean
    public Binding queueCopyRegisteredDlqBinding() {
        return BindingBuilder.bind(copyRegisteredDlq()).to(deadLetterExchange()).with(QUEUE_COPY_REGISTERED);
    }

    // --- Loan exchange (passive dependency — declared here so it exists before loan-service) ---
    @Bean
    public TopicExchange loanServiceExchange() {
        return new TopicExchange(LOAN_EXCHANGE, true, false);
    }

    // --- Saga consumer queues (loan events) ---
    @Bean
    public Queue copyReservationRequestedQueue() {
        return buildQueue(QUEUE_LOAN_COPY_RESERVATION_REQUESTED);
    }

    @Bean
    public Queue copyReleaseRequestedQueue() {
        return buildQueue(QUEUE_LOAN_COPY_RELEASE_REQUESTED);
    }

    // --- Saga response queues (catalog events) ---
    @Bean
    public Queue copyReservedQueue() {
        return buildQueue(QUEUE_COPY_RESERVED);
    }

    @Bean
    public Queue copyReservationFailedQueue() {
        return buildQueue(QUEUE_COPY_RESERVATION_FAILED);
    }

    @Bean
    public Queue copyReleasedQueue() {
        return buildQueue(QUEUE_COPY_RELEASED);
    }

    // --- Saga DLQs ---
    @Bean
    public Queue copyReservationRequestedDlq() {
        return QueueBuilder.durable(QUEUE_LOAN_COPY_RESERVATION_REQUESTED + ".dlq").build();
    }

    @Bean
    public Queue copyReleaseRequestedDlq() {
        return QueueBuilder.durable(QUEUE_LOAN_COPY_RELEASE_REQUESTED + ".dlq").build();
    }

    @Bean
    public Queue copyReservedDlq() {
        return QueueBuilder.durable(QUEUE_COPY_RESERVED + ".dlq").build();
    }

    @Bean
    public Queue copyReservationFailedDlq() {
        return QueueBuilder.durable(QUEUE_COPY_RESERVATION_FAILED + ".dlq").build();
    }

    @Bean
    public Queue copyReleasedDlq() {
        return QueueBuilder.durable(QUEUE_COPY_RELEASED + ".dlq").build();
    }

    // --- Bindings: consumer queues → loan exchange ---
    @Bean
    public Binding copyReservationRequestedBinding() {
        return BindingBuilder.bind(copyReservationRequestedQueue()).to(loanServiceExchange()).with(ROUTING_KEY_COPY_RESERVATION_REQUESTED);
    }

    @Bean
    public Binding copyReleaseRequestedBinding() {
        return BindingBuilder.bind(copyReleaseRequestedQueue()).to(loanServiceExchange()).with(ROUTING_KEY_COPY_RELEASE_REQUESTED);
    }

    // --- Bindings: saga response queues → catalog exchange ---
    @Bean
    public Binding copyReservedBinding() {
        return BindingBuilder.bind(copyReservedQueue()).to(catalogServiceExchange()).with(ROUTING_KEY_COPY_RESERVED);
    }

    @Bean
    public Binding copyReservationFailedBinding() {
        return BindingBuilder.bind(copyReservationFailedQueue()).to(catalogServiceExchange()).with(ROUTING_KEY_COPY_RESERVATION_FAILED);
    }

    @Bean
    public Binding copyReleasedBinding() {
        return BindingBuilder.bind(copyReleasedQueue()).to(catalogServiceExchange()).with(ROUTING_KEY_COPY_RELEASED);
    }

    // --- Bindings: saga DLQs → DLX ---
    @Bean
    public Binding copyReservationRequestedDlqBinding() {
        return BindingBuilder.bind(copyReservationRequestedDlq()).to(deadLetterExchange()).with(QUEUE_LOAN_COPY_RESERVATION_REQUESTED);
    }

    @Bean
    public Binding copyReleaseRequestedDlqBinding() {
        return BindingBuilder.bind(copyReleaseRequestedDlq()).to(deadLetterExchange()).with(QUEUE_LOAN_COPY_RELEASE_REQUESTED);
    }

    @Bean
    public Binding copyReservedDlqBinding() {
        return BindingBuilder.bind(copyReservedDlq()).to(deadLetterExchange()).with(QUEUE_COPY_RESERVED);
    }

    @Bean
    public Binding copyReservationFailedDlqBinding() {
        return BindingBuilder.bind(copyReservationFailedDlq()).to(deadLetterExchange()).with(QUEUE_COPY_RESERVATION_FAILED);
    }

    @Bean
    public Binding copyReleasedDlqBinding() {
        return BindingBuilder.bind(copyReleasedDlq()).to(deadLetterExchange()).with(QUEUE_COPY_RELEASED);
    }

    // --- Message converter (used by RabbitTemplate for publishing) ---
    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    // --- Listener container factory (used by @RabbitListener) ---
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            ObjectMapper objectMapper,
            @Value("${rabbitmq.prefetch:10}") int prefetch) {
        var converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setAlwaysConvertToInferredType(true);

        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        factory.setPrefetchCount(prefetch);
        return factory;
    }

    // --- Helpers ---
    private Queue buildQueue(String name) {
        return QueueBuilder.durable(name).deadLetterExchange(DLX).deadLetterRoutingKey(name).build();
    }
}
