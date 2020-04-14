package ru.ifmo.rain.ageev.hello;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.stream.IntStream.range;

class ServerWorker {
    private final static int AWAIT = 200;
    final ExecutorService workers;
    final DatagramSocket datagramSocket;
    final int size;
    final int threads;

    public ServerWorker(DatagramSocket datagramSocket, final int size, final int threads) {
        this.datagramSocket = datagramSocket;
        workers = Executors.newFixedThreadPool(threads);
        this.size = size;
        this.threads = threads;
    }

    public void start() {
//        Stream.generate(this::work/
        range(0, threads).forEach(threadId -> workers.submit(this::work));
    }

    private void work() {
        final var datagramPacket = new DatagramPacket(new byte[size], size);
        while (!(Thread.currentThread().isInterrupted() || datagramSocket.isClosed())) {
            NetUtils.receive(datagramSocket, datagramPacket);
            NetUtils.setData(datagramPacket, "Hello, " + NetUtils.getData(datagramPacket));
            NetUtils.send(datagramSocket, datagramPacket);
        }
    }

    public void shutdown() {
        datagramSocket.close();
        workers.shutdown();
        try {
            workers.awaitTermination(AWAIT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            System.err.println("Can't terminate pools: " + e.getMessage());
        }
    }
}
