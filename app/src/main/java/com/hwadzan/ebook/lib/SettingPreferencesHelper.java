package com.hwadzan.ebook.lib;

import android.content.Context;

import com.hwadzan.ebook.model.Setting;

public class SettingPreferencesHelper {
    SharedPreferencesHelper helper;
    public SettingPreferencesHelper(Context context) {
        helper = new SharedPreferencesHelper(context, "setting");
    }

    public void save(Setting setting) {
        helper.put("setting", setting);
    }

    public Setting getSetting() {
        Setting setting = helper.getSharedPreference("setting", Setting.class);
        if(setting==null){
            setting = new Setting();
            save(setting);
        }

        return setting;

    }
}
