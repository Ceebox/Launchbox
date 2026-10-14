package com.chadderbox.launchbox.settings.options;

public class DialogOption<T> {

    private final T mItem;
    private final String mDisplayName;

    public DialogOption(T item, String displayName) {
        mItem = item;
        mDisplayName = displayName;
    }

    public T getItem() {
        return mItem;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

}
