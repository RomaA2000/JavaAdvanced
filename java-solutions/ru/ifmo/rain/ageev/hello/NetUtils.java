package ru.ifmo.rain.ageev.hello;

import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;

class NetUtils {
    public static String makeData(final String prefix, final int threadId, final int taskId) {
        return prefix + threadId + "_" + taskId;
    }

    public static void setData(final DatagramPacket packet, final String s) {
        packet.setData(s.getBytes(StandardCharsets.UTF_8));
    }

    public static String getData(final DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }
}
