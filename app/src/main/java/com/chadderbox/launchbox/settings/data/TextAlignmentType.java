package com.chadderbox.launchbox.settings.data;

public enum TextAlignmentType {
    LEFT("Left"),
    CENTER("Center");

    private final String mValue;

    private TextAlignmentType(String value) {
        mValue = value;
    }

    public String getValue() {
        return mValue;
    }

//    public static TextAlignmentType valueOf(String label) {
//        for (var type : values()) {
//            if (java.util.Objects.equals(type.getValue(), label)) {
//                return type;
//            }
//        }
//
//        // Default
//        return LEFT;
//    }
}
