package ru.ifmo.rain.ageev.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final int perHost;
    private final ConcurrentHashMap<String, HostDownloadersControl> downloaderFromHost = new ConcurrentHashMap<>();
    private final Pools pools;

    public WebCrawler(final Downloader downloader, final int downloaders, final int extractors, final int perHost) {
        this.downloader = downloader;
        this.perHost = perHost;
        pools = new Pools(extractors, downloaders);
    }

    @Override
    public Result download(final String url, final int depth) {
        return new Worker(url).result(depth);
    }

    @Override
    public void close() {
        pools.shutdown();
    }

    private class HostDownloadersControl {
        private final Queue<Runnable> tasksQueue = new ArrayDeque<>();
        private int nowRunning = 0;

        public synchronized void run(final Runnable task) {
            tasksQueue.add(task);
            runNext();
        }

        private synchronized void runNext() {
            if (nowRunning < perHost) {
                if (!tasksQueue.isEmpty()) {
                    final var task = tasksQueue.poll();
                    nowRunning++;
                    pools.submitDownloader(() -> {
                        try {
                            task.run();
                        } finally {
                            synchronized (HostDownloadersControl.this) {
                                nowRunning--;
                            }
                            runNext();
                        }
                    });
                }
            }
        }
    }

    private class Worker {
        private final Set<String> results = ConcurrentHashMap.newKeySet();
        private final Set<String> usedUrls = ConcurrentHashMap.newKeySet();
        private final ConcurrentMap<String, IOException> errors = new ConcurrentHashMap<>();
        private ConcurrentLinkedQueue<String> level = new ConcurrentLinkedQueue<>();
        private Phaser levelPhaser;

        Worker(final String url) {
            level.add(url);
        }

        private void extract(final Document page, final int nowDepth) {
            if (nowDepth == 0) {
                return;
            }
            levelPhaser.register();
            pools.submitExtractor(() -> {
                try {
                    level.addAll(page.extractLinks());
                } catch (final IOException ignored) {
                } finally {
                    levelPhaser.arrive();
                }
            });
        }

        private void download(final String link, final int nowDepth) {
            final String newHost;
            try {
                newHost = URLUtils.getHost(link);
            } catch (final MalformedURLException e) {
                errors.put(link, e);
                return;
            }
            final var hostDownloader = downloaderFromHost
                    .computeIfAbsent(newHost, unused -> new HostDownloadersControl());
            levelPhaser.register();
            hostDownloader.run(() -> {
                try {
                    final var page = downloader.download(link);
                    results.add(link);
                    extract(page, nowDepth);
                } catch (final IOException e) {
                    errors.put(link, e);
                } finally {
                    levelPhaser.arrive();
                }
            });
        }

        public Result result(int depth) {
            while (depth-- > 0) {
                levelPhaser = new Phaser(0);
                final var newlevel = level;
                level = new ConcurrentLinkedQueue<>();
                final int nowDepth = depth;
                newlevel.stream()
                        .filter(usedUrls::add)
                        .forEach(link -> download(link, nowDepth));
                levelPhaser.awaitAdvance(0);
            }
            return new Result(new ArrayList<>(results), errors);
        }
    }

    private static int checkedGet(final String[] args, final int i) {
        return i > args.length ? 1 : Integer.parseInt(args[i]);
    }

    /**
     * This method is used to call the {@link WebCrawler#download}.
     *
     * @param args the arguments
     */
    public static void main(final String[] args) {
        if (args == null || args.length == 0 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Url [depth [downloads [extractors [perHost]]]]");
            return;
        }
        try {
            try (final WebCrawler crawler = new WebCrawler(new CachingDownloader(), checkedGet(args, 2),
                    checkedGet(args, 3), checkedGet(args, 4))) {
                crawler.download(args[0], checkedGet(args, 1));
            }
        } catch (final NumberFormatException e) {
            System.err.println("Arguments after first must be numbers: " + e.getMessage());
        } catch (final IOException e) {
            System.err.println("Failed to download: " + e.getMessage());
        }
    }
}
