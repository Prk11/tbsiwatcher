/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.od.psrv.tbsiwatcher;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;
import ua.od.psrv.tbsiwatcher.commands.CountusersCommand;
import ua.od.psrv.tbsiwatcher.commands.HelpCommand;
import ua.od.psrv.tbsiwatcher.commands.ListCommand;
import ua.od.psrv.tbsiwatcher.commands.SettingsCommand;
import ua.od.psrv.tbsiwatcher.commands.StartCommand;
import ua.od.psrv.tbsiwatcher.commands.ToSendMessageAllUsersCommand;
import ua.od.psrv.tbsiwatcher.events.EventSendResponseObject;
import ua.od.psrv.tbsiwatcher.events.EventSendText;
import ua.od.psrv.tbsiwatcher.model.ListResponseObject;

/**
 *
 * @author Prk
 */
public class SiwatcherBot extends TelegramLongPollingCommandBot {

    public static final String LOGTAG = "COMMANDSHANDLER";

    private Sheduller sheduller;
    private Thread threadSheduller;
    
    @Override
    public String getBotUsername() {
        try {
            Properties properties = new Properties();
            properties.load(ClassLoader.getSystemResourceAsStream("settings.properties"));            
            return properties.getProperty("botname");
            
        } catch (IOException ex) {
            BotLogger.error(LOGTAG, ex);           
        }
        return null;
    }

    @Override
    public String getBotToken() {
        try {
            Properties properties = new Properties();
            properties.load(ClassLoader.getSystemResourceAsStream("settings.properties"));            
            return properties.getProperty("bottoken");            
        } catch (IOException ex) {
            BotLogger.error(LOGTAG, ex);           
        }
        return null;
    }

    public SiwatcherBot() {
        registerAll(
                new StartCommand(),
                new HelpCommand(),
                new ListCommand(),
                new SettingsCommand(),
                new CountusersCommand(),
                new ToSendMessageAllUsersCommand()
        );
        Application.databaseManager.setEventResponseObject(
                new EventSendResponseObject() {
                    @Override
                    public void onSendMessage(Long ChatId, ListResponseObject Message) {                       
                        try {
                            Message.setMarkup(true);
                            simpleSendMessage(ChatId, Message.toString(), true);
                        } catch (TelegramApiException ex) {
                            try {
                                Message.setMarkup(false);
                                simpleSendMessage(ChatId, Message.toString(), false);
                            } catch (TelegramApiException ex1) {
                                BotLogger.error(LOGTAG, ex1);
                                try {                                    
                                    simpleSendMessage(Application.databaseManager.getChatIdForAdmin(), "Ошибка диспетчера для чата: "+ ChatId.toString(), false);
                                } catch (TelegramApiException ex2) {
                                    BotLogger.error(LOGTAG, ex2);
                                }
                            }
                        }
                    }
        });
        Application.databaseManager.setEventSendText(new EventSendText() {
            @Override
            public void onSendMessage(Long ChatId, String Message) {
                try {
                    simpleSendMessage(ChatId, Message, true);
                } catch (TelegramApiException ex) {
                    BotLogger.error(LOGTAG, ex);
                }
            }
        });
        sheduller = new Sheduller(
                Integer.parseInt(Application.settings.getProperties().getProperty("timeout","10")));
        threadSheduller = new Thread(sheduller);
        threadSheduller.start();
    }

    private String wordCallbackQuery="";
    private Integer editMessageId;
    
