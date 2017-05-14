/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.od.psrv.tbsiwatcher.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
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
            List<ListResponseObject> listAsList = new ArrayList<>(list);
            Collections.sort(listAsList);
            for (ListResponseObject listResponseObject : listAsList) {
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
                        if (ex1 instanceof TelegramApiRequestException) {
                            try {
                                sendMessage.enableMarkdown(false);
                                String userMessage = "Ошибка вывода сообщения:\n" ;
                                userMessage+="Событие: "+listResponseObject.getEvent().toString()+"\n";
                                userMessage+="Автор: "+listResponseObject.getAuthor();                       
                                sendMessage.setText(userMessage);
                                absSender.sendMessage(sendMessage);
                                
                                sendMessage = new SendMessage();
                                sendMessage.enableMarkdown(false);
                                sendMessage.setChatId(Application.databaseManager.getChatIdForAdmin());
                                String adminMessage = "Ошибка вывода сообщения:\n" ;
                                adminMessage+="Пользователь: "+UserId+"\n";
                                adminMessage+="Событие: "+listResponseObject.getEvent().toString()+"\n";
                                adminMessage+="Автор: "+listResponseObject.getAuthor()+"\n";                          
                                adminMessage+="Код ошибки: "+((TelegramApiRequestException) ex1).getErrorCode().toString()+"\n";                          
                                adminMessage+="Текст ошибки: "+((TelegramApiRequestException) ex1).getMessage()+"\n";                          
                                sendMessage.setText(adminMessage);
                                absSender.sendMessage(sendMessage);
                             }
                            catch (TelegramApiException ex2) {
                                BotLogger.error(LOGTAG, ex2);
                            }
                        }
                    }
                 }
            }
            if (list.isEmpty()) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chat.getId().toString());
                try {
                    sendMessage.enableMarkdown(false);
                    sendMessage.setText("Обновлений нет!");
                    absSender.sendMessage(sendMessage);
                }
                catch (TelegramApiException ex) {
                   BotLogger.error(LOGTAG, ex); 
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
