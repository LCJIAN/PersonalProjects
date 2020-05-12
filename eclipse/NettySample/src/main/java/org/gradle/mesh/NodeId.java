package org.gradle.mesh;

import java.io.Serializable;

public class NodeId implements Comparable<NodeId>, Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    private String macAddress;

    public NodeId(String macAddress) {
        super();
        this.macAddress = macAddress;
    }

    @Override
    public String toString() {
        return macAddress;
    }

    @Override
    public final int hashCode() {
        return macAddress.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof NodeId) {
            NodeId anotherMid = (NodeId) obj;
            return macAddress.equals(anotherMid.macAddress);
        }
        return false;
    }

    @Override
    public int compareTo(NodeId o) {
        return toString().compareTo(o.toString());
    }

}
