/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.od.psrv.tbsiwatcher;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import org.telegram.telegrambots.logging.BotLogger;

/**
 *
 * @author Prk
 */
public class Settings {
    
    private static final String LOGTAG = "SETTINGS";
    private Properties properties;
    
    public static Settings Instance;
    
    public Settings() {
        Instance = this;
         try {
            properties = new Properties();
            properties.load(ClassLoader.getSystemResourceAsStream("settings.properties"));            
        } catch (IOException ex) {
            BotLogger.error(LOGTAG, ex);
        } 
    }
    
    public Settings(String path) {
        Instance = this;
         try {
            properties = new Properties();
            properties.load(new FileInputStream(path));            
        } catch (IOException ex) {
            BotLogger.error(LOGTAG, ex);
        } 
    }

    public Properties getProperties() {
        return properties;
    }
    
    
}
