package org.gradle.mesh.packet;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.gradle.mesh.NodeId;
import org.gradle.mesh.packet.handler.Packer;
import org.gradle.mesh.packet.handler.UnPacker;

public class NodeIQ extends IQ {

    private String nodeId;
    private String nodeName;
    private String nodeAvatar;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

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

    public static class NodeIQPacker implements Packer {

        @Override
        public Packet pack(PackerChain chain) {
            String content = chain.content();
            JsonObject packetObject = new JsonParser().parse(content).getAsJsonObject();
            if (packetObject.get("packet_name").getAsString().equals("IQ")) {
                JsonObject iqObject = packetObject.get("packet_body").getAsJsonObject();
                if (iqObject.get("iq_name").getAsString().equals("NodeIQ")) {
                    JsonObject nodeIqObject = iqObject.get("iq_body").getAsJsonObject();

                    NodeIQ nodeIQ = new NodeIQ();
                    nodeIQ.setPacketId(packetObject.get("packet_id").getAsString());
                    nodeIQ.setPacketTo(new NodeId(packetObject.get("packet_to").getAsString()));
                    nodeIQ.setPacketFrom(new NodeId(packetObject.get("packet_from").getAsString()));
                    nodeIQ.setType(Type.fromString(iqObject.get("iq_type").getAsString()));
                    nodeIQ.setNodeId(nodeIqObject.get("node_id").getAsString());
                    nodeIQ.setNodeName(nodeIqObject.get("node_name").getAsString());
                    nodeIQ.setNodeAvatar(nodeIqObject.get("node_avatar").getAsString());
                    nodeIQ.setType(Type.GET);
                    return nodeIQ;
                }
            }
            return chain.proceed(content);
        }
    }

    public static class NodeIQUnPacker implements UnPacker {

        @Override
        public String unPack(UnPackerChain chain) {
            Packet packet = chain.content();
            if (packet instanceof NodeIQ) {
                NodeIQ nodeIQ = (NodeIQ) packet;
                JsonObject nodeIqObject = new JsonObject();
                nodeIqObject.addProperty("node_id", nodeIQ.getNodeId());
                nodeIqObject.addProperty("node_name", nodeIQ.getNodeName());
                nodeIqObject.addProperty("node_avatar", nodeIQ.getNodeAvatar());

                JsonObject iqObject = new JsonObject();
                iqObject.addProperty("iq_name", "NodeIQ");
                iqObject.addProperty("iq_type", nodeIQ.getType().toString());
                iqObject.add("iq_body", nodeIqObject);

                JsonObject packetObject = new JsonObject();
                packetObject.addProperty("packet_id", nodeIQ.getPacketId());
                packetObject.addProperty("packet_to", nodeIQ.getPacketTo().toString());
                packetObject.addProperty("packet_from", nodeIQ.getPacketFrom().toString());
                packetObject.addProperty("packet_name", "IQ");
                packetObject.add("packet_body", iqObject);
                return packetObject.toString();
            }
            return chain.proceed(packet);
        }
    }
}
