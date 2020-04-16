package ru.ifmo.ageev.walk;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

public class FileHasher extends SimpleFileVisitor<Path> {
    private static final int BUFF_SIZE = 8192;
    private final BufferedWriter fileWriter;
    private byte[] buffer = new byte[BUFF_SIZE];

    FileHasher(BufferedWriter writer) {
        fileWriter = writer;
    }

    private FileVisitResult writeData(int hash, final Path filePath) throws IOException {
        writeToFile(hash, filePath.toString());
        return CONTINUE;
    }

    public void writeToFile(int hash, final String filePath) throws IOException {
        fileWriter.write(String.format("%08x %s%n", hash, filePath));
    }

    @Override
    public FileVisitResult visitFile(final Path filePath, final BasicFileAttributes attrs) throws IOException {
        int hash;
        try {
            try (FileInputStream fileInputStream = new FileInputStream(filePath.toString())) {
                BufferHasher bufferHasher = new BufferHasher();
                int size;
                while ((size = fileInputStream.read(buffer)) >= 0) {
                    bufferHasher.hash(buffer, size);
                }
                hash = bufferHasher.getHash();
            }
        } catch (SecurityException | IOException e) {
            return writeData(0, filePath);
        }
        return writeData(hash, filePath);
    }

    @Override
    public FileVisitResult visitFileFailed(final Path filePath, final IOException e) throws IOException {
        return writeData(0, filePath);
    }
}
