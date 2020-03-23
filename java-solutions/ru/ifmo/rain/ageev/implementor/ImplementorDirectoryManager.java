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
     * Constructor from {@link Path} object. Creates a new instance of this class
     *
     * @param path {@link Path} to location where to create {@link #dir}
     * @throws ImplerException if an error occurs while creating directory
     */
    ImplementorDirectoryManager(Path path) throws ImplerException {
        if (path == null) {
            throw new ImplerException("Not null directory expected");
        }
        try {
            dir = Files.createTempDirectory(path.toAbsolutePath().getParent(), "dir");
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
     * Returns the path of file to be generated for token.
     *
     * @param token     the token go get name and packages from
     * @param root      the directory where the path begins
     * @param extension extension of the file to be generated
     * @return the described instance of {@link Path}
     */
    public static Path getFilePath(Class<?> token, Path root, String extension) {
        return root
                .resolve(token.getPackageName().replace('.', File.separatorChar))
                .resolve(Implementor.makeName(token) + extension);
    }

    /**
     * Getter for {@link #dir}
     *
     * @return directory {@link Path} stored in this class
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
}
