package com.example.whatsappclone.Notification.persistence;

import com.example.whatsappclone.Identity.domain.User;
import com.example.whatsappclone.Notification.domain.NotificationsSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationSettingsRepo extends JpaRepository<NotificationsSettings,String> {
   NotificationsSettings findByUser(User user);
   Optional<NotificationsSettings> findByUserUsername(String username);
}
