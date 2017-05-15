/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.od.psrv.tbsiwatcher.model;

/**
 *
 * @author Prk
 */
public class GoogleTimezoneLocation {
    private Long dstOffset;
    private Long rawOffset;
    private String status;
    private String timeZoneId;
    private String timeZoneName;

    public GoogleTimezoneLocation() {
    }

    public Long getDstOffset() {
        return dstOffset;
    }

    public void setDstOffset(Long dstOffset) {
        this.dstOffset = dstOffset;
    }

    public Long getRawOffset() {
        return rawOffset;
    }

    public void setRawOffset(Long rawOffset) {
        this.rawOffset = rawOffset;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public String getTimeZoneName() {
        return timeZoneName;
    }

    public void setTimeZoneName(String timeZoneName) {
        this.timeZoneName = timeZoneName;
    }
    
    
}
