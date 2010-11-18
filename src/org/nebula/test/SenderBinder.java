package org.nebula.test;

import android.os.Binder;
import android.util.Log;

public class SenderBinder extends Binder {
    private ServiceSender service = null; 
    
	public SenderBinder(ServiceSender service) { 
        super(); 
        Log.v("nebula", "Binder: Set service");
        this.service = service;
    } 
 
    public ServiceSender getService(){ 
        Log.v("nebula", "Binder: Get service");
        return service; 
    }
}
