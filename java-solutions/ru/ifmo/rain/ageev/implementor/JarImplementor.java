package ru.ifmo.rain.ageev.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;


/**
 * Class implementing {@link JarImpler}. Provides public methods to implement {@code .java}
 * and {@code .jar} files for classes extending given class (or implementing given interface).
 *
 * @author ageev
 * @version 1.0
 */
public class JarImplementor extends Implementor implements JarImpler {
    /**
     * Default constructor. Creates new instance of this class.
     */
    public JarImplementor() {
    }

    /**
     * Provides console interface for this class.
     * If 3 arguments provided {@code -jar className jarOutputPath} creates {@code .jar} file using {@link JarImpler} method {@link #implementJar(Class, Path)}
     * All arguments must be not-null. If some arguments are incorrect
     * or an error occurs in runtime an information message is printed in err stream and program is aborted.
     *
     * @param args command line arguments for application
     */
    public static void main(final String[] args) {
        if (args == null || args.length == 3) {
            System.err.println("Invalid arguments number, expected -jar <class name> <output path>");
        } else {
            for (final String arg : args) {
                if (arg == null) {
                    System.err.println("Not null args expected");
                    return;
                }
            }
            try {
                if (!"-jar".equals(args[0])) {
                    System.err.printf("Invalid arguments, only option available is -jar and %s given", args[0]);
                } else {
                    new JarImplementor().implementJar(Class.forName(args[1]), Path.of(args[2]));
                }
            } catch (final ClassNotFoundException e) {
                System.err.print("Invalid class name");
            } catch (final InvalidPathException e) {
                System.err.print("Invalid path");
            } catch (final ImplerException e) {
                System.err.print("Can't create java file");
            }
        }
    }


    /**
     * Compiles implemented class extending or implementing {@code token}
     * and stores {@code .class} file in given {@code tempDirectory}.
     * <p>
     * Uses {@code -classpath} pointing to location of class or interface specified by {@code token}.
     *
     * @param token type token that need to be implemented
     * @param path  to store {@code .class} files
     * @throws ImplerException if an error occurs
     */
    private void compileClass(final Class<?> token, final Path path) throws ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new ImplerException("Failed to get the system compiler");
        }
        final String extendPath;
        try {
            extendPath = Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (final URISyntaxException e) {
            throw new ImplerException("Could not convert URL to URI", e);
        }
        final int exitCode = compiler.run(null, null, null, "-cp", extendPath,
                ImplementorDirectoryManager.getFilePath(token, path, ".java").toString());
        if (exitCode != 0) {
            throw new ImplerException("Failed to compile code: compiler exit code is " + exitCode);
        }
    }

    /**
     * Makes a {@code .jar} file containing sources of implemented class using {@link Manifest}.
     *
     * @param jarFile       path where  {@code .jar} file will be saved
     * @param tempDirectory temporary directory where all {@code .class} files are stored
     * @param token         type token that needs to be implemented
     * @throws ImplerException if {@link JarOutputStream} processing throws an {@link IOException}
     */
    private void makeJar(final Path jarFile, final Path tempDirectory, final Class<?> token) throws ImplerException {
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        try (final JarOutputStream jar = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            final String name = token.getPackageName().replace('.', '/') + "/" + makeName(token) + ".class";
            jar.putNextEntry(new ZipEntry(name));
            Files.copy(Paths.get(tempDirectory.toString(), name), jar);
        } catch (final IOException e) {
            throw new ImplerException(e.getMessage());
        }
    }

    /**
     * Creates a {@code .jar} file that contains sources of class
     * implemented by {@link #implement(Class, Path)}.
     * Uses temporary directory and deletes it after implementation using {@link ImplementorDirectoryManager}
     * and {@link DirectoryCleaner}.
     *
     * @param token   type token to create implementation for
     * @param jarFile target location of {@code .jar} file
     * @throws ImplerException if any error occurs during the implementation
     */
    @Override
    public void implementJar(final Class<?> token, final Path jarFile) throws ImplerException {
        if (token == null || jarFile == null) {
            throw new ImplerException("Not null arguments expected");
        }
        ImplementorDirectoryManager.createDirectoriesOnPath(jarFile.normalize());
        final ImplementorDirectoryManager directoryManager = new ImplementorDirectoryManager(jarFile.toAbsolutePath().getParent());
        try {
            implement(token, directoryManager.getDirectory());
            compileClass(token, directoryManager.getDirectory());
            makeJar(jarFile, directoryManager.getDirectory(), token);
        } finally {
            directoryManager.cleanDirectory();
        }
    }
}