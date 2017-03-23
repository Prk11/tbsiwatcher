/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.od.psrv.tbsiwatcher.commands;

import java.io.IOException;
import java.util.Set;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;
import ua.od.psrv.tbsiwatcher.Application;
import ua.od.psrv.tbsiwatcher.SiwatcherManager;
import ua.od.psrv.tbsiwatcher.model.ListResponseObject;

/**
 *
 * @author Prk
 */
public class ListCommand extends BotCommand {

    private static final String LOGTAG = "LISTCOMMAND";

    private String user_id;

    public ListCommand() {
        super("/list", "Содержимое кабинета");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        try {
            String UserId = Application.databaseManager.getSiwatcherUserId(chat.getId());
            Set<ListResponseObject> list = SiwatcherManager.getResponse(UserId, false);
            for (ListResponseObject listResponseObject : list) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chat.getId().toString());
                try {
                    sendMessage.enableMarkdown(true);
                    listResponseObject.setMarkup(true);
                    sendMessage.setText(listResponseObject.toString());
                    absSender.sendMessage(sendMessage);
                }
                catch (TelegramApiException ex) {
                    try {
                        sendMessage.enableMarkdown(false);
                        listResponseObject.setMarkup(false);
                        sendMessage.setText(listResponseObject.toString());
                        absSender.sendMessage(sendMessage);
                    } catch (TelegramApiException ex1) {
                        BotLogger.error(LOGTAG, ex1);
                    }
                }
            }
        } catch (InterruptedException ex) {
            BotLogger.error(LOGTAG, ex);
        } catch (IOException ex) {
            BotLogger.error(LOGTAG, ex);
            try {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chat.getId().toString());
                sendMessage.enableMarkdown(false);
                sendMessage.setText("Не удалось загрузить список обновлений. Возможно не верно указан ID пользователя. Ознакомтесь со справкой /help");
                absSender.sendMessage(sendMessage);
            } catch (TelegramApiException ex1) {
                BotLogger.error(LOGTAG, ex1);
            }
        } 
        
    }

}
