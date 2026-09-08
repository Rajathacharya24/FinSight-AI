package com.finsight.notification.repository;

import com.finsight.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderBySentAtDesc(String userId);
}
