package moe.alprc.liftAvatarBot;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.Scanner;

/**
 * Created by alprc on 2017/4/28/028.
 */
public class Main {
    public static void main(String[] args) {
        ApiContextInitializer.init();

        TelegramBotsApi botsApi = new TelegramBotsApi();
        TelegramLongPollingBot bot = new LiftAvatarBot();

        class TempClass implements Runnable {
            TelegramBotsApi botsApi;
            TelegramLongPollingBot bot;

            TempClass(TelegramBotsApi botsApi, TelegramLongPollingBot bot) {
                this.botsApi = botsApi;
                this.bot = bot;
            }

            @Override
            public void run() {
                try {
                    botsApi.registerBot(bot);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
        Thread thread = new Thread(new TempClass(botsApi, bot));
        thread.start();

        Scanner in = new Scanner(System.in);
        String cmd, arguments;
        while (true) {
            cmd = in.next();
            arguments = in.nextLine();
            if (arguments.length() > 0) {
                arguments = arguments.substring(1);
            }
            if (cmd.equals("stop")) {
                break;
            }

            long chatId = 0;
            try {
                chatId = Long.parseLong(cmd);
            } catch (Exception e) {
                //
            }

            if (chatId > 0L) {
                System.out.println("Sending message to: " + chatId);

                SendMessage message = new SendMessage()
                        .setChatId(chatId)
                        .setText(arguments);

                try {
                    bot.sendMessage(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

        }

        System.exit(0);
    }
}
