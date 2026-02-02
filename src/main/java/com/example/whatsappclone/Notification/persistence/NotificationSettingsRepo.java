package com.example.whatsappclone.Notification.persistence;

import com.example.whatsappclone.User.domain.User;
import com.example.whatsappclone.Notification.domain.NotificationsSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationSettingsRepo extends JpaRepository<NotificationsSettings,UUID> {
   NotificationsSettings findByUser(User user);
   Optional<NotificationsSettings> findByUserUsername(String username);
}
