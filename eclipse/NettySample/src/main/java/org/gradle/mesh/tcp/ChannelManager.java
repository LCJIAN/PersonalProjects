package org.gradle.mesh.tcp;

import java.util.HashMap;
import java.util.Map;

import org.gradle.mesh.NodeId;
import org.gradle.mesh.Pair;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ChannelManager {

    private final Map<Pair<NodeId>, Channel> channels;
    private final ChannelGroup channelGroup;

    public ChannelManager() {
        super();
        channels = new HashMap<>();
        channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    }

    public ChannelGroup channelGroup() {
        return channelGroup;
    }

}
