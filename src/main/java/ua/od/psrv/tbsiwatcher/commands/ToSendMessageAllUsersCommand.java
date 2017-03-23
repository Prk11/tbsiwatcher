/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.od.psrv.tbsiwatcher.commands;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class ToSendMessageAllUsersCommand extends BotCommand {

    private static final String LOGTAG = "TOSENDMESSAGEALLUSERSCOMMAND";


    public ToSendMessageAllUsersCommand() {
        super("/tosendmessageallusers", "Отправить сообщение всем подписчикам");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        try {
            String message = String.join(" ", arguments);
            List<Long> Chats = Application.databaseManager.getAllChats();
            for (Long Chat : Chats) {
                SendMessage answer = new SendMessage();
                answer.setChatId(Chat);
                answer.enableMarkdown(false);
                answer.setText(message);
                absSender.sendMessage(answer);
            }

        } catch ( TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }

}
