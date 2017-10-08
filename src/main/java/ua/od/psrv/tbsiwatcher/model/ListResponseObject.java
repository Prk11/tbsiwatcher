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
public class ListResponseObject implements Comparable<ListResponseObject> {

    public final static int SITE_SAMLIB=1;
    public final static int SITE_SIWATCHER_COLLECTION=2;
    public final static int SITE_AUTHOR_TODAY=3;
    
     //константы типов оповещений
    /** 
     * изменился размер книги
     */
    public final static int UTYPE_EVENT_CHANGE_SIZE=1; 
    
    /**
     * изменилась аннотация
     */
    public final static int UTYPE_EVENT_CHANGE_DESC=2;
    
    /**
     * изменился жанр
     */
    public final static int UTYPE_EVENT_CHANGE_GENRE=3;
    
    /**
     * изменилось название текста
     */
    public final static int UTYPE_EVENT_CHANGE_TITLE=4;
    
    /**
     * изменилось имя автора
     */
    public final static int UTYPE_EVENT_CHANGE_AUTHOR=5;
    
    /**
     * добавлен новый текст (общий признак, если не известен более точный)
     */
    public final static int UTYPE_EVENT_NEW_TEXT=6;
    
    /**
     * удалён текст (общий признак, если не известен более точный)
     */
    public final static int UTYPE_EVENT_DEL_TEXT=7;
    
    /**
     * создание книги
     */
    public final static int UTYPE_EVENT_CREATE_TEXT=20;
    
    /**
     * первая публикация
     */
    public final static int UTYPE_EVENT_FIRST_PUB_TEXT=21;
    
    /**
     * автор убрал книгу в черновик
     */
    public final static int UTYPE_EVENT_A_HIDE_TEXT=22;
    
    /**
     * автор опубликовал книгу из черновика
     */
    public final static int UTYPE_EVENT_A_UNHIDE_TEXT=23;
    
    /**
     * автор удалил книгу
     */
    public final static int UTYPE_EVENT_A_DEL_TEXT=24;
    
    /**
     * книга помечена как "завершенная"
     */
    public final static int UTYPE_EVENT_TEXT_FINISHED=25;
    
    /**
     * книга помечена как "в процессе"
     */
    public final static int UTYPE_EVENT_TEXT_PROGRESS=26;

    /**
     * начала действовать скидка
     */
    public final static int UTYPE_EVENT_DISCOUNT_START=30;

    /**
     * закончилось действие скидки
     */
    public final static int UTYPE_EVENT_DISCOUNT_END=31;

    /**
     * размер скидки изменен
     */
    public final static int UTYPE_EVENT_DISCOUNT_CHANGE=32;

    /**
     * изменена цена книги
     */
    public final static int UTYPE_EVENT_PRICE_TEXT_UPD=35;

    /**
     * изменена цена главы книги
     */
    public final static int UTYPE_EVENT_PRICE_CHAPTER_UPD=36;

    /**
     * рекомендация администрации ресурса
     */
    public final static int UTYPE_EVENT_TEXT_RECOMMEND=37;

    /**
     * добавлена новая часть
     */
    public final static int UTYPE_EVENT_NEW_CHAPTER_TEXT=40;

    /**
     * часть была отредактирована (изменение текста)
     */
    public final static int UTYPE_EVENT_UPD_CHAPTER_TEXT=41;

    /**
     * глава была убрана в черновик
     */
    public final static int UTYPE_EVENT_HIDE_CHAPTER_TEXT=42;

    /**
     * глава была опубликована из черновика
     */
    public final static int UTYPE_EVENT_UNHIDE_CHAPTER_TEXT=43;

    /**
     * глава была удалена
     */
    public final static int UTYPE_EVENT_DEL_CHAPTER_TEXT=44;

    /**
     * глава была отредактирована
     */
    public final static int UTYPE_EVENT_RENAME_CHAPTER_TEXT=45;

    /**
     * модератор убрал книгу из общего доступа
     */
    public final static int UTYPE_EVENT_M_HIDE_TEXT=101;

    /**
     * модератор удалил книгу
     */
    public final static int UTYPE_EVENT_M_DEL_TEXT=102;

    /**
     * модератор вернул книгу в общий доступ 
     */
    public final static int UTYPE_EVENT_M_UNHIDE_TEXT=103;
        
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
    private Integer site;
    private Integer utype;

    
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
            if ((this.getRating()!=null) && (!"".equals(this.getRating()))) {
                String _$=this.getRating().replace(',', '.').trim().replaceFirst("\\*.*", "");
                Float rating=Float.parseFloat(_$);
                String stars="";
                for (int i = 5; i < Math.round(rating); i++) {
                    stars+="☀️";
                }
                result += "*Оценка: *_(" + this.getRating()+ ")_ " + stars + "\n";
            }
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
            if ((this.getRating()!=null) && (!"".equals(this.getRating()))) {
                String _$=this.getRating().replace(',', '.').trim().replaceFirst("\\*.*", "");
                Float rating=Float.parseFloat(_$);
                String stars="";
                for (int i = 5; i < Math.round(rating); i++) {
                    stars+="☀️";
                }
                result += "*Оценка: *_(" + this.getRating()+ ")_ " + stars + "\n";
            }
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
        return sdf.format(new Date(getTime() * 1000L));
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

    public Integer getSite() {
        return site;
    }

    public void setSite(Integer site) {
        this.site = site;
    }

    public Integer getUtype() {
        return utype;
    }

    public void setUtype(Integer utype) {
        this.utype = utype;
    }

    
    
    @Override
    public int compareTo(ListResponseObject o) {
        return (int) (this.getTime()-o.getTime());
    }

}
