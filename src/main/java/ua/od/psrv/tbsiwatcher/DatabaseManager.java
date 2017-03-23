/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.od.psrv.tbsiwatcher;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.telegram.telegrambots.logging.BotLogger;
import ua.od.psrv.tbsiwatcher.model.ListResponseObject;
import ua.od.psrv.tbsiwatcher.events.EventSendResponseObject;
import ua.od.psrv.tbsiwatcher.events.EventSendText;

/**
 * Управление базой данных
 * @author Prk
 */
public class DatabaseManager {
    
    private static final String LOGTAG = "DATABASEMANAGER";
     
    private Connection connection = null;
    
    public DatabaseManager() {        
        try {
            Class.forName("org.sqlite.JDBC");
            String jdbc = "jdbc:sqlite:" + Application.settings.getProperties().getProperty("database");
            connection = DriverManager.getConnection(jdbc);
        } catch (ClassNotFoundException | SQLException ex) {
            BotLogger.error(LOGTAG, ex);
        }       
    }

    /**
     * Возвращает подключение
     * @return 
     */
    public Connection getConnection() {
        return connection;
    }
    
    /**
     * Регистрирует последний вход пользователя
     * @param chatid 
     */
    public void registryVisitUser(String chatid) {
        try {
            try (Statement statmt = connection.createStatement()) {
                statmt.executeUpdate("update users set lastaccess=datetime('now') where chat_id=" + chatid);
            }
        } catch (SQLException ex) {
            BotLogger.error(LOGTAG, ex);
        }
    }
    
    /**
     * Регистраци о прочитанном событии
     * @param userid
     * @param event_id
     * @return 
     */
    public  boolean printEvent(String userid, int event_id) {
        try {
            try (Statement statmt = connection.createStatement()) {
                ResultSet rs = statmt.executeQuery("select id from users where user_id='" + userid + "'");
                if (rs.next()) {
                    int id = rs.getInt(1);
                    rs.close();
                    rs = statmt.executeQuery("select id from events where (userid=" + Integer.toString(id) + ") and (event_id=" + Integer.toString(event_id) + ")");
                    if (rs.next()) {
                        rs.close();
                        statmt.close();
                        return true;
                    } else {
                        rs.close();
                        statmt.executeUpdate("insert into events (userid, event_id, timeread) values (" + Integer.toString(id) + "," + Integer.toString(event_id) + ",datetime('now'))");
                        statmt.close();
                        return false;
                    }
                }
            }
        } catch (SQLException ex) {
            BotLogger.error(LOGTAG, ex);
        }
        return false;
    }
        
    
    private EventSendResponseObject eventResponseObject;

    public EventSendResponseObject getEventResponseObject() {
        return eventResponseObject;
    }

    public void setEventResponseObject(EventSendResponseObject event) {
        this.eventResponseObject = event;
    }
    
    private EventSendText eventSendText;    

    public EventSendText getEventSendText() {
        return eventSendText;
    }

    public void setEventSendText(EventSendText eventSendText) {
        this.eventSendText = eventSendText;
    }
    
    
    /**
     * Рассылка сообщения всем зарегистрированным пользователям
     */
    public synchronized void DispatchMessageBySubscribe() {
        try {
            Statement statmt = connection.createStatement();
            ResultSet rs = statmt.executeQuery("select u.user_id, u.chat_id from users u inner join settings s on (u.id=s.userid) where ([key]='subscribe') and ([valuekey]='true')");
            while (rs.next()) {
                Set<ListResponseObject> list=null;
                try {
                    list = SiwatcherManager.getResponse(rs.getString(1), true);
                } catch (SQLException ex) {
                    BotLogger.error(LOGTAG, ex);
                    if (this.eventSendText!=null)
                        this.eventSendText.onSendMessage(rs.getLong(2), "Не верно установленый идентификатор пользователя. Осзнакомтесь со справкой /help");
                }
                for (ListResponseObject listResponseObject : list) {
                    if (this.eventResponseObject!=null)
                        this.eventResponseObject.onSendMessage(rs.getLong(2), listResponseObject);
                }
            }
            rs.close();
            statmt.close();
        } catch (InterruptedException | IOException | SQLException ex) {
            BotLogger.error(LOGTAG, ex);
        }
    }
    
    /**
     * Возвращает идентификатор пользователя
     * @param ChatId
     * @return 
     */
    public Integer getUserId(Long ChatId) {
        Integer id =  null;
        try {
            Statement statmt = connection.createStatement();
            ResultSet rs = statmt.executeQuery("select id from users where chat_id=" + ChatId.toString());
            if (rs.next()) {
                id = rs.getInt(1);
            }
            rs.close();
            statmt.close();
        } catch (SQLException ex) {
            BotLogger.error(LOGTAG, ex);
        }
        return id;
    }
    
