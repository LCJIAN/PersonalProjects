package com.lcjian.vastplayer.ui.mine;

import android.os.Bundle;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.lcjian.lib.download.Utils;
import com.lcjian.lib.util.Environment;
import com.lcjian.vastplayer.R;
import com.lcjian.vastplayer.android.preference.FilePickerPreference;

import java.io.File;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    private ListPreference subtitle_font_size_pref;
    private ListPreference memory_card_pref;
    private FilePickerPreference download_directory_pref;

    private String[] mExternalStorageList;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.pref_vast_player);
        subtitle_font_size_pref = findPreference("subtitle_font_size");
        memory_card_pref = findPreference("memory_card");
        download_directory_pref = findPreference("download_directory");

        subtitle_font_size_pref.setOnPreferenceChangeListener(this);
        memory_card_pref.setOnPreferenceChangeListener(this);
        download_directory_pref.setOnPreferenceChangeListener(this);

        File[] externalStorageFiles = Environment.getExternalStorageList(getContext());
        if (externalStorageFiles != null && externalStorageFiles.length != 0) {
            mExternalStorageList = new String[externalStorageFiles.length];
            int i = 0;
            for (File file : externalStorageFiles) {
                mExternalStorageList[i++] = file.getAbsolutePath();
            }
        }
        memory_card_pref.setEntries(mExternalStorageList);
        memory_card_pref.setEntryValues(mExternalStorageList);
        if (TextUtils.isEmpty(memory_card_pref.getValue())) {
            memory_card_pref.setValue(mExternalStorageList[0]);
        }

        setSummarySubtitleFontSize(subtitle_font_size_pref.getValue());
        setSummaryMemoryCard(memory_card_pref.getValue());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        String value = String.valueOf(newValue);
        if (TextUtils.equals(key, "subtitle_font_size")) {
            setSummarySubtitleFontSize(value);
        }
        if (TextUtils.equals(key, "memory_card")) {
            setSummaryMemoryCard(value);
        }
        if (TextUtils.equals(key, "download_directory")) {
            setSummaryDownloadDirectoryPref(value);
        }
        return true;
    }

    private void setSummarySubtitleFontSize(String value) {
        String[] strings = getResources().getStringArray(R.array.array_subtitle_font_size_des);
        String summary = "";
        switch (value) {
            case "12":
                summary = strings[0];
                break;
            case "14":
                summary = strings[1];
                break;
            case "16":
                summary = strings[2];
                break;
            case "18":
                summary = strings[3];
                break;
            case "22":
                summary = strings[4];
                break;
            default:
                break;
        }
        subtitle_font_size_pref.setSummary(summary);
    }

    private void setSummaryMemoryCard(String value) {
        StatFs statFs = new StatFs(value);
        String summary = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            summary = value + " (" + Utils.formatBytes(statFs.getAvailableBytes(), 2)
                    + "/" + Utils.formatBytes(statFs.getTotalBytes(), 2) + ")";
        }
        memory_card_pref.setSummary(summary);
        download_directory_pref.getProperties().root = new File(value);

        String downloadDirectory = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("download_directory", "");
        if (TextUtils.isEmpty(downloadDirectory) || !downloadDirectory.startsWith(value)) {
            downloadDirectory = new File(value, "Download").getAbsolutePath();
            download_directory_pref.setDefaultValue(downloadDirectory);
            PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putString("download_directory", downloadDirectory).apply();
        }
        setSummaryDownloadDirectoryPref(downloadDirectory);
    }

    private void setSummaryDownloadDirectoryPref(String value) {
        download_directory_pref.setSummary(value.split(":")[0]);
    }
}
