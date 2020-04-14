package ru.ifmo.rain.ageev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Objects;

public class HelloUDPServer implements HelloServer {
    ServerWorker serverWorker;

    public static void main(final String[] args) {
        if (args == null || args.length != 2 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("HelloUDPServer port threads");
            return;
        }
        try {
            var server = new HelloUDPServer();
            server.start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        } catch (NumberFormatException e) {
            System.err.println("Arguments must be integers: " + e.getMessage());
        }
    }

    @Override
    public void start(int port, int threads) {
        if (threads <= 0) {
            throw new IllegalArgumentException("threads count must be greater than 0");
        }
        try {
            var datagramSocket = new DatagramSocket(port);
            var size = datagramSocket.getReceiveBufferSize();
            serverWorker = new ServerWorker(datagramSocket, size, threads);
            serverWorker.run();
        } catch (SocketException e) {
            System.err.println("Failed to create socket or get his buffer size: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        serverWorker.shutdown();
    }
}
