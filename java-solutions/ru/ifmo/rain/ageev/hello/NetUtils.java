package ru.ifmo.rain.ageev.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

class NetUtils {
    public static void setData(final DatagramPacket packet, final String s) {
        packet.setData(s.getBytes(StandardCharsets.UTF_8));
    }

    public static void receive(final DatagramSocket socket, final DatagramPacket packet) {
        try {
            socket.receive(packet);
        } catch (IOException e) {
            if (!socket.isClosed()) {
                System.err.println("Exception while receiving: " + e.getMessage());
            }
        }
    }

    public static void send(final DatagramSocket socket, final DatagramPacket packet) {
        try {
            socket.send(packet);
        } catch (IOException e) {
            if (!socket.isClosed()) {
                System.err.println("Exception while sending: " + e.getMessage());
            }
        }
    }

    public static String getData(final DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }

    static boolean check(String s, int threadId, int requestId) {
        final String expr = "[\\D]*";
        return s.matches( expr + threadId + expr + requestId + expr);
    }
}
