package com.dani.catalogservice.messaging;

import com.dani.catalogservice.model.CopyCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CatalogEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CatalogEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public CatalogEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishTitleCreated(UUID titleId, String isbn, String title, String author, String genre) {
        var payload = new TitleCreatedPayload(titleId, isbn, title, author, genre);
        var envelope = MessageEnvelope.of(RabbitMQConfig.ROUTING_KEY_TITLE_CREATED, payload);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_TITLE_CREATED, envelope);
        log.debug("Published {}: titleId={}", RabbitMQConfig.ROUTING_KEY_TITLE_CREATED, titleId);
    }

    public void publishTitleUpdated(UUID titleId) {
        var payload = new TitleUpdatedPayload(titleId);
        var envelope = MessageEnvelope.of(RabbitMQConfig.ROUTING_KEY_TITLE_UPDATED, payload);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_TITLE_UPDATED, envelope);
        log.debug("Published {}: titleId={}", RabbitMQConfig.ROUTING_KEY_TITLE_UPDATED, titleId);
    }

    public void publishTitleDeleted(UUID titleId) {
        var payload = new TitleDeletedPayload(titleId);
        var envelope = MessageEnvelope.of(RabbitMQConfig.ROUTING_KEY_TITLE_DELETED, payload);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_TITLE_DELETED, envelope);
        log.debug("Published {}: titleId={}", RabbitMQConfig.ROUTING_KEY_TITLE_DELETED, titleId);
    }

    public void publishCopyRegistered(UUID copyId, UUID titleId, CopyCondition condition) {
        var payload = new CopyRegisteredPayload(copyId, titleId, condition);
        var envelope = MessageEnvelope.of(RabbitMQConfig.ROUTING_KEY_COPY_REGISTERED, payload);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_COPY_REGISTERED, envelope);
        log.debug("Published {}: copyId={}, titleId={}", RabbitMQConfig.ROUTING_KEY_COPY_REGISTERED, copyId, titleId);
    }

    public void publishCopyReserved(UUID loanId, UUID copyId, UUID titleId) {
        var payload = new CopyReservedPayload(loanId, copyId, titleId);
        var envelope = MessageEnvelope.of(RabbitMQConfig.ROUTING_KEY_COPY_RESERVED, payload);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_COPY_RESERVED, envelope);
        log.debug("Published {}: loanId={}, copyId={}", RabbitMQConfig.ROUTING_KEY_COPY_RESERVED, loanId, copyId);
    }

    public void publishCopyReservationFailed(UUID loanId, UUID titleId, String reason) {
        var payload = new CopyReservationFailedPayload(loanId, titleId, reason);
        var envelope = MessageEnvelope.of(RabbitMQConfig.ROUTING_KEY_COPY_RESERVATION_FAILED, payload);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_COPY_RESERVATION_FAILED, envelope);
        log.debug("Published {}: loanId={}, titleId={}", RabbitMQConfig.ROUTING_KEY_COPY_RESERVATION_FAILED, loanId, titleId);
    }

    public void publishCopyReleased(UUID loanId, UUID copyId, UUID titleId) {
        var payload = new CopyReleasedPayload(loanId, copyId, titleId);
        var envelope = MessageEnvelope.of(RabbitMQConfig.ROUTING_KEY_COPY_RELEASED, payload);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_COPY_RELEASED, envelope);
        log.debug("Published {}: loanId={}, copyId={}", RabbitMQConfig.ROUTING_KEY_COPY_RELEASED, loanId, copyId);
    }

    // --- Event payload records ---

    record TitleCreatedPayload(UUID titleId, String isbn, String title, String author, String genre) {}

    record TitleUpdatedPayload(UUID titleId) {}

    record TitleDeletedPayload(UUID titleId) {}

    record CopyRegisteredPayload(UUID copyId, UUID titleId, CopyCondition condition) {}

    record CopyReservedPayload(UUID loanId, UUID copyId, UUID titleId) {}

    record CopyReservationFailedPayload(UUID loanId, UUID titleId, String reason) {}

    record CopyReleasedPayload(UUID loanId, UUID copyId, UUID titleId) {}
}