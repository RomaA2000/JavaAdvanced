package ru.ifmo.rain.ageev.hello;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.stream.IntStream.range;

public class ServerWorker {
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

    public void run() {
        range(0, threads).forEach(threadId -> workers.submit(
                this::work));
    }

    private void work() {
        var datagramPacket = new DatagramPacket(new byte[size], size);
        while (!(Thread.currentThread().isInterrupted() || datagramSocket.isClosed())) {
            NetUtils.receive(datagramSocket, datagramPacket);
            NetUtils.setData(datagramPacket, "Hello, " + NetUtils.getData(datagramPacket));
            NetUtils.send(datagramSocket, datagramPacket);
        }
    }

    public void shutdown() {
        datagramSocket.close();
        workers.shutdown();
    }
}
