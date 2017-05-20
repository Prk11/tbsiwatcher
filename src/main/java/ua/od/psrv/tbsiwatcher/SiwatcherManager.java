/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.od.psrv.tbsiwatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.api.objects.Location;
import org.telegram.telegrambots.logging.BotLogger;
import ua.od.psrv.tbsiwatcher.model.GoogleTimezoneLocation;
import ua.od.psrv.tbsiwatcher.model.ListResponseObject;

/**
 *
 * @author Prk
 */
public class SiwatcherManager {
    private static final String LOGTAG = "SIWATCHERMANAGER";
    
    /**
     * Возвращает перечень обновлений в кабинете
     * @param user_id
     * @param filtering
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    public static Set<ListResponseObject> getResponse(String user_id, Boolean filtering) throws IOException, InterruptedException {
        Set<ListResponseObject> result = new HashSet();
        URL url = new URL(String.format(
            Application.settings.getProperties().getProperty("url1"),
            Application.settings.getProperties().getProperty("appid"),
            user_id));
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        if ("gzip".equals(conn.getContentEncoding())) {
            is = new GZIPInputStream(is);
        }
        String resultJson = IOUtils.toString(is, Charset.defaultCharset());
        JSONObject jsonObjects = new JSONObject(resultJson);
        ObjectMapper objectMapper = new ObjectMapper();
        JSONArray jsonArray = jsonObjects.getJSONArray("response");
        ListResponseObject lro;

        Boolean SubscribeTypeText_deleted = true;
        Boolean SubscribeTypeText_updated = true;
        Boolean SubscribeTypeText_updatedSize_incremented = false;
        Boolean SubscribeTypeAuthor_typed = true;
        
        if (filtering) {
            try { 
                Integer UID=Application.databaseManager.getUserIdBySiwatcherUserId(user_id);
                SubscribeTypeText_deleted = ((UID!=null) && Application.databaseManager.getSetting(UID, "subscribe.type.text_deleted").equals("true"));
                SubscribeTypeText_updated = ((UID!=null) && Application.databaseManager.getSetting(UID, "subscribe.type.text_updated").equals("true"));
                SubscribeTypeText_updatedSize_incremented = ((UID!=null) && Application.databaseManager.getSetting(UID, "subscribe.type.text_update.size_incremented").equals("true"));
                SubscribeTypeAuthor_typed = ((UID!=null) && Application.databaseManager.getSetting(UID, "subscribe.type.author_typed").equals("true"));
            } catch (Exception ex) {
                BotLogger.error(LOGTAG, ex);
            }
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            lro = objectMapper.readValue(jsonArray.get(i).toString(), ListResponseObject.class);
            if (filtering && (lro.getEvent() != null) && Application.databaseManager.printEvent(user_id, lro.getEvent())) {
                continue;
            }
            if (filtering && !SubscribeTypeText_deleted && (lro.getType()==null)) continue;
            if (filtering && !SubscribeTypeText_updated && (lro.getType()!=null) &&(lro.getType()==1)) continue;
            if (filtering && 
                    SubscribeTypeText_updated && 
                    SubscribeTypeText_updatedSize_incremented && 
                    (lro.getType()!=null) &&
                    (lro.getType()==1) && 
                    (lro.getSize()!=null) && 
                    (lro.getOldsize()!=null) && 
                    (lro.getSize()<=lro.getOldsize())) continue;
            if (filtering && !SubscribeTypeAuthor_typed && (lro.getType()!=null) && (lro.getType()==2)) continue;
            result.add(lro);
        }
        return result;
    }
    
    /**
     * Возвращает часовой пояс в указаной локации
     * @param location Широта и долгота
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
    public static GoogleTimezoneLocation getResponseLocation(Location location) throws IOException, InterruptedException {
        URL url = new URL(String.format(
            Application.settings.getProperties().getProperty("url2"),
            location.getLatitude().toString()+","+location.getLongitude().toString(),
            Application.settings.getProperties().getProperty("appid2")));
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        if ("gzip".equals(conn.getContentEncoding())) {
            is = new GZIPInputStream(is);
        }
        String resultJson = IOUtils.toString(is, Charset.defaultCharset());
        JSONObject jsonObject = new JSONObject(resultJson);
        ObjectMapper objectMapper = new ObjectMapper();
        GoogleTimezoneLocation result=objectMapper.readValue(jsonObject.toString(), GoogleTimezoneLocation.class);
        return result; 
     }   
     
    /**
     * Список книг за последний день
     * @return
     * @throws IOException
     * @throws InterruptedException 
     */
     public static Set<ListResponseObject> getResponseByDay() throws IOException, InterruptedException {
        Set<ListResponseObject> result = new HashSet();
        URL url = new URL(String.format(
            Application.settings.getProperties().getProperty("url3"),
            Application.settings.getProperties().getProperty("appid")));
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        if ("gzip".equals(conn.getContentEncoding())) {
            is = new GZIPInputStream(is);
        }
        String resultJson = IOUtils.toString(is, Charset.defaultCharset());
        JSONObject jsonObjects = new JSONObject(resultJson);
        ObjectMapper objectMapper = new ObjectMapper();
        JSONArray jsonArray = jsonObjects.getJSONArray("response");
        ListResponseObject lro;
        for (int i = 0; i < jsonArray.length(); i++) {
            lro = objectMapper.readValue(jsonArray.get(i).toString(), ListResponseObject.class);
            result.add(lro);
        }
        return result;
     }
     
     /**
      * Список всех последних изменений текстов с заданной даты
      * @param date
      * @return
      * @throws IOException
      * @throws InterruptedException 
      */
     public static Set<ListResponseObject> getResponseFromDay(Date date) throws IOException, InterruptedException {
        Set<ListResponseObject> result = new HashSet();
        Long unixtime = date.getTime()/1000L;
        URL url = new URL(String.format(
            Application.settings.getProperties().getProperty("url4"),
            unixtime.toString(),
            Application.settings.getProperties().getProperty("appid")
            ));
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        if ("gzip".equals(conn.getContentEncoding())) {
            is = new GZIPInputStream(is);
        }
        String resultJson = IOUtils.toString(is, Charset.defaultCharset());
        JSONObject jsonObjects = new JSONObject(resultJson);
        ObjectMapper objectMapper = new ObjectMapper();
        JSONArray jsonArray = jsonObjects.getJSONArray("response");
        ListResponseObject lro;
        for (int i = 0; i < jsonArray.length(); i++) {
            lro = objectMapper.readValue(jsonArray.get(i).toString(), ListResponseObject.class);
            result.add(lro);
        }
        return result;
     }    
}
