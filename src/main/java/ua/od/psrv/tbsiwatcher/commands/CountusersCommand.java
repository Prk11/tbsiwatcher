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
import ua.od.psrv.tbsiwatcher.Application;

/**
 *
 * @author Prk
 */
public class CountusersCommand extends BotCommand {

    private static final String LOGTAG = "COUNTUSERSCOMMAND";

    public CountusersCommand() {
        super("/countusers", "Количество зарегистрированных пользователей");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        try {
            SendMessage answer = new SendMessage();
            answer.setChatId(chat.getId().toString());
            answer.enableMarkdown(false);
            String message = "Всего пользователей: " + Application.databaseManager.getCountUsers().toString();
            answer.setText(message);
            absSender.sendMessage(answer);
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }

}
