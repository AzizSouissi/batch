package com.batch.repository;

import com.batch.domain.model.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShipmentRepository extends MongoRepository<Shipment, String> {

    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    Optional<Shipment> findByReference(String reference);

    /**
     * Find shipments that need status updates based on multiple criteria:
     * - Not currently being processed
     * - Due for a status check (nextCheckDate <= now)
     * - Not in terminal status (delivered, unknown)
     */
    @Query("{'processingInProgress': ?0, 'nextCheckDate': {$lte: ?1}, 'lastEventType': {$nin: ?2}}")
    Page<Shipment> findShipmentsForStatusUpdate(
            boolean processingInProgress,
            LocalDateTime nextCheckDateBefore,
            List<String> excludedStatuses,
            Pageable pageable);

    /**
     * Find shipments by carrier that are due for processing
     */
    @Query("{'carrierName': ?0, 'processingInProgress': ?1, 'nextCheckDate': {$lte: ?2}}")
    Page<Shipment> findShipmentsByCarrierForProcessing(
            String carrierName,
            boolean processingInProgress,
            LocalDateTime nextCheckDateBefore,
            Pageable pageable);

    /**
     * Find shipments that have pending updates and need reprocessing after failures
     */
    @Query("{'processingInProgress': ?0, 'lastUpdateDate': {$lte: ?1}, 'retryCount': {$gt: 0, $lt: ?2}}")
    Page<Shipment> findPendingUpdateShipments(
            boolean processingInProgress,
            LocalDateTime lastUpdateBefore,
            int maxRetryCount,
            Pageable pageable);
}