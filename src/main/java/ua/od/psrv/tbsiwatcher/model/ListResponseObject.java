/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.od.psrv.tbsiwatcher.model;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import ua.od.psrv.tbsiwatcher.Application;

/**
 *
 * @author Prk
 */
public class ListResponseObject {

    private Integer event;
    private Long time;
    private String url;
    private Integer type;
    private String rating;
    private String author;
    private String title;
    private String genre;
    private String description;
    private Integer size;
    private Integer oldsize;
    private String texts;

    
    private Boolean markup=true;
    
    public ListResponseObject() {
    }

    public Boolean getMarkup() {
        return markup;
    }

    public void setMarkup(Boolean markup) {
        this.markup = markup;
    }

    @Override
    public String toString() {
        return this.getMarkup()?this.toStringMarkup():this.toStringNoMarkup() ;
    }
       
    
    
    protected String toStringNoMarkup() {
        String result="";
        if (this.getTime() != null) result += "[" + this.getTimeFormatting() + "] ";
        if (this.getType()==null) {
            if (!"".equals(this.getDescription())) result +=  this.getDescription() + "\n\n";
        }
        else if(this.getType()==1) {
            if (this.getAuthor()!=null) result += "" + this.getAuthor() + "\n";
            if (this.getUrl()!=null && !"".equals(this.getUrl())) {
                result += "[" + this.getTitle() + "](http://samlib.ru/" + this.getUrl() + ") ";
                if (Boolean.parseBoolean(Application.settings.getProperties().getProperty("checkfb2"))) {
                    String fb2url = "http://samlib.ru/" + this.getUrl().replaceAll(".shtml", ".fb2.zip");                    
                    try {
                        URL urlfb2 = new URL(fb2url);
                        URLConnection connfb2 = urlfb2.openConnection();
                        connfb2.getContent();
                        result += "\n [FB2](" + fb2url + ") ";
                    } catch (IOException ex) {}                    
                }
                result += "\n";               
            }
            else if (this.getTitle()!=null) result += this.getTitle()+ "\n";
            if (!"".equals(this.getDescription())) result += "" + this.getDescription() + "\n";
            if (this.getSize() != null) {
                    if (this.getOldsize() != null) {
                        int diffsize = this.getSize() - this.getOldsize();
                        if (diffsize == 0) {
                            result += "Размер: " + Integer.toString(this.getSize()) + " КБ\n";
                        } else {
                            result += "Изменение размера: " + ((diffsize > 0) ? "+" : "") + Integer.toString(diffsize) + " КБ\n";
                        }
                    } else {
                        result += "Размер: " + Integer.toString(this.getSize()) + " КБ\n";
                    }
                }
                result += "\n";
        }
        else if(this.getType()==2) {          
            if (this.getAuthor()!=null) {
                if (this.getUrl()!=null && !"".equals(this.getUrl())) 
                    result += "[" + this.getAuthor() + "](" + "http://samlib.ru/" +this.getUrl() + ")\n";
                else
                    result += "" + this.getAuthor() + "\n";
            }
            if (this.getDescription()!=null && !"".equals(this.getDescription())) result += "_" + this.getDescription() + "_\n";
//            if (this.getTexts()!=null && !"".equals(this.getTexts())) result += "http://samlib.ru/"+this.getTexts().trim().replaceAll(" ", " \nhttp://samlib.ru/")+"\n";
            result += "\n";
        }                                                      
        return result;
    }
  
    protected String toStringMarkup() {
        String result="";
        if (this.getTime() != null) result += "`[" + this.getTimeFormatting() + "]` ";
        if (this.getType()==null) {
            if (!"".equals(this.getDescription())) result += "_" + this.getDescription() + "_\n\n";
        }
        else if(this.getType()==1) {
            if (this.getAuthor()!=null) result += "*" + this.getAuthor() + "*\n";
            if (this.getUrl()!=null && !"".equals(this.getUrl())) {
                result += "[" + this.getTitle() + "](http://samlib.ru/" + this.getUrl() + ") ";
                if (Boolean.parseBoolean(Application.settings.getProperties().getProperty("checkfb2"))) {
                    String fb2url = "http://samlib.ru/" + this.getUrl().replaceAll(".shtml", ".fb2.zip");                    
                    try {
                        URL urlfb2 = new URL(fb2url);
                        URLConnection connfb2 = urlfb2.openConnection();
                        connfb2.getContent();
                        result += "( [FB2](" + fb2url + ") ) ";
                    } catch (IOException ex) {}                    
                }
                result += "\n";               
            }
            else if (this.getTitle()!=null) result += this.getTitle()+ "\n";
            if (!"".equals(this.getDescription())) result += "_" + this.getDescription() + "_\n";
            if (this.getSize() != null) {
                    if (this.getOldsize() != null) {
                        int diffsize = this.getSize() - this.getOldsize();
                        if (diffsize == 0) {
                            result += "Размер: " + Integer.toString(this.getSize()) + " КБ\n";
                        } else {
                            result += "Изменение размера: " + ((diffsize > 0) ? "+" : "") + Integer.toString(diffsize) + " КБ\n";
                        }
                    } else {
                        result += "Размер: " + Integer.toString(this.getSize()) + " КБ\n";
                    }
                }
                result += "\n";
        }
        else if(this.getType()==2) {          
            if (this.getAuthor()!=null) {
                if (this.getUrl()!=null && !"".equals(this.getUrl())) 
                    result += "[" + this.getAuthor() + "](" + "http://samlib.ru/" +this.getUrl() + ")\n";
                else
                    result += "*" + this.getAuthor() + "*\n";
            }
            if (this.getDescription()!=null && !"".equals(this.getDescription())) result += "_" + this.getDescription() + "_\n";
//            if (this.getTexts()!=null && !"".equals(this.getTexts())) result += "`http://samlib.ru/"+this.getTexts().trim().replaceAll(" ", " \nhttp://samlib.ru/")+"`\n";
            result += "\n";
        }                                                      
        return result;
    }
    
    public Integer getEvent() {
        return event;
    }

    public void setEvent(Integer event) {
        this.event = event;
    }

    public Long getTime() {
        return time;
    }

    public String getTimeFormatting() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm");
        return sdf.format(new Date(time * 1000L));
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getOldsize() {
        return oldsize;
    }

    public void setOldsize(Integer oldsize) {
        this.oldsize = oldsize;
    }

    public String getTexts() {
        return texts;
    }

    public void setTexts(String texts) {
        this.texts = texts;
    }

}
