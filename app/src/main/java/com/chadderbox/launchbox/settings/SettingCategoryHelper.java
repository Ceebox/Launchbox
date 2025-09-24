package com.chadderbox.launchbox.settings;

import com.chadderbox.launchbox.settings.options.ISettingOption;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class SettingCategoryHelper {

    private SettingCategoryHelper() { }

    public static Map<SettingGroup, List<ISettingOption>> groupByCategory(List<ISettingOption> options) {
        Map<SettingGroup, List<ISettingOption>> map = new LinkedHashMap<>();
        for (ISettingOption option : options) {
            var ann = option.getClass().getAnnotation(SettingCategory.class);
            var category = (ann != null) ? ann.category() : SettingGroup.NONE;

            map.computeIfAbsent(category, k -> new ArrayList<>()).add(option);
        }

        return map;
    }
}
