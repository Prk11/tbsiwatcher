/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.od.psrv.tbsiwatcher.commands;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
        Long timezoneDefault = 10800L;
        try {
            timezoneDefault = Long.parseLong(Application.settings.getProperties().getProperty("timezone"));
        } catch (Exception ex) {}
        Long timezone = timezoneDefault;
        try {                    
            Integer UID = Application.databaseManager.getUserId(chat.getId());
            timezone = Long.parseLong(Application.databaseManager.getSetting(UID, "timezone"));
        } catch (Exception ex) {
            timezone=timezoneDefault;
        }
        
        Boolean autofilteringdisable=false;
        Boolean autofiltering=false;
        Boolean paginate=false;
        Boolean fromdate=false;
        Boolean random=false;
        try {
            String UserId = Application.databaseManager.getSiwatcherUserId(chat.getId());
            Set<ListResponseObject> list =new HashSet<>();
            Date fromday=new Date(System.currentTimeMillis()-24L*3600L*1000L);
            Integer pagenum=1;
            Integer randomnum=3;
            int cnt=0;
            for (String argument : arguments) {               
                switch (argument.toLowerCase()) {
                    case "lastday" : {
                        list = SiwatcherManager.getResponseByDay();
                        break;
                    }
                    case "autofiltering=no":
                    case "autofiltering=false":
                    case "autofiltering=0": 
                    {
                        autofilteringdisable=true;
                        break;
                    }
                    case "autofiltering=yes":
                    case "autofiltering=true":
                    case "autofiltering=1": 
                    {
                        autofiltering=true;
                        break;
                    }
                    case "paginate":
                    case "page":
                    case "pages":
                    {
                        paginate=true;
                        break;
                    }
                    case "random":
                    {
                        random=true;
                        break;
                    }
                    case "fromdate":
                    case "fromday":
                    {
                        fromdate=true;
                        break;
                    }
                    default:{
                        try {
                            if (random && arguments[cnt-1].toLowerCase().equals("random"))
                                randomnum=Integer.parseInt(argument);
                        } catch (NumberFormatException ex) {}
                        try {
                            if (paginate && (
                                    arguments[cnt-1].toLowerCase().equals("paginate") ||
                                    arguments[cnt-1].toLowerCase().equals("page") ||
                                    arguments[cnt-1].toLowerCase().equals("pages")
                                    ))
                                pagenum=Integer.parseInt(argument);
                        } catch (NumberFormatException ex) {}
                        try {
                            if (fromdate && (arguments[cnt-1].toLowerCase().equals("fromdate") || (arguments[cnt-1].toLowerCase().equals("fromday")))) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");
                                fromday=sdf.parse(argument);
                            }
                        } catch (ParseException ex) {}
                    }
                }
                cnt++;
            }
            if (fromdate) {
                list = SiwatcherManager.getResponseFromDay(fromday);
            } else if (list.isEmpty()) {
                list = SiwatcherManager.getResponse(UserId, false);
            }  
            List<ListResponseObject> listAsList = new ArrayList<>(list);
            Collections.sort(listAsList);
            Integer pagesize = Integer.parseInt(Application.settings.getProperties().getProperty("pagesize"));
            cnt=0;
            Integer missedMessages = 0;
            if (autofiltering || ((listAsList.size()>pagesize) && !autofilteringdisable)) {
                autofiltering=true;
                SendMessage _sendMessage = new SendMessage();
                _sendMessage.setChatId(chat.getId().toString());
                _sendMessage.enableMarkdown(true);
                _sendMessage.setText("*Автоматическая фильтрация включена! Показываются только новые тексты*");
                absSender.sendMessage(_sendMessage);
                Thread.sleep(2000);
                
                // Очищаем список от отфильтрованых значений
                while (listAsList.size()>cnt) {
                    ListResponseObject listResponseObject = listAsList.get(cnt);
                    if (
                            (listResponseObject.getType()==null) || 
                            (
                                (listResponseObject.getType()==1) && (listResponseObject.getSize()<=listResponseObject.getOldsize())
                            )                           
                       ) {
                        missedMessages++;
                        listAsList.remove(cnt);
                        continue;
                    }
                    cnt++;
                }
            }           

            //Отработка рулетки
            if (random && (randomnum>0)) {
                for (int i = 0; i < randomnum; i++) {
                    int randomcurrent = (new Double(Math.random() * Double.parseDouble((new Integer(listAsList.size())).toString()))).intValue();
                    ListResponseObject item=listAsList.get(i);
                    listAsList.set(i, listAsList.get(randomcurrent));
                    listAsList.set(randomcurrent,item);
                }
                listAsList.subList(randomnum-1, listAsList.size()-1).clear();
            }            
            
            Integer messagefail=0;
            cnt=0;
            int cnt2=-1;
            Integer pages = listAsList.size()/pagesize;
            if (listAsList.size()%pagesize>0) pages++;
            if (paginate &&((pages<pagenum )|| (pagenum<1))) {
                SendMessage _sendMessage = new SendMessage();
               _sendMessage.setChatId(chat.getId().toString());
               _sendMessage.enableMarkdown(true);
               _sendMessage.setText("*Номер страницы должен быть от 1 до "+pages.toString()+"!*");
               absSender.sendMessage(_sendMessage);
               return;
            }
 
            
            for (ListResponseObject listResponseObject : listAsList) {
                //Механизм отображения только текущей страницы
                cnt2++;
                if (paginate) {
                    if ((pagenum-1)*pagesize>cnt2) continue;
                    if (pagenum*pagesize<cnt2) break;                        
                }
                
                cnt++;
                if (!paginate && (cnt>pagesize)) {
                    cnt=0;
                    SendMessage _sendMessage = new SendMessage();
                   _sendMessage.setChatId(chat.getId().toString());
                   _sendMessage.enableMarkdown(true);
                   _sendMessage.setText("*Автоматическая пауза на 90 секунд!*");
                   absSender.sendMessage(_sendMessage);
                   Thread.sleep(90000);
                }
                if (listResponseObject.getTime()!=null) {
                    listResponseObject.setTime(listResponseObject.getTime()-timezoneDefault+timezone);
                }
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
                        Thread.sleep(2000);
                        sendMessage.enableMarkdown(false);
                        listResponseObject.setMarkup(false);
                        sendMessage.setText(listResponseObject.toString());
                        absSender.sendMessage(sendMessage);
                    } catch (TelegramApiException ex1) {
                        messagefail++;
                    }
                 }
                Thread.sleep(200);
            }
            if (listAsList.isEmpty()) {
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
            } else {
                SendMessage _sendMessage = new SendMessage();
                _sendMessage.setChatId(chat.getId().toString());
                _sendMessage.enableMarkdown(true);
                if (messagefail==0){
                    if (paginate) {
                        _sendMessage.setText("*Страница "+pagenum.toString()+" из "+ pages.toString() +".\n Вывод завершен!*");
                    } else {
                        _sendMessage.setText("*Вывод завершен!*");
                    }
                }
                else {
                    if (paginate) {
                        _sendMessage.setText("*Страница "+pagenum.toString()+" из "+ pages.toString() +".\n Вывод завершен!*\nЗаблокировано сообщений: "+messagefail.toString());
                    } else {
                        _sendMessage.setText("*Вывод завершен!* \nЗаблокировано сообщений: "+messagefail.toString());
                    }
                }
                absSender.sendMessage(_sendMessage);
            }
            if (autofiltering && (missedMessages>0)) {
                SendMessage _sendMessage = new SendMessage();
                _sendMessage.setChatId(chat.getId().toString());
                _sendMessage.enableMarkdown(true);
                _sendMessage.setText("Отфильтровано сообщений: "+missedMessages.toString());
                absSender.sendMessage(_sendMessage);
            }
        } catch (InterruptedException ex) {
            BotLogger.error(LOGTAG, ex);
        } catch (IOException ex) {
            BotLogger.error(LOGTAG, ex);            
            try {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chat.getId().toString());
                sendMessage.enableMarkdown(false);
                if (fromdate)
                    sendMessage.setText("Не удалось загрузить список обновлений. Возможно на сервере siwatcher.ru отсутсвуют списки обновлений на указанную дату");
                else
                    sendMessage.setText("Не удалось загрузить список обновлений. Возможно не верно указан ID пользователя. Ознакомтесь со справкой /help");
                absSender.sendMessage(sendMessage);
            } catch (TelegramApiException ex1) {
                BotLogger.error(LOGTAG, ex1);
            }
        } catch (TelegramApiException ex) {
            BotLogger.error(LOGTAG, ex);
        } 
        
    }

}
