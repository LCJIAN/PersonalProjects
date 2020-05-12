package org.gradle.mesh.roster;

import org.gradle.mesh.NodeId;
import org.gradle.mesh.packet.Presence;

public interface RosterListener {

    void entryAdded(NodeId mid);

    void entryDeleted(NodeId mid);

    void presenceChanged(Presence presence);
}
