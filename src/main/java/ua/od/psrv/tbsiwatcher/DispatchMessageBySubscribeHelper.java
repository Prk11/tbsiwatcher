/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.od.psrv.tbsiwatcher;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.telegram.telegrambots.logging.BotLogger;
import ua.od.psrv.tbsiwatcher.events.EventSendResponseObject;
import ua.od.psrv.tbsiwatcher.events.EventSendText;
import ua.od.psrv.tbsiwatcher.model.ListResponseObject;

/**
 *
 * @author Prk
 */
public class DispatchMessageBySubscribeHelper implements Runnable {
    
    private static final String LOGTAG = "DATABASEMANAGERHELPER";
    
    private List<ListResponseObject> listAsList; 
    private Integer pagesize; 
    private Long timezoneDefault; 
    private Long timezone; 
    private Long CharId;    
    private EventSendResponseObject eventResponseObject;
    private EventSendText eventSendText;    

    public List<ListResponseObject> getListAsList() {
        return listAsList;
    }

    public void setListAsList(List<ListResponseObject> listAsList) {
        this.listAsList = listAsList;
    }

    public Integer getPagesize() {
        return pagesize;
    }

    public void setPagesize(Integer pagesize) {
        this.pagesize = pagesize;
    }

    public Long getTimezoneDefault() {
        return timezoneDefault;
    }

    public void setTimezoneDefault(Long timezoneDefault) {
        this.timezoneDefault = timezoneDefault;
    }

    public Long getTimezone() {
        return timezone;
    }

    public void setTimezone(Long timezone) {
        this.timezone = timezone;
    }

    public Long getCharId() {
        return CharId;
    }

    public void setCharId(Long CharId) {
        this.CharId = CharId;
    }

    public EventSendResponseObject getEventResponseObject() {
        return eventResponseObject;
    }

    public void setEventResponseObject(EventSendResponseObject event) {
        this.eventResponseObject = event;
    }
    
    public EventSendText getEventSendText() {
        return eventSendText;
    }

    public void setEventSendText(EventSendText eventSendText) {
        this.eventSendText = eventSendText;
    }

    public DispatchMessageBySubscribeHelper() {
    }

    public DispatchMessageBySubscribeHelper(List<ListResponseObject> listAsList, Integer pagesize, Long timezoneDefault, Long timezone, Long CharId, EventSendResponseObject eventResponseObject, EventSendText eventSendText) {
        this.listAsList = listAsList;
        this.pagesize = pagesize;
        this.timezoneDefault = timezoneDefault;
        this.timezone = timezone;
        this.CharId = CharId;
        this.eventResponseObject = eventResponseObject;
        this.eventSendText = eventSendText;
    }

    
    
    @Override
    public void run() {
        int cnt=0;
        for (ListResponseObject listResponseObject : listAsList) {
            if (this.eventResponseObject!=null) {
                if (listResponseObject.getTime()!=null) {
                    listResponseObject.setTime(listResponseObject.getTime()-timezoneDefault+timezone);
                }
                this.eventResponseObject.onSendMessage(CharId, listResponseObject);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    BotLogger.error(LOGTAG, ex);
                }
            }
            cnt++;
            if (cnt>pagesize) {
                    cnt=0;
                    if (this.eventSendText!=null) this.eventSendText.onSendMessage(CharId, "*Автоматическая пауза на 90 секунд!*");
                    try {
                       Thread.sleep(90000);
                    } catch (InterruptedException ex) {
                        BotLogger.error(LOGTAG, ex);
                    }
            }
        }    
    }
    
    
}
