/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.od.psrv.tbsiwatcher.events;

import ua.od.psrv.tbsiwatcher.model.ListResponseObject;

/**
 *
 * @author Prk
 */
public interface EventSendResponseObject {
    public void onSendMessage(Long ChatId, ListResponseObject Message);
}
