package com.example.newbiechen.ireader.model.local;

import com.example.newbiechen.ireader.model.bean.DownloadTaskBean;

import java.util.List;

/**
 * Created by newbiechen on 17-4-28.
 */

public interface GetDbHelper {
    List<DownloadTaskBean> getDownloadTaskList();
}
