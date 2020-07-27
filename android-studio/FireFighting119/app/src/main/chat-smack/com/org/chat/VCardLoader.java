package com.org.chat;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.collection.LruCache;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lcjian.lib.util.common.BitmapUtils;
import com.lcjian.lib.util.common.FileUtils;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.EntityBareJid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class VCardLoader {

    private static VCardLoader INSTANCE;

    private final SmackClient mSmackClient;
    private final VCardCache mVCardCache;
    private final VCardCache mTempVCardCache;
    private final ExecutorService mExecutorService;
    private final Handler mHandler;

    public static synchronized VCardLoader getInstance(SmackClient smackClient) {
        if (INSTANCE == null) {
            INSTANCE = new VCardLoader(smackClient);
        }
        return INSTANCE;
    }

    private VCardLoader(SmackClient smackClient) {
        this.mSmackClient = smackClient;
        this.mVCardCache = new WrapCache(Constants.DIRECTORY_V_CARD.getPath());
        this.mTempVCardCache = new WrapCache(new File(Constants.DIRECTORY_V_CARD, "temp").getPath());
        this.mHandler = new Handler(Looper.getMainLooper());
        ThreadPoolExecutor temp = new ThreadPoolExecutor(1, 1,
                30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        temp.allowCoreThreadTimeOut(true);
        this.mExecutorService = temp;
    }

    public synchronized void destroy() {
        mExecutorService.shutdown();
        INSTANCE = null;
    }

    public void putTempVCard(EntityBareJid entityBareJid, VCard vCard) {
        mTempVCardCache.put(entityBareJid, vCard);
    }

    public VCard getVCard(EntityBareJid entityBareJid) {
        VCard vCard = mVCardCache.get(entityBareJid);
        if (vCard == null) {
            vCard = loadVCard(entityBareJid);
        }
        if (vCard == null) {
            vCard = mTempVCardCache.get(entityBareJid);
        } else {
            VCard tempVCard = mTempVCardCache.get(entityBareJid);
            if (TextUtils.isEmpty(vCard.getNickName()) && tempVCard != null) {
                vCard.setNickName(tempVCard.getNickName());
            }
        }
        return vCard;
    }

    public void displayAvatar(EntityBareJid entityBareJid, ImageView imageView, Bitmap placeholder) {
        //需要开启新线程，用url唯一标识
        imageView.setTag(entityBareJid.toString());

        mExecutorService.submit(() -> {
            VCard vCard = getVCard(entityBareJid);

            String filename = vCard == null ? null : vCard.getField("avatar_local_path");
            Bitmap bitmap = TextUtils.isEmpty(filename)
                    ? placeholder
                    : BitmapUtils.decodeSampledBitmapFromFile(filename, imageView.getWidth(), imageView.getHeight());
            mHandler.post(() -> {
                if (TextUtils.equals(imageView.getTag().toString(), entityBareJid.toString())) {
                    imageView.setImageBitmap(bitmap);
                }
            });
        });
    }

    public void displayNickName(EntityBareJid entityBareJid, TextView textView) {
        //需要开启新线程，用url唯一标识
        textView.setTag(entityBareJid.toString());

        mExecutorService.submit(() -> {
            VCard vCard = getVCard(entityBareJid);

            String nickname = vCard == null ? null : vCard.getNickName();
            mHandler.post(() -> {
                if (TextUtils.equals(textView.getTag().toString(), entityBareJid.toString())) {
                    textView.setText(nickname);
                }
            });
        });
    }

    private VCard loadVCard(EntityBareJid entityBareJid) {
        VCard vCard = mSmackClient.getVCard(entityBareJid);
        if (vCard == null) {
            return null;
        }
        byte[] avatar = vCard.getAvatar();
        if (avatar != null && avatar.length != 0) {
            String path = Constants.DIRECTORY_AVATAR.getPath();
            String name = entityBareJid.getLocalpart() + ".jpg";
            FileUtils.writeBytesToFile(path, name, avatar);
            vCard.setAvatar((byte[]) null);
            vCard.setField("avatar_local_path", path + "/" + name);
        }
        mVCardCache.put(entityBareJid, vCard);
        return vCard;
    }

    private interface VCardCache {
        void put(EntityBareJid entityBareJid, VCard vCard);

        VCard get(EntityBareJid entityBareJid);
    }

    private static class MemoryCache implements VCardCache {

        private LruCache<EntityBareJid, VCard> mCache;

        MemoryCache() {
            mCache = new LruCache<>(100);
        }

        @Override
        public void put(EntityBareJid entityBareJid, VCard vCard) {
            mCache.put(entityBareJid, vCard);
        }

        @Override
        public VCard get(EntityBareJid entityBareJid) {
            return mCache.get(entityBareJid);
        }
    }

    private static class DiskCache implements VCardCache {

        private String mPath;

        public DiskCache(String path) {
            this.mPath = path;
        }

        @Override
        public void put(EntityBareJid entityBareJid, VCard vCard) {
            Map<String, String> map = new HashMap<>();
            map.put("avatar", vCard.getField("avatar_local_path"));
            map.put("nick_name", vCard.getNickName());
            String name = entityBareJid.getLocalpart() + ".vCard";
            FileUtils.writeBytesToFile(mPath, name, new Gson().toJson(map).getBytes());
        }

        @Override
        public VCard get(EntityBareJid entityBareJid) {
            String name = entityBareJid.getLocalpart() + ".vCard";
            Reader reader = null;
            VCard vCard = null;
            try {
                reader = new FileReader(new File(mPath, name));
                Map<String, String> map = new Gson().fromJson(reader,
                        new TypeToken<Map<String, String>>() {
                        }.getType());
                if (map != null) {
                    vCard = new VCard();
                    vCard.setNickName(map.get("nick_name"));
                    vCard.setField("avatar_local_path", map.get("avatar_local_path"));
                }
            } catch (FileNotFoundException e) {
                Timber.w(e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Timber.w(e);
                    }
                }
            }
            return vCard;
        }
    }

    private static class WrapCache implements VCardCache {

        private MemoryCache mMemoryCache;
        private DiskCache mDiskCache;

        public WrapCache(String path) {
            this.mMemoryCache = new MemoryCache();
            this.mDiskCache = new DiskCache(path);
        }

        @Override
        public void put(EntityBareJid entityBareJid, VCard vCard) {
            mMemoryCache.put(entityBareJid, vCard);
            mDiskCache.put(entityBareJid, vCard);
        }

        @Override
        public VCard get(EntityBareJid entityBareJid) {
            VCard vCard = mMemoryCache.get(entityBareJid);
            if (vCard == null) {
                vCard = mDiskCache.get(entityBareJid);
            }
            return vCard;
        }
    }
}
