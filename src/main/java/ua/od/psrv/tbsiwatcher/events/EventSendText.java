/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.od.psrv.tbsiwatcher.events;

/**
 *
 * @author Prk
 */
public interface EventSendText {
    public void onSendMessage(Long ChatId, String Message);
}
