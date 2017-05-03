package moe.alprc.liftAvatarBot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * Created by alprc on 2017/4/28/028.
 */
public class Main {
    public static void main(String[] args) {
        ApiContextInitializer.init();

        TelegramBotsApi botsApi = new TelegramBotsApi();

        try {
            botsApi.registerBot(new LiftAvatarBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