    public Integer getUserIdBySiwatcherUserId(String UserId) {
        Integer id =  null;
        try {
            Statement statmt = connection.createStatement();
            ResultSet rs = statmt.executeQuery("select id from users where user_id='" + UserId.toString()+"'");
            if (rs.next()) {
                id = rs.getInt(1);
            }
            rs.close();
            statmt.close();
        } catch (SQLException ex) {
            BotLogger.error(LOGTAG, ex);
        }
        return id;
    }
    
    public String getSiwatcherUserId(Long ChatId) {
        String user_id =  "";
        try {
            Statement statmt = connection.createStatement();
            ResultSet rs = statmt.executeQuery("select user_id from users where chat_id=" + ChatId.toString());
            if (rs.next()) {
                user_id = rs.getString(1);
            }
            rs.close();
            statmt.close();
        } catch (SQLException ex) {
            BotLogger.error(LOGTAG, ex);
        }
        return user_id;
    }
    
    /**
     * Связывает чат с пользователем SIWATCHER
     * @param ChatId
     * @param UserId
     * @throws IOException 
     */
    public void linkCharAndUser(Long ChatId, String UserId) throws IOException, SQLException {
        // Для проверки коректности UserId
        URL url = new URL(String.format(
            Application.settings.getProperties().getProperty("url1"),
            Application.settings.getProperties().getProperty("appid"),
            UserId));
        URLConnection conn = url.openConnection();
        conn.getContent();
         
        Integer id = getUserId(ChatId);
        String sql;
        if (id==null) {
            sql="insert into users (user_id,chat_id) values ('" + UserId + "'," + ChatId.toString() + ")";
        }
        else {
            sql="update users SET user_id='" + UserId + "' where chat_id=" + ChatId.toString();
        }
        
        Statement statmt = connection.createStatement();
        statmt.executeUpdate(sql);          
        statmt.close();  
        if (id==null) {
            id = getUserId(ChatId);
            try {
                this.setDefaultUserSettings(id, true);
            } catch (Exception ex) {
                BotLogger.error(LOGTAG, ex);
            }
        }
    }
    
    /**
     * Задание настроек по-умолчанию для нового пользователя
     * @param UserId - идентификатор нового пользователя
     */
    public void setDefaultUserSettings(Integer UserId, Boolean rewrite) throws Exception {
        if (UserId==null) 
            throw new Exception("Отсутствует привязанный пользователь");
        try {
            Statement statmt = connection.createStatement();
            String _part_sql_for_rewrite = "";
            if (rewrite)
                statmt.executeUpdate("delete from settings where (userid=" + UserId.toString() + ")"); 
            else
                _part_sql_for_rewrite = "OR IGNORE ";
            // Подписка на обновления
            statmt.executeUpdate("insert "+_part_sql_for_rewrite+"into settings (userid,key,valuekey) values (" + UserId.toString() + ",'subscribe','true')");          
            // Указание на какие события мы подписались
            statmt.executeUpdate("insert "+_part_sql_for_rewrite+"into settings (userid,key,valuekey) values (" + 
                    UserId.toString() + ",'subscribe.type.text_deleted','true')");          
            statmt.executeUpdate("insert "+_part_sql_for_rewrite+"into settings (userid,key,valuekey) values (" + 
                    UserId.toString() + ",'subscribe.type.text_updated','true')");          
            statmt.executeUpdate("insert "+_part_sql_for_rewrite+"into settings (userid,key,valuekey) values (" + 
                    UserId.toString() + ",'subscribe.type.text_update.size_incremented','true')");          
            statmt.executeUpdate("insert "+_part_sql_for_rewrite+"into settings (userid,key,valuekey) values (" + 
                    UserId.toString() + ",'subscribe.type.author_typed','true')");                      
            statmt.close();
        } catch (SQLException ex) {
            BotLogger.error(LOGTAG, ex);
        }
    }
    
    /**
     * Запись пользовательских настроек
     * @param UserId
     * @param paramName
     * @param paramValue
     * @throws Exception 
     */
    public void setSetting(Integer UserId, String paramName, String paramValue) throws Exception {
        if (UserId==null) 
            throw new Exception("Отсутствует привязанный пользователь");
        try {
            Statement statmt = connection.createStatement();
            // Подписка на обновления
            statmt.executeUpdate("update settings set valuekey='"+paramValue.trim()+"' where (userid=" + UserId.toString() + ") and (key='"+paramName.trim().toLowerCase()+"')");          
            statmt.close();
        } catch (SQLException ex) {
            BotLogger.error(LOGTAG, ex);
        }
    }
    
