package drot.telegram.telegrambot.repoditory;

import drot.telegram.telegrambot.model.NotificationTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface BotRepository extends JpaRepository<NotificationTask,Long> {

    NotificationTask findNotificationTaskByDateTimeIsLike(String str);
}
