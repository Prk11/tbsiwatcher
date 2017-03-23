/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.od.psrv.tbsiwatcher.commands;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

/**
 *
 * @author Prk
 */
public class StartCommand extends BotCommand {

    private static final String LOGTAG = "HELPCOMMAND";

    public StartCommand() {
        super("/start", "Начало работы");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        try {
            SendMessage answer = new SendMessage();
            answer.setChatId(chat.getId().toString());
            answer.enableMarkdown(false);
            String message = "Ознакомтесь со справкой командой /help";
            answer.setText(message);
            absSender.sendMessage(answer);
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }

}
