package com.example.openbook.Data;

import java.io.Serializable;

public class AdminTableList implements Serializable {

    String adminTableNumber;
    String adminTableMenu;
    String adminTablePrice;
    String adminTableGender;
    String adminTableGuestNumber;

    public AdminTableList(String adminTableNumber, String adminTableMenu, String adminTablePrice, String adminTableGender, String adminTableGuestNumber){
        this.adminTableNumber = adminTableNumber;
        this.adminTableMenu = adminTableMenu;
        this.adminTablePrice = adminTablePrice;
        this.adminTableGender = adminTableGender;
        this.adminTableGuestNumber = adminTableGuestNumber;
    }


    public String getAdminTableNumber() {
        return adminTableNumber;
    }

    public void setAdminTableNumber(String adminTableNumber) {
        this.adminTableNumber = adminTableNumber;
    }

    public String getAdminTableMenu() {
        return adminTableMenu;
    }

    public void setAdminTableMenu(String adminTableMenu) {
        this.adminTableMenu = adminTableMenu;
    }

    public String getAdminTablePrice() {
        return adminTablePrice;
    }

    public void setAdminTablePrice(String adminTablePrice) {
        this.adminTablePrice = adminTablePrice;
    }

    public String getAdminTableGender() {
        return adminTableGender;
    }

    public void setAdminTableGender(String adminTableGender) {
        this.adminTableGender = adminTableGender;
    }

    public String getAdminTableGuestNumber() {
        return adminTableGuestNumber;
    }

    public void setAdminTableGuestNumber(String adminTableGuestNumber) {
        this.adminTableGuestNumber = adminTableGuestNumber;
    }


}
