package com.example.openbook.Data;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class AdminData implements Parcelable {
    String id;
    ArrayList<AdminTableList> adminTableLists;
    boolean isFcmExist;

    public AdminData(String id, ArrayList<AdminTableList> adminTableLists, boolean isFcmExist){
        this.id = id;
        this.adminTableLists = adminTableLists;
        this.isFcmExist = isFcmExist;
    }


    protected AdminData(Parcel in) {
        id = in.readString();
        adminTableLists = in.createTypedArrayList(AdminTableList.CREATOR);
        isFcmExist = in.readByte() != 0;
    }

    public static final Creator<AdminData> CREATOR = new Creator<AdminData>() {
        @Override
        public AdminData createFromParcel(Parcel in) {
            return new AdminData(in);
        }

        @Override
        public AdminData[] newArray(int size) {
            return new AdminData[size];
        }
    };

    public String getId() {
        return id;
    }

    public ArrayList<AdminTableList> getAdminTableLists() {
        return adminTableLists;
    }

    public boolean isFcmExist() {
        return isFcmExist;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAdminTableLists(ArrayList<AdminTableList> adminTableLists) {
        this.adminTableLists = adminTableLists;
    }

    public void setFcmExist(boolean fcmExist) {
        isFcmExist = fcmExist;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeTypedList(adminTableLists);
        parcel.writeByte((byte) (isFcmExist ? 1 : 0));
    }
}
