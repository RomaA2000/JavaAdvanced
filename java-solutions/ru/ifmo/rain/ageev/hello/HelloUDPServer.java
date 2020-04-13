package ru.ifmo.rain.ageev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.net.DatagramSocket;
import java.net.SocketException;

public class HelloUDPServer implements HelloServer {
    @Override
    public void start(int port, int threads) {
        try {
            var socket = new DatagramSocket(port);

        } catch (SocketException e) {
            System.err.println("Failed to create socket on port " + port + ": " + e.getMessage());
        }
    }

    @Override
    public void close() {

    }
}
