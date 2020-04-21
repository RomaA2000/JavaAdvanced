package ru.ifmo.rain.ageev.hello;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

class NetUtils {
    public static void setData(final DatagramPacket packet, final String s) {
        packet.setData(s.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean receive(final DatagramSocket socket, final DatagramPacket packet) {
        try {
            socket.receive(packet);
            return true;
        } catch (final IOException e) {
            if (!socket.isClosed()) {
                System.err.println("Exception while receiving: " + e.getMessage());
            }
            return false;
        }
    }

    public static boolean send(final DatagramSocket socket, final DatagramPacket packet) {
        try {
            socket.send(packet);
            return true;
        } catch (final IOException e) {
            if (!socket.isClosed()) {
                System.err.println("Exception while sending: " + e.getMessage());
            }
            return false;
        }
    }

    public static String getData(final DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }

    private static int skipByDigitsImpl(int idx, String s, boolean needToSkip) {
        while ((idx < s.length()) && (Character.isDigit(s.charAt(idx)) ^ needToSkip)) {
            idx++;
        }
        return idx;
    }

    public static int skipToDigits(int idx, String s) {
        return skipByDigitsImpl(idx, s, true);
    }

    public static int skipDigits(int idx, String s) {
        return skipByDigitsImpl(idx, s, false);
    }

    public static boolean check(final String string, final int threadId, final int requestId) {
        final String idThread = Integer.toString(threadId);
        final String idRequest = Integer.toString(requestId);
        int firstNumberStart = skipToDigits(0, string);
        int firstNumberEnd = skipDigits(firstNumberStart, string);
        if (!idThread.equals(string.substring(firstNumberStart, firstNumberEnd))) {
            return false;
        }
        firstNumberStart = skipToDigits(firstNumberEnd, string);
        firstNumberEnd = skipDigits(firstNumberStart, string);
        if (!idRequest.equals(string.substring(firstNumberStart, firstNumberEnd))) {
            return false;
        }
        return string.length() == skipToDigits(firstNumberEnd, string);
    }
}
