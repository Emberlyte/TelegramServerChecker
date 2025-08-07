package com.github.emberlyte.telegramserverchecker.telegramserverchecker.Consumers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Service
@Slf4j
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    @Value("${admin.user.id}")
    private Long adminUserID;

    @Value("${scripts.dir}")
    private String scriptsDir;

    private final TelegramClient telegramClient;

    public UpdateConsumer(@Value("${telegram.bot.token}") String token) {
        this.telegramClient = new OkHttpTelegramClient(token);
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            checkMessageText(update);
        }
    }

    private void checkMessageText(Update update) {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.hasMessage() ? update.getMessage().getFrom().getId() : null;
        String username = update.getMessage().getFrom().getUserName();
        String text = update.hasMessage() ? update.getMessage().getText() : null;

        if (text == null || chatId == null || userId == null) {
            log.error("Message/chatId/userId text is null");
            return;
        }

        if(!hasAccess(userId)){
            sendMessage(chatId, "этот бот онли для эксикры:((.\nВаш ID: " + userId);
            log.warn("Попытка доступа от неавторизованного пользователя: ID={}, Username= @{}",
                    userId, username);
            return;
        }

        try {
            switch (text) {
                case "/start" -> sendMessage(chatId, "Привет!");
                case "/script" -> executeFail2BanScript(chatId);
                default -> sendMessage(chatId, "а");
            }
        } catch (Exception e) {
            log.error("Ошибка при отправке команды: {}", e.getMessage(), e);
        }
    }

    private void executeFail2BanScript(Long chatId) {
        try {
            String filePath = scriptsDir + "monitor.sh";
            File scriptFile = new File(filePath);

            if (!scriptFile.exists() || !scriptFile.canExecute()) {
                sendMessage(chatId, "Ошибка: Скрипт 'monitor.sh' не найден или не имеет прав на выполнение.");
                log.error("Скрипт не найден или не исполняем: {}", filePath);
                return;
            }

            ProcessBuilder processBuilder = new ProcessBuilder("sudo", "/bin/bash", scriptFile.getAbsolutePath());
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            log.info("Запущен новый скрипт: {}", process);

            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                sendMessage(chatId, "Скрипт выполнен!");
                sendMessage(chatId, output.toString());
            } else {
                sendMessage(chatId, "Скрипт не выполнен!");
                sendMessage(chatId, output.toString());
            }
        } catch (Exception e) {
            log.error("Ошибка при выполнении скрипта (fail2ban). Ошибка: {}", e.getMessage(), e);
            sendMessage(chatId, "Произошла ошибка при выполнения скрипта");
        }
    }

    private boolean hasAccess(Long userId) {
        return userId.equals(adminUserID);
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = SendMessage
                .builder()
                .chatId(chatId)
                .text(text)
                .build();

        try {
            telegramClient.execute(message);
            log.info("Новое сообщение от: {}. В: {}", text, chatId);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения: {}", e.getMessage(), e);
        }
    }
}
