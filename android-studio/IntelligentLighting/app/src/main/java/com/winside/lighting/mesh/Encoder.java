package com.winside.lighting.mesh;

import java.util.List;

public interface Encoder {

    List<byte[]> encode(Packet packet, int mtu);


}
