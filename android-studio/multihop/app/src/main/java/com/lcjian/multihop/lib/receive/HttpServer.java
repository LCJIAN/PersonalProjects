package com.lcjian.multihop.lib.receive;

import android.text.TextUtils;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import fi.iki.elonen.NanoFileUpload;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.router.RouterNanoHTTPD;

public class HttpServer extends RouterNanoHTTPD {

    private File uploadFileDirectory;

    private CopyOnWriteArrayList<OnReceivedListener> onReceivedListeners;

    private OnReceivedListener listener;

    public HttpServer(File uploadFileDirectory, int port) {
        super(port);
        this.uploadFileDirectory = uploadFileDirectory;
        this.onReceivedListeners = new CopyOnWriteArrayList<>();
        this.listener = new OnReceivedListener() {
            @Override
            public void onTextMessageReceived(String text) {
                for (OnReceivedListener l : onReceivedListeners) {
                    l.onTextMessageReceived(text);
                }
            }

            @Override
            public void onAudioMessageReceived(File audio) {
                for (OnReceivedListener l : onReceivedListeners) {
                    l.onAudioMessageReceived(audio);
                }
            }
        };
        addMappings();
    }

    @Override
    public void addMappings() {
        super.addMappings();
        addRoute("/message/text", TextMessageHandler.class, listener);
        addRoute("/message/audio", AudioMessageHandler.class, uploadFileDirectory, listener);
    }

    public void addOnReceivedListener(OnReceivedListener onReceivedListener) {
        onReceivedListeners.add(onReceivedListener);
    }

    public void removeOnReceivedListener(OnReceivedListener onReceivedListener) {
        onReceivedListeners.remove(onReceivedListener);
    }

    public interface OnReceivedListener {

        void onTextMessageReceived(String text);

        void onAudioMessageReceived(File audio);
    }

    public static class TextMessageHandler extends DefaultHandler {

        @Override
        public String getText() {
            return "failed";
        }

        @Override
        public String getMimeType() {
            return "text/plain";
        }

        @Override
        public Response.IStatus getStatus() {
            return Response.Status.OK;
        }

        @Override
        public Response post(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            Map<String, String> map = new HashMap<>();
            try {
                session.parseBody(map);
                String body = map.get("postData");
                if (!TextUtils.isEmpty(body)) {
                    OnReceivedListener onReceivedListener = uriResource.initParameter(OnReceivedListener.class);
                    onReceivedListener.onTextMessageReceived(body);
                    return NanoHTTPD.newFixedLengthResponse(getStatus(), getMimeType(), "success");
                }
            } catch (IOException | ResponseException e) {
                e.printStackTrace();
            }

            return get(uriResource, urlParams, session);
        }
    }

    public static class AudioMessageHandler extends DefaultHandler {

        @Override
        public String getText() {
            return "failed";
        }

        @Override
        public String getMimeType() {
            return "text/plain";
        }

        @Override
        public Response.IStatus getStatus() {
            return Response.Status.OK;
        }

        @Override
        public Response post(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
            NanoFileUpload uploader = new NanoFileUpload(new DiskFileItemFactory());
            try {
                Map<String, List<FileItem>> files = uploader.parseParameterMap(session);
                FileItem fileItem = null;
                for (List<FileItem> l : files.values()) {
                    if (!l.isEmpty()) {
                        fileItem = l.get(0);
                        break;
                    }
                }
                if (fileItem == null) {
                    return get(uriResource, urlParams, session);
                } else {
                    File uploadFileDirectory = uriResource.initParameter(0, File.class);
                    OnReceivedListener onReceivedListener = uriResource.initParameter(1, OnReceivedListener.class);

                    File file = new File(uploadFileDirectory, fileItem.getName());
                    FileUtils.copyInputStreamToFile(fileItem.getInputStream(), file);
                    onReceivedListener.onAudioMessageReceived(file);
                    return NanoHTTPD.newFixedLengthResponse(getStatus(), getMimeType(), "success");
                }
            } catch (FileUploadException | IOException e) {
                e.printStackTrace();
                return get(uriResource, urlParams, session);
            }
        }
    }
}
