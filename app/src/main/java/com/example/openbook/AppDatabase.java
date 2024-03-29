package com.example.openbook;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {MenuData.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MenuDao menuDao();
}