    @Override
    public void processNonCommandUpdate(Update update) {
        Long ChatId = 0L;
        if (update.hasMessage()) ChatId=update.getMessage().getChatId();
        else if (update.hasCallbackQuery()) {
            ChatId=update.getCallbackQuery().getMessage().getChatId();
            editMessageId=update.getCallbackQuery().getMessage().getMessageId();
        }
        Message message = update.getMessage();
        Integer UID = Application.databaseManager.getUserId(ChatId);
        
        if (update.hasCallbackQuery()) {
            wordCallbackQuery=update.getCallbackQuery().getData();
            
            if (wordCallbackQuery.startsWith("setuserid")) {
                try {
                    simpleSendMessage(ChatId,"Введите идентификатор пользователя!", false);                    
                } catch (TelegramApiException ex) {
                    BotLogger.error(LOGTAG, ex);
                }
            }
            else if (wordCallbackQuery.startsWith("subscribe:on")) {
                wordCallbackQuery="";
                try {
                    Application.databaseManager.setSetting(UID, "subscribe", "true");
                    simpleRefreshMessage(ChatId);                           
                } catch (Exception ex) {
                    BotLogger.error(LOGTAG, ex);
                }
            }
            else if (wordCallbackQuery.startsWith("subscribe:off")) {
                wordCallbackQuery="";
                try {
                    Application.databaseManager.setSetting(UID, "subscribe", "false");
                    simpleRefreshMessage(ChatId);                           
                } catch (Exception ex) {
                    BotLogger.error(LOGTAG, ex);
                }
            }    
            else if (wordCallbackQuery.startsWith("subscribe.type.text_deleted:on")) {
                wordCallbackQuery="";
                try {
                    Application.databaseManager.setSetting(UID, "subscribe", "true");
                    Application.databaseManager.setSetting(UID, "subscribe.type.text_deleted", "true");
                    simpleRefreshMessage(ChatId);                           
                } catch (Exception ex) {
                    BotLogger.error(LOGTAG, ex);
                }
            }
            else if (wordCallbackQuery.startsWith("subscribe.type.text_deleted:off")) {
                wordCallbackQuery="";
                try {
                    Application.databaseManager.setSetting(UID, "subscribe.type.text_deleted", "false");
                    simpleRefreshMessage(ChatId);                           
                } catch (Exception ex) {
                    BotLogger.error(LOGTAG, ex);
                }
            }    
            else if (wordCallbackQuery.startsWith("subscribe.type.text_updated:on")) {
                wordCallbackQuery="";
                try {
                    Application.databaseManager.setSetting(UID, "subscribe", "true");
                    Application.databaseManager.setSetting(UID, "subscribe.type.text_updated", "true");
                    simpleRefreshMessage(ChatId);                           
                } catch (Exception ex) {
                    BotLogger.error(LOGTAG, ex);
                }
            }
            else if (wordCallbackQuery.startsWith("subscribe.type.text_updated:off")) {
                wordCallbackQuery="";
                try {
                    Application.databaseManager.setSetting(UID, "subscribe.type.text_updated", "false");
                    simpleRefreshMessage(ChatId);                           
                } catch (Exception ex) {
                    BotLogger.error(LOGTAG, ex);
                }
            }    
            else if (wordCallbackQuery.startsWith("subscribe.type.text_update.size_incremented:on")) {
                wordCallbackQuery="";
                try {
                    Application.databaseManager.setSetting(UID, "subscribe", "true");
                    Application.databaseManager.setSetting(UID, "subscribe.type.text_updated", "true");
                    Application.databaseManager.setSetting(UID, "subscribe.type.text_update.size_incremented", "true");
                    simpleRefreshMessage(ChatId);                           
                } catch (Exception ex) {
                    BotLogger.error(LOGTAG, ex);
                }
            }
            else if (wordCallbackQuery.startsWith("subscribe.type.text_update.size_incremented:off")) {
                wordCallbackQuery="";
                try {
                    Application.databaseManager.setSetting(UID, "subscribe.type.text_update.size_incremented", "false");
                    simpleRefreshMessage(ChatId);                           
                } catch (Exception ex) {
                    BotLogger.error(LOGTAG, ex);
                }
            }    
            else if (wordCallbackQuery.startsWith("subscribe.type.author_typed:on")) {
                wordCallbackQuery="";
                try {
                    Application.databaseManager.setSetting(UID, "subscribe", "true");
                    Application.databaseManager.setSetting(UID, "subscribe.type.author_typed", "true");
                    simpleRefreshMessage(ChatId);                           
                } catch (Exception ex) {
                    BotLogger.error(LOGTAG, ex);
                }
            }
            else if (wordCallbackQuery.startsWith("subscribe.type.author_typed:off")) {
                wordCallbackQuery="";
                try {
                    Application.databaseManager.setSetting(UID, "subscribe.type.author_typed", "false");
                    simpleRefreshMessage(ChatId);                           
                } catch (Exception ex) {
                    BotLogger.error(LOGTAG, ex);
                }
            }    
            
        }
        else if (message != null && message.hasText()) {
            if (message.getText().equals("выключить бота!")) {
                sheduller.Terminate();
                try {
                    Thread.sleep(1000);
                    simpleSendMessage(message.getChatId(),"Выполнено!", false);
                } catch (InterruptedException | TelegramApiException ex) {
                    BotLogger.error(LOGTAG, ex);
                }               
                System.exit(1);
            } 
            else if (message.getText().equals("Введите E-Mail:")) {
                try {
                    simpleSendMessage(message.getChatId(),"Введите пароль siwatcher:", false);
                } catch (TelegramApiException ex) {
                    BotLogger.error(LOGTAG, ex);
                }           
            }
            else if (message.getText().equals("Введите пароль siwatcher:")) {
                try {
                    simpleSendMessage(message.getChatId(),"Введите пароль siwatcher:", false);
                } catch (TelegramApiException ex) {
                    BotLogger.error(LOGTAG, ex);
                }           
            }
            else if (wordCallbackQuery.startsWith("setuserid")) {
                wordCallbackQuery="";
                if (message.getText().trim().equals(Application.databaseManager.getSiwatcherUserId(ChatId))) return;
                try {
                    Application.databaseManager.linkCharAndUser(message.getChatId(), message.getText());
                    simpleRefreshMessage(ChatId);
                } catch (IOException ex) {
                    try {
                        simpleSendMessage(ChatId,"Не корректный идентификатор пользователя. Ознакомтесь со справкой /help", false);                        
                    } catch (TelegramApiException ex1) {
                        BotLogger.error(LOGTAG, ex1);
                    }
                } catch (TelegramApiException ex) {
                    BotLogger.error(LOGTAG, ex);
                } catch (SQLException ex) {
                    try {
                        simpleSendMessage(ChatId,"Не корректный идентификатор пользователя. Возможно такой идентификатор закреплен за другим аккаунтом Telegram. Ознакомтесь со справкой /help", false);                        
                    } catch (TelegramApiException ex1) {
                        BotLogger.error(LOGTAG, ex1);
                    }
                } 
            }
        }
    }

    public void simpleSendMessage(Long chatid, String message, Boolean markdown) throws TelegramApiException {
        SendMessage _sendMessage = new SendMessage();
        _sendMessage.enableMarkdown(markdown);
        _sendMessage.setChatId(chatid);
        _sendMessage.setText(message);
        sendMessage(_sendMessage);
    }   

    public void simpleRefreshMessage(Long chatid) throws TelegramApiException {
        EditMessageText _editMessageText = new EditMessageText();
        _editMessageText.setChatId(chatid);
        _editMessageText.setMessageId(editMessageId);
        _editMessageText.enableMarkdown(true);
        String newEditMessageText=SettingsCommand.getSettingsButtonsText(chatid);
        _editMessageText.setText(newEditMessageText);
        _editMessageText.setReplyMarkup(SettingsCommand.getSettingsKeyboards(chatid));
        editMessageText(_editMessageText);
    }   




   
    


    
}
