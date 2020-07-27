package com.org.chat;

import com.org.firefighting.App;

import java.io.File;

public class Constants {

    public final static File DIRECTORY = App.getInstance().getExternalFilesDir("smack");

    public final static File DIRECTORY_IMAGE = new File(DIRECTORY, "image");

    public final static File DIRECTORY_AVATAR = new File(DIRECTORY, "avatar");

    public final static File DIRECTORY_V_CARD = new File(DIRECTORY, "vCard");

    public final static File DIRECTORY_VOICE = new File(DIRECTORY, "voice");

}
