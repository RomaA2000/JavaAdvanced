package ru.ifmo.rain.ageev.hello;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.util.stream.IntStream.range;

public class ClientWorker {
    private static final int SECONDS_PER_REQUEST = 5;
    private static final int AWAIT_FOR_RESPONSE = 200;

    final SocketAddress address;
    final ExecutorService workers;
    final int threads;

    public ClientWorker(final InetAddress host, final int port, final int threads) {
        address = new InetSocketAddress(host, port);
        workers = Executors.newFixedThreadPool(threads);
        this.threads = threads;
    }

    public void run(final String prefix, final int requests) throws InterruptedException {
        range(0, threads).forEach(threadId -> workers.submit(
                () -> work(prefix, threadId, requests)));
        workers.shutdown();
        workers.awaitTermination(SECONDS_PER_REQUEST * requests * threads, TimeUnit.SECONDS);
    }

    private void work(final String prefix, final int threadId, final int requests) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(AWAIT_FOR_RESPONSE);
            var size = socket.getReceiveBufferSize();
            final var datagramPacket = new DatagramPacket(new byte[size], size, address);
            range(0, requests).mapToObj(request -> NetUtils.makeData(prefix, threadId, request))
                    .forEach(m -> sendAndReceive(m, socket, datagramPacket, size));
        } catch (SocketException e) {
            System.err.println("Can't set connection with socket: " + e.getMessage());
        }
    }

    private void sendAndReceive(final String message, final DatagramSocket socket,
                                final DatagramPacket datagramPacket, final int size) {
        while (!(socket.isClosed() || Thread.interrupted())) {
            NetUtils.setData(datagramPacket, message);
            try {
                socket.send(datagramPacket);
            } catch (IOException e) {
                System.err.println("Exception while sending: " + e.getMessage());
            }
            datagramPacket.setData(new byte[size]);
            try {
                socket.receive(datagramPacket);
            } catch (IOException e) {
                System.err.println("Exception while receiving: " + e.getMessage());
            }
            var response = NetUtils.getData(datagramPacket);
            if (response.contains(message)) {
                System.out.println(response);
                break;
            }
        }
    }
}
