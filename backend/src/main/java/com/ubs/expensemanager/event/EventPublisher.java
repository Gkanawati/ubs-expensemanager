package com.ubs.expensemanager.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Service responsible for publishing domain events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Publishes a budget exceeded event.
     *
     * @param event the budget exceeded event to publish
     */
    public void publishBudgetExceededEvent(BudgetExceededEvent event) {
        log.debug("Publishing budget exceeded event: {}", event);
        applicationEventPublisher.publishEvent(event);
    }
}