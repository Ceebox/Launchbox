package com.chadderbox.launchbox.widgets.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface WidgetDao {
    @Query("SELECT * FROM widgets")
    List<WidgetItem> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(WidgetItem widget);

    @Update
    void update(WidgetItem widget);

    @Delete
    void delete(WidgetItem widget);

    @Query("DELETE FROM widgets")
    void wipe();
}
