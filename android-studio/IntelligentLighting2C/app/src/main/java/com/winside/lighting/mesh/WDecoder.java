package com.winside.lighting.mesh;

import com.winside.lighting.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class WDecoder implements Decoder {

    private byte tempFrameTid;
    private byte expectationFrameIndex;
    private List<byte[]> tempFrameDataList;

    @Override
    public Packet decode(byte[] value) {
        byte length = (byte) (value[0] + 1);
        byte totalFrame = value[1];
        byte frameIndex = value[2];
        byte tid = value[3];

        byte[] frameData = new byte[length - 4];
        System.arraycopy(value, 4, frameData, 0, length - 4);

        if (frameIndex == 0) {
            tempFrameTid = tid;
            expectationFrameIndex = 0;
            tempFrameDataList = new ArrayList<>();
        }
        if (expectationFrameIndex == frameIndex && tempFrameTid == tid) {
            tempFrameDataList.add(frameData);

            if (expectationFrameIndex == totalFrame - 1) {
                byte[] encryptedData = Utils.concatForByte(tempFrameDataList);
                byte[] data = encryptedData;
                Packet packet = new Packet();
                packet.setData(data);
                tempFrameDataList = null;
                return packet;
            } else {
                expectationFrameIndex = (byte) (expectationFrameIndex + 1);
            }
        }

        return null;
    }

}
