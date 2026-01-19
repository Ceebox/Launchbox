package com.chadderbox.launchbox.widgets.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {WidgetItem.class}, version = 1, exportSchema = false)
public abstract class WidgetDatabase extends RoomDatabase {
    private static volatile WidgetDatabase INSTANCE;

    public abstract WidgetDao widgetDao();

    public static WidgetDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (WidgetDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        WidgetDatabase.class,
                        "launchbox_widgets"
                    ).build();
                }
            }
        }

        return INSTANCE;
    }
}
