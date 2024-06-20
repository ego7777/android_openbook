package com.example.openbook.Data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.example.openbook.Category.PaymentCategory;

public class AdminTableList implements Parcelable {

    String adminTableNumber;
    String adminTableMenu;
    String adminTablePrice;
    String adminTableGender;
    String adminTableGuestNumber;

    String adminTableStatement;
    int adminTableIdentifier;
    int paymentType; //0 -> now, 1-> later



    public AdminTableList(String adminTableNumber, String adminTableMenu, String adminTablePrice,
                          String adminTableGender, String adminTableGuestNumber, int paymentType, int adminTableIdentifier){
        this.adminTableNumber = adminTableNumber;
        this.adminTableMenu = adminTableMenu;
        this.adminTablePrice = adminTablePrice;
        this.adminTableGender = adminTableGender;
        this.adminTableGuestNumber = adminTableGuestNumber;
        this.adminTableIdentifier = adminTableIdentifier;
        this.paymentType = paymentType;

    }


    protected AdminTableList(Parcel in) {
        adminTableNumber = in.readString();
        adminTableMenu = in.readString();
        adminTablePrice = in.readString();
        adminTableGender = in.readString();
        adminTableGuestNumber = in.readString();
        adminTableStatement = in.readString();
        adminTableIdentifier = in.readInt();
        paymentType = in.readInt();
    }

    public static final Creator<AdminTableList> CREATOR = new Creator<AdminTableList>() {
        @Override
        public AdminTableList createFromParcel(Parcel in) {
            return new AdminTableList(in);
        }

        @Override
        public AdminTableList[] newArray(int size) {
            return new AdminTableList[size];
        }
    };

    public int getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(int paymentType) {
        this.paymentType = paymentType;
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(adminTableNumber);
        parcel.writeString(adminTableMenu);
        parcel.writeString(adminTablePrice);
        parcel.writeString(adminTableGender);
        parcel.writeString(adminTableGuestNumber);
        parcel.writeString(adminTableStatement);
        parcel.writeInt(adminTableIdentifier);
        parcel.writeInt(paymentType);
    }

    public void init(){
        adminTableStatement = null;
        adminTableMenu = null;
        adminTablePrice = null;
        adminTableGender = null;
        adminTableGuestNumber = null;
        adminTableIdentifier = 0;
        paymentType = PaymentCategory.UNSELECTED.getValue();
    }
}
