package com.example.whatsappclone.Repositries;

import com.example.whatsappclone.Entities.NotificationsSettings;
import com.example.whatsappclone.Entities.User;
import org.apache.tomcat.util.modeler.NotificationInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationSettingsRepo extends JpaRepository<NotificationsSettings,String> {
   NotificationsSettings findByUser(User user);
   Optional<NotificationsSettings> findByUserUsername(String username);
   NotificationsSettings findByUserUuid(String userUuid);
}
