package com.autulin.updatehosts;


public class HostsBean {
    String title;
    String address;
    boolean isBacked;

    public HostsBean(){
    }

    public HostsBean(String title, String address, boolean isBacked) {
        this.title = title;
        this.address = address;
        this.isBacked = isBacked;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isBacked() {
        return isBacked;
    }

    public void setIsBacked(boolean isBacked) {
        this.isBacked = isBacked;
    }
}
