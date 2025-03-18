package com.batch.job;

import com.batch.domain.enumeration.EventType;
import com.batch.domain.model.Shipment;
import com.batch.domain.model.ShippingStatus;
import com.batch.repository.ShipmentRepository;
import com.batch.service.carrier.CarrierTrackingService;
import com.batch.service.carrier.CarrierTrackingServiceFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentStatusUpdateJob {

    private final ShipmentRepository shipmentRepository;
    private final CarrierTrackingServiceFactory carrierTrackingServiceFactory;

    @Value("${batch.shipment.status.update.page-size:100}")
    private int pageSize;

    @Value("${batch.shipment.status.update.max-retry:3}")
    private int maxRetry;

    @Value("${batch.shipment.status.update.retry-delay-hours:2}")
    private int retryDelayHours;

    /**
     * Scheduled job to process shipments pending status updates
     * Runs every 30 minutes by default
     */
    @Scheduled(cron = "${batch.shipment.status.update.cron:0 0/30 * * * ?}")
    public void processShipmentStatusUpdates() {
        log.info("Starting shipment status update batch job");

        boolean hasMoreShipments = true;
        int pageNumber = 0;

        while (hasMoreShipments) {
            Page<Shipment> shipments = findShipmentsForProcessing(pageNumber, pageSize);

            if (shipments.isEmpty()) {
                hasMoreShipments = false;
                continue;
            }

            processShipmentBatch(shipments.getContent());

            pageNumber++;
            hasMoreShipments = !shipments.isLast();
        }

        log.info("Completed shipment status update batch job");
    }

    private Page<Shipment> findShipmentsForProcessing(int pageNumber, int pageSize) {
        LocalDateTime now = LocalDateTime.now();

        return shipmentRepository.findShipmentsForStatusUpdate(
                false,                              // processingInProgress = false
                now,                                // nextCheckDate before or equal to now
                List.of(                            // exclude terminal statuses
                        EventType.SHIPMENT_DELIVERED.name(),
                        EventType.UNKNOWN.name()
                ),
                PageRequest.of(pageNumber, pageSize)
        );
    }

    private void processShipmentBatch(List<Shipment> shipments) {
        log.info("Processing batch of {} shipments", shipments.size());

        for (Shipment shipment : shipments) {
            try {
                // Mark shipment as processing to prevent concurrent updates
                shipment.setProcessingInProgress(true);
                shipmentRepository.save(shipment);

                // Get tracking service for this carrier
                Optional<CarrierTrackingService> trackingService =
                        carrierTrackingServiceFactory.getTrackingService(shipment.getCarrierName());

                if (trackingService.isEmpty()) {
                    handleProcessingError(shipment, "No tracking service found for carrier: " + shipment.getCarrierName());
                    continue;
                }

                // Get updated shipping status
                String trackingNumber = shipment.getTrackingNumber();
                if (trackingNumber == null || trackingNumber.isBlank()) {
                    handleProcessingError(shipment, "Missing tracking number for shipment: " + shipment.getId());
                    continue;
                }

                ShippingStatus status = trackingService.get().getShippingStatus(trackingNumber);
                updateShipmentStatus(shipment, status);

            } catch (Exception e) {
                log.error("Error processing shipment {}: {}", shipment.getId(), e.getMessage(), e);
                handleProcessingError(shipment, e.getMessage());
            }
        }
    }

    private void updateShipmentStatus(Shipment shipment, ShippingStatus status) {
        // Get the most recent event
        Optional<ShippingStatus.ShippingEvent> latestEvent = status.getEvents().stream()
                .max((e1, e2) -> e1.getDate().compareTo(e2.getDate()));

        if (latestEvent.isPresent()) {
            ShippingStatus.ShippingEvent event = latestEvent.get();
            shipment.setLastEventType(event.getEventType().name());

            // For terminal statuses, no more checks needed
            boolean isTerminal = EventType.SHIPMENT_DELIVERED == event.getEventType() ||
                    EventType.UNKNOWN == event.getEventType();

            shipment.setProcessingInProgress(false);
            shipment.setLastUpdateDate(LocalDateTime.now());
            shipment.setRetryCount(0);

            // If not terminal, schedule next check
            if (!isTerminal) {
                shipment.setNextCheckDate(LocalDateTime.now().plusHours(retryDelayHours));
            } else {
                shipment.setNextCheckDate(null);  // No more checks for terminal statuses
            }
        } else {
            // No events found, set to retry later
            handleProcessingError(shipment, "No shipping events found");
        }

        shipmentRepository.save(shipment);
        log.info("Updated status for shipment {}: {}", shipment.getId(), shipment.getLastEventType());
    }

    private void handleProcessingError(Shipment shipment, String errorMessage) {
        Integer retryCount = shipment.getRetryCount();
        retryCount = retryCount == null ? 1 : retryCount + 1;
        shipment.setRetryCount(retryCount);

        // If exceeded max retries, mark as unknown
        if (retryCount >= maxRetry) {
            shipment.setLastEventType(EventType.UNKNOWN.name());
            shipment.setNextCheckDate(null);  // No more checks
        } else {
            // Exponential backoff for retries
            int delayHours = retryDelayHours * retryCount;
            shipment.setNextCheckDate(LocalDateTime.now().plusHours(delayHours));
        }

        shipment.setProcessingInProgress(false);
        shipment.setLastUpdateDate(LocalDateTime.now());

        shipmentRepository.save(shipment);
        log.warn("Processing error for shipment {}: {}. Retry count: {}",
                shipment.getId(), errorMessage, retryCount);
    }
}