/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ua.od.psrv.tbsiwatcher;

/**
 * Обслуживание по расписанию
 * @author Prk
 */
public class Sheduller implements Runnable {

    private Integer timeout;
    private Boolean terminate = false;

    public static final String LOGTAG = "SHEDULLERHANDLER";

    public Sheduller(Integer timeout) {
        this.timeout = timeout;
    }

    @Override
    public void run() {
        Long starttime = System.currentTimeMillis();
        while (!this.terminate) {
            if ((System.currentTimeMillis()-starttime)>(this.timeout * 60000)) {
                Application.databaseManager.DispatchMessageBySubscribe();
                starttime = System.currentTimeMillis();
            }
        }       
    }

    public void Terminate() {
        this.terminate = true;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    
}
