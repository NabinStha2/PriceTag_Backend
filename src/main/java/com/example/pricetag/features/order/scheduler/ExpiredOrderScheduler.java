package com.example.pricetag.features.order.scheduler;

import com.example.pricetag.features.order.repository.OrderRepo;
import com.example.pricetag.features.order.service.OrderLifecycleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class ExpiredOrderScheduler {

    private final OrderRepo orderRepo;
    private final OrderLifecycleService orderLifecycleService;
    private final int batchSize;

    public ExpiredOrderScheduler(OrderRepo orderRepo,
                                 OrderLifecycleService orderLifecycleService,
                                 @Value("${orders.expiration.batch-size:100}")
                                 int batchSize) {
        this.orderRepo = orderRepo;
        this.orderLifecycleService = orderLifecycleService;
        this.batchSize = Math.max(batchSize, 1);
    }

    @Scheduled(fixedDelayString = "${orders.expiration.check-delay-ms:60000}")
    public void expireOrders() {
        List<Long> orderIds = orderRepo.findExpiredPendingPaymentOrderIds(
                LocalDateTime.now(), PageRequest.of(0, batchSize));

        for (Long orderId : orderIds) {
            try {
                if (orderLifecycleService.expirePendingOrder(orderId)) {
                    log.info("Expired pending-payment order {}", orderId);
                }
            } catch (RuntimeException exception) {
                log.error("Could not expire order {}: {}", orderId, exception.getMessage());
            }
        }
    }
}
