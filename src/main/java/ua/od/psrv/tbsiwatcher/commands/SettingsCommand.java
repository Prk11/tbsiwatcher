/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.od.psrv.tbsiwatcher.commands;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;
import ua.od.psrv.tbsiwatcher.Application;

/**
 *
 * @author Prk
 */
public class SettingsCommand extends BotCommand {

    private static final String LOGTAG = "SETTINGSCOMMAND";

    public SettingsCommand() {
        super("/settings", "Задание настроек");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        try {
            SendMessage answer = new SendMessage();
            answer.enableMarkdown(true);
            answer.setChatId(chat.getId().toString());
            answer.setText("Не известная команда. Ознакомтесь со справкой /help");
            Integer UID=Application.databaseManager.getUserId(chat.getId());
            if (arguments.length == 0) {
                String text = getSettingsButtonsText(chat.getId());
                answer.setText(text);
                InlineKeyboardMarkup inlineKeyboard = getSettingsKeyboards(chat.getId());
                answer.setReplyMarkup(inlineKeyboard);                                                                                                                                                       
            } else {
                if (arguments[0].trim().toLowerCase().equals("user")) {
                    try {
                        Application.databaseManager.linkCharAndUser(chat.getId(), arguments[1].trim());
                        answer.setText("Выполнено!");
                    } catch (IOException ex) {
                        answer.setText("Не корректный идентификатор пользователя. Ознакомтесь со справкой /help");
                    } catch (SQLException ex) {
                        answer.setText("Не корректный идентификатор пользователя. Возможно такой идентификатор закреплен за другим аккаунтом Telegram. Ознакомтесь со справкой /help");
                    }
                } else if (arguments[0].trim().toLowerCase().equals("subscribe")) {
                    String actionSubscribe = arguments[1].trim().toLowerCase();
                    switch (actionSubscribe) {
                        case "yes":
                        case "true":
                        case "1":
                        case "да":
                        case "on":
                            try {
                                Application.databaseManager.setSetting(UID, "subscribe", "true");
                                answer.setText("Пользователя подписан");
                            } catch (Exception ex) {
                                BotLogger.error(LOGTAG, ex);
                            }   
                            break;
                        case "no":
                        case "false":
                        case "0":
                        case "нет":
                        case "off":
                            try {
                                Application.databaseManager.setSetting(UID, "subscribe", "false");
                                answer.setText("Пользователя отписан");
                            } catch (Exception ex) {
                                BotLogger.error(LOGTAG, ex);
                            }   
                            break;
                        default:
                            answer.setText("Не корректный задан параметр. Ознакомтесь со справкой /help");
                            break;
                    }
                } else if (arguments[0].trim().toLowerCase().equals("logon")) {              
                    if ((arguments.length==3)&& (arguments[1].contains("@") || arguments[2].contains("@"))) {
                        
                    } else if (arguments.length==1){
                        try {
                            answer.setText("Введите E-Mail:");
                        } 
                        catch (Exception ex) {
                            BotLogger.error(LOGTAG, ex);
                        } 
                    } else {
                        try {
                                answer.setText("Не верный формат команды. Ознакомтесь со справкой /help ");
                        } 
                        catch (Exception ex) {
                                BotLogger.error(LOGTAG, ex);
                        }  
                    }                        
                } else if (arguments[0].trim().toLowerCase().equals("get") && arguments.length==2) {
                    answer.enableMarkdown(false);    
                    String key = arguments[1].trim();                   
                    try {
                        if (key.toLowerCase().equals("location")) {
                            String latitude= Application.databaseManager.getSettings(UID, "latitude").trim();
                            String longitude= Application.databaseManager.getSettings(UID, "longitude").trim();
                            String locationMessage = "Ваше месторасположение:\n";
                            locationMessage += "https://www.google.com/maps/@"+latitude+","+longitude+",12z";
                            answer.setText(locationMessage);                            
                        } else {
                            String result = "Результат выполнения команды get:\n";
                            result += Application.databaseManager.getSettings(UID, key);
                            answer.setText(result);
                        }
                    } catch (Exception ex) {
                        answer.setText(ex.getMessage());
                    }
                }
                else if (arguments[0].trim().toLowerCase().equals("set") && arguments.length>=2) {
                    answer.enableMarkdown(false);    
                    try {
                        String value = "";
                        for (int i = 2; i < arguments.length; i++) {
                            value+=arguments[i]+" ";
                        }  
                        value=value.trim();
                        String key = arguments[1].trim();
                        if (key.toLowerCase().equals("location")) {
                            try {                                 
                                answer.setText("Ваше месторасположение");
                                ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
                                List<KeyboardRow> keyboardRow = new ArrayList<>();
                                keyboard.setKeyboard(keyboardRow);
                                keyboardRow.add(new KeyboardRow());
                                KeyboardButton keyboardButton = new KeyboardButton("Оправить месторасположение");
                                keyboardButton.setRequestLocation(Boolean.TRUE);
                                keyboardRow.get(0).add(keyboardButton);
                                answer.setReplyMarkup(keyboard);          
                            } catch (Exception ex) {
                                BotLogger.error(LOGTAG, ex);
                                answer.setText("Не выполнено!");
                            }                            
                        }
                        else if (key.toLowerCase().startsWith("subscribe")) {
                            switch (value) {
                                case "yes":
                                case "true":
                                case "1":
                                case "да":
                                case "on":
                                    try {
                                        Application.databaseManager.setSetting(UID, key, "true");
                                        answer.setText("Выполнено!");
                                    } catch (Exception ex) {
                                        BotLogger.error(LOGTAG, ex);
                                        answer.setText("Не выполнено!");
                                    }   
                                    break;
                                case "no":
                                case "false":
                                case "0":
                                case "нет":
                                case "off":
                                    try {
                                        Application.databaseManager.setSetting(UID, key, "false");
                                        answer.setText("Выполнено!");
                                    } catch (Exception ex) {
                                        BotLogger.error(LOGTAG, ex);
                                        answer.setText("Не выполнено!");
                                    }   
                                    break;
                            }
                            
                        }
                        else{ 
                            Application.databaseManager.setSetting(UID, key,value);
                            answer.setText("Выполнено!");
                        }
                    } catch (Exception ex) {
                        answer.setText("Не выполнено!");
                    }
                }
                else if (arguments[0].trim().toLowerCase().equals("execute")) {
                    answer.enableMarkdown(false);    
                    try {
                        String value = "";
                        for (int i = 1; i < arguments.length; i++) {
                            value+=arguments[i]+" ";
                        } 
                        String result = "Результат выполнения команды execute:\n";
                        result += Application.databaseManager.executeQuery(value.trim());
                        answer.setText(result);
                    } catch (Exception ex) {
                        answer.setText(ex.getMessage());
                    }
                }                
                else if (arguments[0].trim().toLowerCase().equals("update") && (arguments.length>1) && arguments[1].trim().toLowerCase().equals("version")) {
                    try {          
                        Application.databaseManager.updateVersion();
                        answer.setText("Обновление базы завершено");
                    } catch (Exception ex) {
                        answer.setText(ex.getMessage());
                    }
                }
                else if (arguments[0].trim().toLowerCase().equals("version")) {
                    try {          
                        String version=Application.databaseManager.getVersion();
                        answer.setText("Версия бота: "+version);
                    } catch (Exception ex) {
                        answer.setText(ex.getMessage());
                    }
                }
//                else if (arguments[0].trim().toLowerCase().equals("test")) {
//                    answer.enableMarkdown(false); 
//                    answer.enableWebPagePreview();
//                    answer.setText("https://www.google.com/maps?q=46.496420,30.724046&ll=46.496420,30.724046&z=16");
//                }
            }   
            absSender.sendMessage(answer);
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }

    public static String getSettingsButtonsText(Long ChatId) {
        Integer UID=Application.databaseManager.getUserId(ChatId);
        String UserId =Application.databaseManager.getSiwatcherUserId(ChatId);
        Boolean Subscribe = false;
        Boolean SubscribeTypeText_deleted = true;
        Boolean SubscribeTypeText_updated = true;
        Boolean SubscribeTypeText_updatedSize_incremented = false;
        Boolean SubscribeTypeAuthor_typed = true;
        try { 
            Subscribe = ((UID!=null) && Application.databaseManager.getSetting(UID, "subscribe").equals("true"));
            SubscribeTypeText_deleted = ((UID!=null) && Application.databaseManager.getSetting(UID, "subscribe.type.text_deleted").equals("true"));
            SubscribeTypeText_updated = ((UID!=null) && Application.databaseManager.getSetting(UID, "subscribe.type.text_updated").equals("true"));
            SubscribeTypeText_updatedSize_incremented = ((UID!=null) && Application.databaseManager.getSetting(UID, "subscribe.type.text_update.size_incremented").equals("true"));
            SubscribeTypeAuthor_typed = ((UID!=null) && Application.databaseManager.getSetting(UID, "subscribe.type.author_typed").equals("true"));
        } catch (Exception ex) {
            BotLogger.error(LOGTAG, ex);
        }

        String text = "`Выберите желаемое действие`\n";
        text +="\t *ID пользователя:* "+(("".equals(UserId))?"Не установлен":UserId)+"\n";
        text +="\t *Подписка:* "+(Subscribe?"Подписан":"Не подписан")+"\n";     
        text +="\t\t *Подписка на удаление текстов:* "+(SubscribeTypeText_deleted?"Включено":"Выключено")+"\n";     
        text +="\t\t *Подписка на обновление текстов:* "+(SubscribeTypeText_updated?"Включено":"Выключено")+"\n";     
        text +="\t\t *Подписка только на увеличение размера текста:* "+(SubscribeTypeText_updatedSize_incremented?"Включено":"Выключено")+"\n";     
        text +="\t\t *Подписка на появление новых произведений:* "+(SubscribeTypeAuthor_typed?"Включено":"Выключено")+"\n";     

        return text;
    }
    
    public static InlineKeyboardMarkup getSettingsKeyboards(Long ChatId){
        Integer UID=Application.databaseManager.getUserId(ChatId);
//        String UserId =Application.databaseManager.getSiwatcherUserId(ChatId);
        Boolean Subscribe = false;
        Boolean SubscribeTypeText_deleted = true;
        Boolean SubscribeTypeText_updated = true;
        Boolean SubscribeTypeText_updatedSize_incremented = false;
        Boolean SubscribeTypeAuthor_typed = true;
        try { 
            Subscribe = ((UID!=null) && Application.databaseManager.getSetting(UID, "subscribe").equals("true"));
            SubscribeTypeText_deleted = ((UID!=null) && Application.databaseManager.getSetting(UID, "subscribe.type.text_deleted").equals("true"));
            SubscribeTypeText_updated = ((UID!=null) && Application.databaseManager.getSetting(UID, "subscribe.type.text_updated").equals("true"));
            SubscribeTypeText_updatedSize_incremented = ((UID!=null) && Application.databaseManager.getSetting(UID, "subscribe.type.text_update.size_incremented").equals("true"));
            SubscribeTypeAuthor_typed = ((UID!=null) && Application.databaseManager.getSetting(UID, "subscribe.type.author_typed").equals("true"));
        } catch (Exception ex) {
            BotLogger.error(LOGTAG, ex);
        }
        
        InlineKeyboardMarkup inlineKeyboard = new InlineKeyboardMarkup();            
        List<List<InlineKeyboardButton>> listListKeyboard = new ArrayList<>();
        inlineKeyboard.setKeyboard(listListKeyboard);
        List<InlineKeyboardButton> listKeyboardRow1 = new ArrayList<>();
        listListKeyboard.add(listKeyboardRow1);

        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Задание идентификатора пользователя");
        inlineKeyboardButton1.setCallbackData("setuserid");
        listKeyboardRow1.add(inlineKeyboardButton1);

        List<InlineKeyboardButton> listKeyboardRow2 = new ArrayList<>();
        listListKeyboard.add(listKeyboardRow2);

        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        if (Subscribe) {
            inlineKeyboardButton2.setText("Отписаться");
            inlineKeyboardButton2.setCallbackData("subscribe:off");
        } else {
            inlineKeyboardButton2.setText("Подписаться");
            inlineKeyboardButton2.setCallbackData("subscribe:on");
        }
        listKeyboardRow2.add(inlineKeyboardButton2);   
        
        List<InlineKeyboardButton> listKeyboardRow3 = new ArrayList<>();
        listListKeyboard.add(listKeyboardRow3);
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        if (SubscribeTypeText_deleted) {
            inlineKeyboardButton3.setText("Подписка на удаление текстов: Выключить");
            inlineKeyboardButton3.setCallbackData("subscribe.type.text_deleted:off");
        } else {
            inlineKeyboardButton3.setText("Подписка на удаление текстов: Включить");
            inlineKeyboardButton3.setCallbackData("subscribe.type.text_deleted:on");
        }
        listKeyboardRow3.add(inlineKeyboardButton3);    
         
        List<InlineKeyboardButton> listKeyboardRow4 = new ArrayList<>();
        listListKeyboard.add(listKeyboardRow4);
        InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();
        if (SubscribeTypeText_updated) {
            inlineKeyboardButton4.setText("Подписка на обновление текстов: Выключить");
            inlineKeyboardButton4.setCallbackData("subscribe.type.text_updated:off");
        } else {
            inlineKeyboardButton4.setText("Подписка на обновление текстов: Включить");
            inlineKeyboardButton4.setCallbackData("subscribe.type.text_updated:on");
        }
        listKeyboardRow4.add(inlineKeyboardButton4);    
         
        List<InlineKeyboardButton> listKeyboardRow5 = new ArrayList<>();
        listListKeyboard.add(listKeyboardRow5);
        InlineKeyboardButton inlineKeyboardButton5 = new InlineKeyboardButton();
        if (SubscribeTypeText_updatedSize_incremented) {
            inlineKeyboardButton5.setText("Подписка только на увеличение размера текста: Выключить");
            inlineKeyboardButton5.setCallbackData("subscribe.type.text_update.size_incremented:off");
        } else {
            inlineKeyboardButton5.setText("Подписка только на увеличение размера текста: Включить");
            inlineKeyboardButton5.setCallbackData("subscribe.type.text_update.size_incremented:on");
        }
        listKeyboardRow5.add(inlineKeyboardButton5);    
         
        List<InlineKeyboardButton> listKeyboardRow6 = new ArrayList<>();
        listListKeyboard.add(listKeyboardRow6);
        InlineKeyboardButton inlineKeyboardButton6 = new InlineKeyboardButton();
        if (SubscribeTypeAuthor_typed) {
            inlineKeyboardButton6.setText("Подписка на появление новых произведений: Выключить");
            inlineKeyboardButton6.setCallbackData("subscribe.type.author_typed:off");
        } else {
            inlineKeyboardButton6.setText("Подписка на появление новых произведений: Включить");
            inlineKeyboardButton6.setCallbackData("subscribe.type.author_typed:on");
        }
        listKeyboardRow6.add(inlineKeyboardButton6);    
         
        return inlineKeyboard;
    }
}
