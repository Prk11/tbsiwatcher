/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.od.psrv.tbsiwatcher;

import java.io.File;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

/**
 *
 * @author Prk
 */
public class Application {

    public static final String LOGTAG = "APPLICATIONHANDLER";
    public static Settings settings = null;
    public static DatabaseManager databaseManager;
    
    public static void main(String[] args) {
        String configFileName="";
        String dbPath = "";
        for (int i = 0; i < args.length; i++) {
            if ((args.length>i) && (args[i].toLowerCase().equals("-d") && ((new File(args[i+1])).exists()))) {                
                dbPath = args[i+1];                
            }
            else if ((args.length>i) && (args[i].toLowerCase().equals("-c") && ((new File(args[i+1])).exists()))) {
                configFileName = args[i+1];
            }
            
        }
        if (!"".equals(configFileName))
            settings = new Settings(configFileName);
        else
            settings = new Settings();
        if (!"".equals(dbPath))
            settings.getProperties().replace("database", dbPath);
        databaseManager = new DatabaseManager();
        
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new SiwatcherBot());
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }

}
