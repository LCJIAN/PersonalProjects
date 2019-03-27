package com.lcjian.multihop.lib.send;

public interface Task {

    void run(String ip, int port) throws Exception;
}
