package com.example.openbook.Data;

import java.io.Serializable;

public class AdminTableList implements Serializable {

    String adminTableNumber;
    String adminTableMenu;
    String adminTablePrice;
    String adminTableGender;
    String adminTableGuestNumber;

    String adminTableStatement;
    int adminTableIdentifier;
    int viewType; //0 -> 후불, 1-> 선불



    public AdminTableList(String adminTableNumber, String adminTableMenu, String adminTablePrice,
                          String adminTableGender, String adminTableGuestNumber, int viewType, int adminTableIdentifier){
        this.adminTableNumber = adminTableNumber;
        this.adminTableMenu = adminTableMenu;
        this.adminTablePrice = adminTablePrice;
        this.adminTableGender = adminTableGender;
        this.adminTableGuestNumber = adminTableGuestNumber;
        this.adminTableIdentifier = adminTableIdentifier;
        this.viewType = viewType;

    }


    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public int getAdminTableIdentifier() {
        return adminTableIdentifier;
    }

    public void setAdminTableIdentifier(int adminTableIdentifier) {
        this.adminTableIdentifier = adminTableIdentifier;
    }

    public String getAdminTableStatement() {
        return adminTableStatement;
    }

    public void setAdminTableStatement(String adminTableStatement) {
        this.adminTableStatement = adminTableStatement;
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
