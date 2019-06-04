package org.gradle.mesh.packet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.gradle.mesh.NodeId;
import org.gradle.mesh.packet.handler.Packer;
import org.gradle.mesh.packet.handler.UnPacker;

public class Message extends Packet {

    private Type type;
    private String content;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public enum Type {

        TEXT,

        IMAGE,

        AUDIO,

        VIDEO,

        FILE;

        public static Type fromString(String name) {
            try {
                return Type.valueOf(name);
            } catch (Exception e) {
                return TEXT;
            }
        }
    }

    public static class MessagePacker implements Packer {

        @Override
        public Packet pack(PackerChain chain) {
            String content = chain.content();
            JsonObject packetObject = new JsonParser().parse(content).getAsJsonObject();
            if (packetObject.get("packet_name").getAsString().equals("message")) {
                JsonObject messageObject = packetObject.get("packet_body").getAsJsonObject();
                Message message = new Message();
                message.setPacketId(packetObject.get("packet_id").getAsString());
                message.setPacketTo(new NodeId(packetObject.get("packet_to").getAsString()));
                message.setPacketFrom(new NodeId(packetObject.get("packet_from").getAsString()));

                message.setType(Type.fromString(messageObject.get("type").getAsString()));
                message.setContent(messageObject.get("content").getAsString());
                return message;
            }
            return chain.proceed(content);
        }
    }

    public static class MessageUnPacker implements UnPacker {

        @Override
        public String unPack(UnPackerChain chain) {
            Packet packet = chain.content();
            if (packet instanceof Message) {
                Message message = (Message) packet;
                JsonObject messageObject = new JsonObject();
                messageObject.addProperty("type", message.getType().toString());
                messageObject.addProperty("content", message.getContent());

                JsonObject packetObject = new JsonObject();
                packetObject.addProperty("packet_id", message.getPacketId());
                packetObject.addProperty("packet_to", message.getPacketTo().toString());
                packetObject.addProperty("packet_from", message.getPacketFrom().toString());
                packetObject.addProperty("packet_name", "message");
                packetObject.add("packet_body", messageObject);
                return packetObject.toString();
            }
            return chain.proceed(packet);
        }
    }
}
