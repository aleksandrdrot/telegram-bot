package drot.telegram.telegrambot.service;

import drot.telegram.telegrambot.model.NotificationTask;
import drot.telegram.telegrambot.repoditory.BotRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
@EnableScheduling
public class BotService extends TelegramLongPollingBot {

    private static final String START = "/start";

    private final BotRepository repository;


    public BotService(@Value("${bot.token}") String botToken, BotRepository repository) {
        super(botToken);
        this.repository = repository;
    }

    @Override
    public String getBotUsername() {
        return "${bot.name}";
    }

    @Override
    public void onUpdateReceived(Update update) {
        String userName = update.getMessage().getChat().getFirstName();
        String message = update.getMessage().getText();
        Long id = update.getMessage().getChatId();

        if (message.equals(START)) {
            log.info(userName);
            StartCom(id, userName);
        } else {
            CommentSave(id, message);
        }

    }

    public void CommentSave(long id, String message) {
        String dateTime = "";
        String text = "";
        Pattern pattern = Pattern.compile("[0-9\\.\\:\\s]{16}+");
        Matcher matcher = pattern.matcher(message);
        while (matcher.find()) {
            dateTime = message.substring(matcher.start(), matcher.end());
        }
        if (dateTime.matches("[0-9\\.\\:\\s]{16}+")) {
            text = message.substring(17);
            log.info(dateTime);
            log.info(text);

            LocalDateTime date = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            log.info(date.toString());

            NotificationTask t = new NotificationTask();
            t.setId(id);
            t.setDateTime(date.toString());
            t.setComment(text);
            repository.save(t);
        } else {
            sendMessage(id, "Дата или время введены не корректно!");
            log.error("Дата введена не корректно!");
        }
    }


    @Scheduled(cron = "1 * * * * *")
    public void databaseSearch() {
        LocalDateTime date = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        NotificationTask t = repository.findNotificationTaskByDateTimeIsLike(date.toString());

        if (t != null) {
            sendMessage(t.getId(), t.getComment());
            log.info(t.getComment());
        }
    }

    public void StartCom(long id, String userName) {
        String text = "Привет " + userName + ", сдесь ты можеш написать сообщение с датой и времинем в формате " +
                "'дд.мм.гггг чч:мм 'текст'' и я верну 'текст' сообщения в указанную дату и время!";
        sendMessage(id, text);

    }

    public void sendMessage(Long id, String text) {
        var idStr = String.valueOf(id);
        var sendMessage = new SendMessage(idStr, text);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Сообщение не отправлено!");
        }
    }
}
