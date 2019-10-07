package com.winside.lighting.mesh;

import java.util.ArrayList;
import java.util.List;

public class WEncoder implements Encoder {

    private byte tid;

    @Override
    public List<byte[]> encode(Packet packet, int mtu) {
        byte[] data = packet.getData();
        byte[] encryptedData = data;

        int maxFrameDataUnit = mtu - 4;
        byte frameCount = (byte) (encryptedData.length % maxFrameDataUnit == 0
                ? encryptedData.length / maxFrameDataUnit
                : encryptedData.length / maxFrameDataUnit + 1);

        List<byte[]> result = new ArrayList<>();
        byte cursor = 0;
        for (byte frameIndex = 0; frameIndex < frameCount; frameIndex++) {

            byte[] frameData;
            if (frameIndex == frameCount - 1) {
                frameData = new byte[encryptedData.length % maxFrameDataUnit == 0 ? maxFrameDataUnit : encryptedData.length % maxFrameDataUnit];
            } else {
                frameData = new byte[maxFrameDataUnit];
            }

            for (int x = 0; x < frameData.length; x++) {
                frameData[x] = encryptedData[cursor];
                cursor++;
            }

            byte[] frame = new byte[4 + frameData.length];
            frame[0] = (byte) frame.length;
            frame[1] = frameCount;
            frame[2] = frameIndex;
            frame[3] = tid;

            System.arraycopy(frameData, 0, frame, 4, frameData.length);
            result.add(frame);
        }

        tid = (byte) (tid + 1);
        return result;
    }

}
