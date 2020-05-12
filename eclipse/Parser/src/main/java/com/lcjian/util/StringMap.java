package com.lcjian.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class StringMap implements Map<String, String> {

    private HashMap<String, String> mMaps;

    private File mFile;

    public StringMap(File file) {
        mFile = file;
        Reader reader = null;
        try {
            reader = new FileReader(mFile);
            mMaps = new Gson().fromJson(reader,
                    new TypeToken<HashMap<String, String>>() {}.getType());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (mMaps == null) {
            mMaps = new HashMap<String, String>();
        }
    }

    @Override
    public int size() {
        return mMaps.size();
    }

    @Override
    public boolean isEmpty() {
        return mMaps.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return mMaps.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return mMaps.containsKey(value);
    }

    @Override
    public String get(Object key) {
        return mMaps.get(key);
    }

    @Override
    public String put(String key, String value) {
        String str = mMaps.put(key, value);
        persistent();
        return str;
    }

    @Override
    public String remove(Object key) {
        String str = mMaps.remove(key);
        persistent();
        return str;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        mMaps.putAll(m);
        persistent();
    }

    @Override
    public void clear() {
        mMaps.clear();
        persistent();
    }

    @Override
    public Set<String> keySet() {
        return mMaps.keySet();
    }

    @Override
    public Collection<String> values() {
        return mMaps.values();
    }

    @Override
    public Set<java.util.Map.Entry<String, String>> entrySet() {
        return mMaps.entrySet();
    }

    private void persistent() {
        FileWriter writer = null;
        try {
            writer = new FileWriter(mFile);
            new Gson().toJson(mMaps, writer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
