package org.gradle.mesh.packet;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.gradle.mesh.NodeId;
import org.gradle.mesh.packet.handler.Packer;
import org.gradle.mesh.packet.handler.UnPacker;

public class Presence extends Packet {

    private String nodeName;
    private String nodeAvatar;

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeAvatar() {
        return nodeAvatar;
    }

    public void setNodeAvatar(String nodeAvatar) {
        this.nodeAvatar = nodeAvatar;
    }

    public static class PresencePacker implements Packer {

        @Override
        public Packet pack(PackerChain chain) {
            String content = chain.content();
            JsonObject packetObject = new JsonParser().parse(content).getAsJsonObject();
            if (packetObject.get("packet_name").getAsString().equals("Presence")) {
                JsonObject presenceObject = packetObject.get("packet_body").getAsJsonObject();

                Presence presence = new Presence();
                presence.setPacketId(packetObject.get("packet_id").getAsString());
                presence.setPacketTo(new NodeId(packetObject.get("packet_to").getAsString()));
                presence.setPacketFrom(new NodeId(packetObject.get("packet_from").getAsString()));
                if (!(presenceObject.get("node_name") instanceof JsonNull)) {
                    presence.setNodeName(presenceObject.get("node_name").getAsString());
                }
                if (!(presenceObject.get("node_avatar") instanceof JsonNull)) {
                    presence.setNodeAvatar(presenceObject.get("node_avatar").getAsString());
                }
                return presence;
            }
            return chain.proceed(content);
        }
    }

    public static class PresenceUnPacker implements UnPacker {

        @Override
        public String unPack(UnPackerChain chain) {
            Packet packet = chain.content();
            if (packet instanceof Presence) {
                Presence presence = (Presence) packet;
                JsonObject presenceObject = new JsonObject();
                presenceObject.addProperty("node_name", presence.getNodeName());
                presenceObject.addProperty("node_avatar", presence.getNodeAvatar());

                JsonObject packetObject = new JsonObject();
                packetObject.addProperty("packet_id", presence.getPacketId());
                packetObject.addProperty("packet_to", presence.getPacketTo().toString());
                packetObject.addProperty("packet_from", presence.getPacketFrom().toString());
                packetObject.addProperty("packet_name", "Presence");
                packetObject.add("packet_body", presenceObject);
                return packetObject.toString();
            }
            return chain.proceed(packet);
        }
    }
}
