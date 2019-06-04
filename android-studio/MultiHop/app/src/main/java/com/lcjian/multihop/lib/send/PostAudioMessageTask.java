package com.lcjian.multihop.lib.send;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class PostAudioMessageTask implements Task {

    private File audio;

    public PostAudioMessageTask(File audio) {
        this.audio = audio;
    }

    @Override
    public void run(String ip, int port) throws Exception {
        new OkHttpClient()
                .newCall(new Request.Builder()
                        .url("http://" + ip + ":" + port + "/message/audio")
                        .post(new MultipartBody.Builder()
                                .addPart(MultipartBody.Part.createFormData(
                                        "audio_file", audio.getName(), RequestBody.create(MediaType.parse("audio/*"), audio)))
                                .build())
                        .build())
                .execute();
    }

}
