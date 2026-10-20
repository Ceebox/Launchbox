package com.chadderbox.launchbox.data;

public enum ItemViewType {
    HEADER(0),
    APP(1),
    WEB(2),
    SUGGESTION(3),
    SETTING(4),
    WIDGET(5);

    private final int mId;

    ItemViewType(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public static ItemViewType fromId(int id) {
        for (var type : values()) {
            if (type.mId == id) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown view type: " + id);
    }
}
