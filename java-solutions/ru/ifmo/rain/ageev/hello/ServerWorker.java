package ru.ifmo.rain.ageev.hello;

import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;

public class ServerWorker {
    final SocketAddress address;
    final ExecutorService workers;
    final int threads;

    public ServerWorker(SocketAddress address) {
        this.address = address;
    }
}
