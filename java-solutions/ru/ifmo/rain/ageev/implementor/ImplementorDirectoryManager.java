package ru.ifmo.rain.ageev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class providing directories management for {@link JarImplementor}
 *
 * @author ageev
 * @version 1.0
 */
public class ImplementorDirectoryManager {
    /**
     * Directory {@link Path} object where all files created by the {@link JarImplementor} will be stored
     */
    private Path dir;

    /**
     * Constructor from {@link Path} object. Creates a new instance of {@link ImplementorDirectoryManager}
     *
     * @param path {@link Path} to location where to create {@link #dir}
     * @throws ImplerException if an error occurs while creating directory
     */
    ImplementorDirectoryManager(Path path) throws ImplerException {
        if (path == null) {
            throw new ImplerException("Not null directory expected");
        }
        try {
            dir = Files.createTempDirectory(path.toAbsolutePath(), "dir");
        } catch (IOException e) {
            throw new ImplerException("Unable to create directory:" + e.getMessage(), e);
        }
    }

    /**
     * Static method which creates all upper directories for given {@link Path}
     *
     * @param path {@link Path} pointing to target location
     * @throws ImplerException if null path given or an error occurs while creating directories leading to given {@code path}
     * @see Path#getParent()
     */
    public static void createDirectoriesOnPath(Path path) throws ImplerException {
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new ImplerException("Unable to create directories on path", e);
        } catch (NullPointerException e) {
            throw new ImplerException("Not null path expected", e);
        }
    }

    /**
     * Getter for {@code token} implementation path
     *
     * @param token an instance of {@link Class} for which method generates path
     * @return {@link String} with package name and simple name
     */
    public static String getImplementationPath(Class<?> token) {
        return String.join(File.separator, token.getPackageName().split("\\.")) +
                File.separator +
                token.getSimpleName();
    }

    /**
     * Getter for {@link #dir}
     *
     * @return directory {@link Path} stored in this {@link ImplementorDirectoryManager}
     */
    public Path getDirectory() {
        return dir;
    }

    /**
     * Recursively deletes {@link #dir} using {@link DirectoryCleaner}
     *
     * @throws ImplerException if an error occurs during cleaning {@link #dir}
     * @see Files#walkFileTree(Path, FileVisitor)
     */
    public void cleanDirectory() throws ImplerException {
        try {
            Files.walkFileTree(dir, new DirectoryCleaner());
        } catch (IOException e) {
            throw new ImplerException("Can't delete directory: ", e);
        }
    }

    /**
     * Makes valid code directories.
     *
     * @param token {@link Class} class, which implementation is required
     * @param separator {@link String} separator
     * @return Path to source code as {@link String}
     */
    static String getImplementationPath(Class<?> token, String separator) {
        return String.join(separator, token.getPackageName().split("\\.")) +
                separator +
                token.getSimpleName();
    }
}
