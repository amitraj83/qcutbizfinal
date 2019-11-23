package com.qcut.biz.models;

public class Shop {

    String id = "";
    public String email = "";
    public String name = "";
    public String shopname = "";
    public String password = "";
    public String phone = "";
    public String address = "";
    public String sunOpen = "9:00 AM";
    public String monOpen = "9:00 AM";
    public String tueOpen = "9:00 AM";
    public String wedOpen = "9:00 AM";
    public String thuOpen = "9:00 AM";
    public String friOpen = "9:00 AM";
    public String satOpen = "9:00 AM";

    public String sunClose = "9:00 PM";
    public String monClose = "9:00 PM";
    public String tueClose = "9:00 PM";
    public String wedClose = "9:00 PM";
    public String thuClose = "9:00 PM";
    public String friClose = "9:00 PM";
    public String satClose = "9:00 PM";

    public Shop(String id, String email, String name, String shopname, String password) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.shopname = shopname;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShopname() {
        return shopname;
    }

    public void setShopname(String shopname) {
        this.shopname = shopname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSunOpen() {
        return sunOpen;
    }

    public void setSunOpen(String sunOpen) {
        this.sunOpen = sunOpen;
    }

    public String getMonOpen() {
        return monOpen;
    }

    public void setMonOpen(String monOpen) {
        this.monOpen = monOpen;
    }

    public String getTueOpen() {
        return tueOpen;
    }

    public void setTueOpen(String tueOpen) {
        this.tueOpen = tueOpen;
    }

    public String getWedOpen() {
        return wedOpen;
    }

    public void setWedOpen(String wedOpen) {
        this.wedOpen = wedOpen;
    }

    public String getThuOpen() {
        return thuOpen;
    }

    public void setThuOpen(String thuOpen) {
        this.thuOpen = thuOpen;
    }

    public String getFriOpen() {
        return friOpen;
    }

    public void setFriOpen(String friOpen) {
        this.friOpen = friOpen;
    }

    public String getSatOpen() {
        return satOpen;
    }

    public void setSatOpen(String satOpen) {
        this.satOpen = satOpen;
    }

    public String getSunClose() {
        return sunClose;
    }

    public void setSunClose(String sunClose) {
        this.sunClose = sunClose;
    }

    public String getMonClose() {
        return monClose;
    }

    public void setMonClose(String monClose) {
        this.monClose = monClose;
    }

    public String getTueClose() {
        return tueClose;
    }

    public void setTueClose(String tueClose) {
        this.tueClose = tueClose;
    }

    public String getWedClose() {
        return wedClose;
    }

    public void setWedClose(String wedClose) {
        this.wedClose = wedClose;
    }

    public String getThuClose() {
        return thuClose;
    }

    public void setThuClose(String thuClose) {
        this.thuClose = thuClose;
    }

    public String getFriClose() {
        return friClose;
    }

    public void setFriClose(String friClose) {
        this.friClose = friClose;
    }

    public String getSatClose() {
        return satClose;
    }

    public void setSatClose(String satClose) {
        this.satClose = satClose;
    }
}