    public void updateVersion() {       
        try {
            Statement statmt = connection.createStatement();
            ResultSet rs = statmt.executeQuery("select [valuekey] from settings where ([key]='version') and (userid=0)"); 
            if (rs.next()) {
                rs.close();
                return;
            }
            rs.close();
            statmt.executeUpdate("create table new_events (id INTEGER PRIMARY KEY AUTOINCREMENT, [event_id] INTEGER NOT NULL, [userid] INTEGER NOT NULL, timeread TIMESTAMP)");
            statmt.executeUpdate("INSERT INTO new_events ([event_id],[userid],timeread) SELECT [event_id],[userid],timeread FROM events");
            statmt.executeUpdate("DROP TABLE events");
            statmt.executeUpdate("ALTER TABLE new_events RENAME TO events");
            statmt.executeUpdate("create unique index ind_userid_event on [events]([userid],[event_id])");
            statmt.executeUpdate("create unique index ind_userid_key on [settings]([userid],[key])");
            statmt.executeUpdate("delete from settings where ([key]='version')"); 
            statmt.executeUpdate("insert into settings ([valuekey],[key],userid) values ('1.1.0', 'version', 0 )"); 
            rs = statmt.executeQuery("select id from users"); 
            while (rs.next()) {                
                try {
                    this.setDefaultUserSettings(rs.getInt(1), false);
                } catch (Exception ex) {
                    BotLogger.error(LOGTAG, ex);
                }
            }
            rs.close();
            statmt.close();
        } catch (SQLException ex) {
            BotLogger.error(LOGTAG, ex);
        }
    }
    
    
    public String getSetting(Integer UserId, String paramName) throws Exception {
        if (UserId==null) 
            throw new Exception("Отсутствует привязанный пользователь");
        String result="";
        try {
            Statement statmt = connection.createStatement();           
            ResultSet rs = statmt.executeQuery("select valuekey from settings where (userid=" + UserId.toString() + ") and (key='"+paramName.trim().toLowerCase()+"')");          
            if (rs.next()) {                
                result=rs.getString(1);
            }
            rs.close();
            statmt.close();
        } catch (SQLException ex) {
            BotLogger.error(LOGTAG, ex);
        }
        return result;
    }
    
    public String getSettings(Integer UserId, String paramName) throws Exception {
        if (UserId==null) 
            throw new Exception("Отсутствует привязанный пользователь");
        String result="";
        try {
            Statement statmt = connection.createStatement();           
            ResultSet rs = statmt.executeQuery("select valuekey from settings where (userid=" + UserId.toString() + ") and (key='"+paramName.trim().toLowerCase()+"')");          
            while (rs.next()) {                
                result+=rs.getString(1)+"\n";
            }
            rs.close();
            statmt.close();
        } catch (SQLException ex) {
            BotLogger.error(LOGTAG, ex);
        }
        return result;
    }   
    
    /**
     * Позволяет вернуть результат любого sql запроса
     * @param sqlQuery
     * @return 
     */
    public String executeQuery(String sqlQuery) {        
        String result="";
        try {
            Statement statmt = connection.createStatement();           
            ResultSet rs = statmt.executeQuery(sqlQuery);  
            int fieldcount = rs.getMetaData().getColumnCount();
            for (int i = 1; i < fieldcount+1; i++) {
                result+=rs.getMetaData().getColumnName(i)+"\t";
            }
            result+="\n";
            while (rs.next()) {   
                for (int i = 1; i < fieldcount+1; i++) {
                    result+=rs.getString(i)+"\t";
                }
                result+="\n";
            }
            rs.close();
            statmt.close();
        } catch (SQLException ex) {
            BotLogger.error(LOGTAG, ex);
        }
        return result;
    }
    
    public List<Long> getAllChats() {
        List<Long> result = new ArrayList<>();
        try {           
            Statement statmt = connection.createStatement();
            ResultSet rs = statmt.executeQuery("select chat_id from users");
            while (rs.next()) {
                result.add(rs.getLong(1));
            }
            rs.close();
            statmt.close();
        } catch (SQLException ex) {
            BotLogger.error(LOGTAG, ex);
        }
        return result;
    }
    
    public Integer getCountUsers() {
        Integer result = 0;
        try {
            Statement statmt = connection.createStatement();
            ResultSet rs = statmt.executeQuery("select count(*) from users");           
            if (rs.next()) {
                result = rs.getInt(1);
            }
            rs.close();
            statmt.close();
        } catch (SQLException ex) {
            BotLogger.error(LOGTAG, ex);
        }
        return result;
    }
    
    public String getVersion(){
        String result = "1.0.0";
        try {
            Statement statmt = connection.createStatement();
            ResultSet rs = statmt.executeQuery("select [valuekey] from settings where ([key]='version') and (userid=0)"); 
            if (rs.next()) {
                result = rs.getString(1);
            }
            rs.close();
            statmt.close();
        } catch (SQLException ex) {
            BotLogger.error(LOGTAG, ex);
        }
        return result;
    }
}
