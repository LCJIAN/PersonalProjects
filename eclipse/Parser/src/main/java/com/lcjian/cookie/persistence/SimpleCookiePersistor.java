/*
 * Copyright (C) 2016 Francisco José Montiel Navarro.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lcjian.cookie.persistence;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;

import com.lcjian.util.StringMap;

public class SimpleCookiePersistor implements CookiePersistor {
    
    private StringMap map;

    public SimpleCookiePersistor(File file) {
        map = new StringMap(file);
    }

    @Override
    public List<Cookie> loadAll() {
        List<Cookie> cookies = new ArrayList<Cookie>();

        for (Map.Entry<String, ?> entry : map.entrySet()) {
            String serializedCookie = (String) entry.getValue();
            Cookie cookie = new SerializableCookie().decode(serializedCookie);
            cookies.add(cookie);
        }
        return cookies;
    }

    @Override
    public void saveAll(Collection<Cookie> cookies) {
        HashMap<String, String> temp = new HashMap<String, String>();
        for (Cookie cookie : cookies) {
            if (cookie.persistent()) {
                temp.put(createCookieKey(cookie), new SerializableCookie().encode(cookie));
            }
        }
        map.putAll(temp);
    }

    @Override
    public void removeAll(Collection<Cookie> cookies) {
        for (Cookie cookie : cookies) {
            map.remove(createCookieKey(cookie));
        }
    }

    private static String createCookieKey(Cookie cookie) {
        return (cookie.secure() ? "https" : "http") + "://" + cookie.domain() + cookie.path() + "|" + cookie.name();
    }

    @Override
    public void clear() {
        map.clear();
    }
}
