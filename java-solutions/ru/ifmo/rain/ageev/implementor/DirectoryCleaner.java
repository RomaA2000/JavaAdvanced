package ru.ifmo.rain.ageev.implementor;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Recursive directory cleaner for {@link JarImplementor}.
 */
public class DirectoryCleaner extends SimpleFileVisitor<Path> {
    /**
     * Default constructor. Creates new instance of {@link DirectoryCleaner} class
     */
    DirectoryCleaner() {
        super();
    }

    /**
     * File visitor. Visits file and deletes it from file system
     *
     * @param file  {@link Path} to file to be deleted
     * @param attrs {@link BasicFileAttributes} file attributes of given {@code file}
     * @return {@link FileVisitResult} if no error occurs
     * @throws IOException if file deletion fails
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
    }

    /**
     * Directory visitor. Visits directory and deleted it from file system
     *
     * @param dir {@link Path} to directory to be deleted
     * @param exc {@link IOException} instance if any error occurs during directory visiting
     * @return {@link FileVisitResult} if no error occurs
     * @throws IOException if directory deletion fails
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        Files.delete(dir);
        return FileVisitResult.CONTINUE;
    }
}

