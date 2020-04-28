package ru.ifmo.rain.ageev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
            new HelloUDPServer().start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
            final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Press any key to terminate");
            reader.readLine();
        } catch (final NumberFormatException e) {
            System.err.println("Arguments must be integers: " + e.getMessage());
        } catch (final IOException e) {
            System.err.println("IO error occurred: " + e.getMessage());
        }
    }

    @Override
    public void start(final int port, final int threads) {
        if (threads <= 0) {
            throw new IllegalArgumentException("threads count must be greater than 0");
        }
        try {
            final var datagramSocket = new DatagramSocket(port);
            final var size = datagramSocket.getReceiveBufferSize();
            serverWorker = new ServerWorker(datagramSocket, size, threads);
            serverWorker.start();
        } catch (final SocketException e) {
            System.err.println("Failed to create socket or get his buffer size: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        serverWorker.shutdown();
    }
}
