package ru.ifmo.rain.ageev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;

public class HelloUDPClient implements HelloClient {
    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.out.println("HelloUDPClient (name|ip) port prefix threads requests");
        } else {
            if (Arrays.stream(args).anyMatch(Objects::isNull)) {
                System.out.println("Non-null arguments expected");
                return;
            }
            try {
                new HelloUDPClient().run(args[0], Integer.parseInt(args[1]), args[2],
                        Integer.parseInt(args[3]), Integer.parseInt(args[4]));
            } catch (NumberFormatException e) {
                System.out.println("Arguments 'port', 'threads' and 'requests' are expected to be integers: " + e.getMessage());
            }
        }
    }

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        try {
            new ClientWorker(InetAddress.getByName(host), port, threads).run(prefix, requests);
        } catch (UnknownHostException e) {
            System.err.println("Unable to reach specified host: " + e.getMessage());
        } catch (InterruptedException e) {
            System.err.println("Execution was interrupted: " + e.getMessage());
        }
    }
}
