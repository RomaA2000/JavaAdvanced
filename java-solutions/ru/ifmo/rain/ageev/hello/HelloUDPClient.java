package ru.ifmo.rain.ageev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;

public class HelloUDPClient implements HelloClient {
    public static void main(final String[] args) {
        if (args == null || args.length != 5 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.out.println("HelloUDPClient host port prefix threads requests");
            return;
        }
        try {
            new HelloUDPClient().run(args[0], Integer.parseInt(args[1]), args[2],
                    Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        } catch (final NumberFormatException e) {
            System.out.println("Arguments must be integers: " + e.getMessage());
        }
    }

    @Override
    public void run(final String host, final int port, final String prefix, final int threads, final int requests) {
        Objects.requireNonNull(host);
        Objects.requireNonNull(prefix);
        if (threads <= 0) {
            throw new IllegalArgumentException("threads count must be greater than 0");
        }
        if (requests <= 0) {
            throw new IllegalArgumentException("requests count must be greater than 0");
        }
        try {
            new ClientWorker(InetAddress.getByName(host), port, threads).run(prefix, requests);
        } catch (final UnknownHostException e) {
            System.err.println("Unable to reach specified host: " + e.getMessage());
        } catch (final InterruptedException e) {
            System.err.println("Execution was interrupted: " + e.getMessage());
        }
    }
}
