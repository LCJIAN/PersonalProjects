package com.example.newbiechen.ireader.model.local;

import com.example.newbiechen.ireader.model.bean.DownloadTaskBean;
import com.example.newbiechen.ireader.model.gen.DaoSession;

import java.util.List;

/**
 * Created by newbiechen on 17-4-26.
 */

public class LocalRepository implements SaveDbHelper, GetDbHelper, DeleteDbHelper {

    private static volatile LocalRepository sInstance;
    private DaoSession mSession;

    private LocalRepository() {
        mSession = DaoDbHelper.getInstance().getSession();
    }

    public static LocalRepository getInstance() {
        if (sInstance == null) {
            synchronized (LocalRepository.class) {
                if (sInstance == null) {
                    sInstance = new LocalRepository();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void saveDownloadTask(DownloadTaskBean bean) {
        BookRepository.getInstance()
                .saveBookChaptersWithAsync(bean.getBookChapters());
        mSession.getDownloadTaskBeanDao()
                .insertOrReplace(bean);
    }

    @Override
    public List<DownloadTaskBean> getDownloadTaskList() {
        return mSession.getDownloadTaskBeanDao()
                .loadAll();
    }

    @Override
    public void deleteAll() {
        //清空全部数据。
    }
}
