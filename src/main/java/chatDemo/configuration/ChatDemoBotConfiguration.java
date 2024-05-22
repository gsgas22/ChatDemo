package chatDemo.configuration;

import chatDemo.bot.ChatDemoBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class ChatDemoBotConfiguration{

    @Bean
    public TelegramBotsApi telegramBotsApi(ChatDemoBot chatDemoBot) throws TelegramApiException{
        var api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(chatDemoBot);
        return api;
    }
}
