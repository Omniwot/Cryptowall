package com.e.aucrypto;

import android.graphics.Color;

import jnr.constants.platform.INAddr;

public class EtherAccount {
    private String name="";
    private String address="";
    private String fullpath="";


    // Constructor
    public EtherAccount(String name, String address,String fullpath) {
       this.name=name;
       this.address=address;
       this.fullpath=fullpath;
    }

    // Getter and Setter
    public String get_name() {
        return name ;
    }

    public void set_name(String course_name) {
        this.name = course_name;
    }

    public String get_address() {
        return address;
    }

    public void set_address(String course_address) {
        this.address = course_address;
    }

    public void set_fullpath(String path){this.fullpath= path;}

    public String get_fullpath(){return fullpath;}




}
