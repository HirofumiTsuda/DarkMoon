package com.dacho.darkmoon.layoutFragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.dacho.darkmoon.R;
import com.dacho.darkmoon.dialogs.OauthDialogFragment;

import java.util.Iterator;

/**
 * Created by admin on 2015/07/17.
 */
public class Preference extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    ListPreference left,center,right,left_long,center_long,right_long;
    int left_num,center_num,right_num,left_long_num,center_long_num,right_long_num;
    SharedPreferences sharedPreferences;
    PreferenceScreen confirm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().remove("left_pref").commit();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().remove("center_pref").commit();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().remove("right_pref").commit();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().remove("left_long_pref").commit();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().remove("center_long_pref").commit();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().remove("right_long_pref").commit();

        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        left = (ListPreference)getPreferenceScreen().findPreference("left_pref");
        center = (ListPreference)getPreferenceScreen().findPreference("center_pref");
        right = (ListPreference)getPreferenceScreen().findPreference("right_pref");
        left_long = (ListPreference)getPreferenceScreen().findPreference("left_long_pref");
        center_long = (ListPreference)getPreferenceScreen().findPreference("center_long_pref");
        right_long = (ListPreference)getPreferenceScreen().findPreference("right_long_pref");
        confirm = (PreferenceScreen)getPreferenceScreen().findPreference("confirm");
        setDefault();
        confirm.setOnPreferenceClickListener(new android.preference.Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                OauthDialogFragment oauthDialogFragment = new OauthDialogFragment();
                oauthDialogFragment.setActivity(getActivity());
                oauthDialogFragment.show(getActivity().getFragmentManager(), "Oauth");
                return false;
            }
        });




}

    void setDefault(){

        left_num = sharedPreferences.getInt("left", 0);
        left.setValueIndex(left_num);
        left.setSummary(left.getEntries()[left_num]);
        center_num = sharedPreferences.getInt("center", 1);
        center.setValueIndex(center_num);
        center.setSummary(center.getEntries()[center_num]);
        right_num = sharedPreferences.getInt("right", 2);
        right.setValueIndex(right_num);
        right.setSummary(right.getEntries()[right_num]);

        left_long_num = sharedPreferences.getInt("left_long", 3);
        left_long.setValueIndex(left_long_num);
        left_long.setSummary(left_long.getEntries()[left_long_num]);
        center_long_num = sharedPreferences.getInt("center_long", 4);
        center_long.setValueIndex(center_long_num);
        center_long.setSummary(center_long.getEntries()[center_long_num]);
        right_long_num = sharedPreferences.getInt("right_long", 2);
        right_long.setValueIndex(right_long_num);
        right_long.setSummary(right_long.getEntries()[right_long_num]);

    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d("pref", "call on resume");
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("pref", "call on pause");
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }



    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("pref","on shared preferences changed");
        ListPreference listPreference = (ListPreference)findPreference(key);
        Log.d("pref", "key:" + key + ",value:" + listPreference.getValue());
        int id = stringToId(listPreference.getValue());
        listPreference.setSummary(listPreference.getEntries()[id]);
    }

    @Override
    public void onDestroy() {
        Log.d("pref", "call on destoroy");
        sharedPreferences.edit().putInt("left", stringToId(left.getValue())).commit();
        sharedPreferences.edit().putInt("center", stringToId(center.getValue())).commit();
        sharedPreferences.edit().putInt("right", stringToId(right.getValue())).commit();
        sharedPreferences.edit().putInt("left_long", stringToId(left_long.getValue())).commit();
        sharedPreferences.edit().putInt("center_long", stringToId(center_long.getValue())).commit();
        sharedPreferences.edit().putInt("right_long", stringToId(right_long.getValue())).commit();
        super.onDestroy();
    }

    int stringToId(String str){
        switch (str){
            case "fav":
                return 0;
            case "menu":
                return 1;
            case "reply":
                return 2;
            case "rt":
                return 3;
            case "steal":
                return 4;
            case "uort":
                return 5;
        }
        return -1;
    }
}
