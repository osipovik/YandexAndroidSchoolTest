package com.osipoff.testyandexmoney;

import java.io.Serializable;

/**
 * Created by OsIpOff on 16.08.2015.
 */
public class YaService implements Serializable {

    private int id;
    private String title;

    public YaService(int id, String title){
        this.id = id;
        this.title = title;
    }

    public String getTitle(){
        return this.title;
    }

    public int getId(){
        return this.id;
    }
}
