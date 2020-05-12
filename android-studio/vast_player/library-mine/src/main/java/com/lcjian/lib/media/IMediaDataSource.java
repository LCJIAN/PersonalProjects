package com.lcjian.lib.media;

import java.io.IOException;

public interface IMediaDataSource {

    int readAt(long position, byte[] buffer, int offset, int size) throws IOException;

    long getSize() throws IOException;

    void close() throws IOException;

}
