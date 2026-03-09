package com.airline.aws.events;

import com.airline.model.Reservation;
import com.airline.service.events.ReservationEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

/**
 * AWS SNS-based publisher for reservation lifecycle events.
 *
 * This implementation is activated only in AWS-oriented profiles and
 * publishes a simple JSON payload to a configured SNS topic.
 */
@Component
@Profile({"aws-events", "aws-prod"})
public class SnsReservationEventPublisher implements ReservationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SnsReservationEventPublisher.class);

    private final SnsClient snsClient;
    private final String topicArn;

    public SnsReservationEventPublisher(
            SnsClient snsClient,
            @Value("${aws.sns.reservation-topic-arn:}") String topicArn
    ) {
        this.snsClient = snsClient;
        this.topicArn = topicArn;
    }

    @Override
    public void publishReservationCreated(Reservation reservation) {
        publish("RESERVATION_CREATED", reservation);
    }

    @Override
    public void publishReservationCancelled(Reservation reservation) {
        publish("RESERVATION_CANCELLED", reservation);
    }

    private void publish(String eventType, Reservation reservation) {
        if (topicArn == null || topicArn.isBlank()) {
            log.warn("SNS reservation-topic-arn not configured; skipping publish of {} for reservationCode={}", eventType, reservation.getReservationCode());
            return;
        }

        try {
            String payload = buildPayload(eventType, reservation);
            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(payload)
                    .build();
            snsClient.publish(request);
            log.info("Published {} event to SNS for reservationCode={}", eventType, reservation.getReservationCode());
        } catch (Exception e) {
            log.warn("Failed to publish {} event to SNS for reservationCode={}: {}", eventType, reservation.getReservationCode(), e.getMessage());
        }
    }

    private String buildPayload(String eventType, Reservation reservation) {
        return "{"
                + "\"eventType\":\"" + eventType + "\","
                + "\"reservationCode\":\"" + reservation.getReservationCode() + "\","
                + "\"reservationId\":" + reservation.getId() + ","
                + "\"status\":\"" + reservation.getStatus() + "\""
                + "}";
    }
}

