package com.github.emberlyte.telegramserverchecker.telegramserverchecker.Bot;

import com.github.emberlyte.telegramserverchecker.telegramserverchecker.Consumers.UpdateConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;

@Service
public class MyBot implements SpringLongPollingBot {

    @Value("${telegram.bot.token}")
    private String token;

    private final UpdateConsumer updateConsumer;

    public MyBot(UpdateConsumer updateConsumer) {
        this.updateConsumer = updateConsumer;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return updateConsumer;
    }
}
