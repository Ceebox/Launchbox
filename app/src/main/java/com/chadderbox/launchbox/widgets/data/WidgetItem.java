package com.chadderbox.launchbox.widgets.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "widgets")
public class WidgetItem {
    @PrimaryKey
    public int appWidgetId;
    public int orderId;
    public int widthDp;
    public int heightDp;

    public WidgetItem(int appWidgetId, int orderId, int widthDp, int heightDp) {
        this.appWidgetId = appWidgetId;
        this.orderId = orderId;
        this.widthDp = widthDp;
        this.heightDp = heightDp;
    }
}
