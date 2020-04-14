package ru.ifmo.rain.ageev.hello;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.stream.IntStream.range;

public class ClientWorker {
    private static final int SECONDS_PER_REQUEST = 100;
    private static final int AWAIT_FOR_RESPONSE = 1000;

    final SocketAddress address;
    final ExecutorService workers;
    final int threads;

    public ClientWorker(final InetAddress host, final int port, final int threads) {
        address = new InetSocketAddress(host, port);
        workers = Executors.newFixedThreadPool(threads);
        this.threads = threads;
    }

    public static String makeData(final String prefix, final int threadId, final int taskId) {
        return prefix + threadId + "_" + taskId;
    }

    public void run(final String prefix, final int requests) throws InterruptedException {
        range(0, threads).forEach(threadId -> workers.submit(
                () -> work(prefix, threadId, requests)));
        workers.shutdown();
        workers.awaitTermination(SECONDS_PER_REQUEST * requests * threads, TimeUnit.SECONDS);
    }

    private void work(final String prefix, final int threadId, final int requests) {
        try (var datagramSocket = new DatagramSocket()) {
            datagramSocket.setSoTimeout(AWAIT_FOR_RESPONSE);
            var size = datagramSocket.getReceiveBufferSize();
            final var datagramPacket = new DatagramPacket(new byte[size], size, address);
            range(0, requests).mapToObj(request -> makeData(prefix, threadId, request))
                    .forEach(m -> sendAndReceive(m, datagramSocket, datagramPacket, size));
        } catch (SocketException e) {
            System.err.println("Can't set connection with socket: " + e.getMessage());
        }
    }

    private void sendAndReceive(final String message, final DatagramSocket datagramSocket,
                                final DatagramPacket datagramPacket, final int size) {
        while (!(datagramSocket.isClosed() || Thread.currentThread().isInterrupted())) {
            NetUtils.setData(datagramPacket, message);
            NetUtils.send(datagramSocket, datagramPacket);
            datagramPacket.setData(new byte[size]);
            NetUtils.receive(datagramSocket, datagramPacket);
            var response = NetUtils.getData(datagramPacket);
            if (true) {
                System.out.println(response);
            }
        }
    }
}
