package com.example.openbook;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MenuDao {
    @Insert
    void insertMenuItem(MenuData ... menuData);

    @Query("SELECT * FROM menudata")
    List<MenuData> getAll();


}
