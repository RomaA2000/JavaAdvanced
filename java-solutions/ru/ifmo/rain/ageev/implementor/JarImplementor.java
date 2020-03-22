package ru.ifmo.rain.ageev.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;


/**
 * Class implementing {@link JarImpler}. Provides public methods to implement <code>.java</code>
 * and <code>.jar</code> files for classes extending given class (or implementing given interface).
 *
 * @author ageev
 * @version 1.0
 */
public class JarImplementor extends Implementor implements JarImpler {
    /**
     * Default constructor. Creates new instance of {@link JarImplementor}.
     */
    public JarImplementor() {
    }

    /**
     * Provides console interface for {@link JarImplementor}.
     * If 3 arguments provided <code>-jar className jarOutputPath</code> creates <code>.jar</code> file using {@link JarImpler} method {@link #implementJar(Class, Path)}
     * All arguments must be not-null. If some arguments are incorrect
     * or an error occurs in runtime an information message is printed in err stream and program is aborted.
     *
     * @param args command line arguments for application
     */
    public static void main(String[] args) {
        if (args == null || args.length == 3) {
            System.err.println("Invalid arguments number, expected -jar <class name> <output path>");
        } else {
            for (String arg : args) {
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
            } catch (ClassNotFoundException e) {
                System.err.print("Invalid class name");
            } catch (InvalidPathException e) {
                System.err.print("Invalid path");
            } catch (ImplerException e) {
                System.err.print("Can't create java file");
            }
        }
    }


    /**
     * Compiles implemented class extending or implementing {@code token}
     * and stores <code>.class</code> file in given {@code tempDirectory}.
     * <p>
     * Uses <code>-classpath</code> pointing to location of class or interface specified by {@code token}.
     *
     * @param token         type token that need to be implemented
     * @param tempDirectory directory to store <code>.class</code> files
     * @throws ImplerException if an error occurs
     */
    private void compileClass(Class<?> token, Path tempDirectory) throws ImplerException {
        Path superPath;
        try {
            CodeSource codeSource = token.getProtectionDomain().getCodeSource();
            if (codeSource == null) {
                throw new ImplerException("Failed to retrieve super class source code");
            }
            URL sourceCodeUrl = codeSource.getLocation();
            if (sourceCodeUrl == null) {
                throw new ImplerException("Failed to retrieve super class code source location");
            }
            String sourceCodePath = sourceCodeUrl.getPath();
            if (sourceCodePath.isEmpty()) {
                throw new ImplerException("Failed to convert source code location");
            }
            if (sourceCodePath.startsWith("/")) {
                sourceCodePath = sourceCodePath.substring(1);
            }
            superPath = Path.of(sourceCodePath);
        } catch (InvalidPathException e) {
            throw new ImplerException("Failed to retrieve super class source code");
        }

        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        if (javaCompiler == null) {
            throw new ImplerException("No Java compiler provided");
        }

        String[] compilerArgs = {
                "-cp",
                tempDirectory.toString() + File.pathSeparator + superPath.toString(),
                tempDirectory.resolve(ImplementorDirectoryManager.getImplementationPath(token, File.separator) + IMPL_SUFFIX + JAVA_EXTENSION).toString(),
        };

        int returnCode = javaCompiler.run(null, null, null, compilerArgs);
        if (returnCode != 0) {
            throw new ImplerException("Implementation compilation returned non-zero code " + returnCode);
        }
    }

    /**
     * Makes a <code>.jar</code> file containing sources of implemented class using {@link Manifest}.
     *
     * @param jarFile       path where  <code>.jar</code> file will be saved
     * @param tempDirectory temporary directory where all <code>.class</code> files are stored
     * @param token         type token that needs to be implemented
     * @throws ImplerException if {@link JarOutputStream} processing throws an {@link IOException}
     */
    private void makeJar(Path jarFile, Path tempDirectory, Class<?> token) throws ImplerException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        try (JarOutputStream stream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
            String implementationPath = ImplementorDirectoryManager.getImplementationPath(token, "/") + IMPL_SUFFIX + CLASS_EXTENSION;
            stream.putNextEntry(new ZipEntry(implementationPath));
            Files.copy(Path.of(tempDirectory.toString(), implementationPath), stream);
        } catch (IOException e) {
            throw new ImplerException("Failed to write JAR", e);
        }

    }

    /**
     * Creates a <code>.jar</code> file that contains sources of class
     * implemented by {@link #implement(Class, Path)}.
     * Uses temporary directory and deletes it after implementation using {@link ImplementorDirectoryManager}
     * and {@link DirectoryCleaner}.
     *
     * @param token   type token to create implementation for
     * @param jarFile target location of <code>.jar</code> file
     * @throws ImplerException if any error occurs during the implementation
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        if (token == null || jarFile == null) {
            throw new ImplerException("Not null arguments expected");
        }
        ImplementorDirectoryManager.createDirectoriesOnPath(jarFile.normalize());
        ImplementorDirectoryManager directoryManager = new ImplementorDirectoryManager(jarFile.toAbsolutePath().getParent());
        try {
            implement(token, directoryManager.getDirectory());
            compileClass(token, directoryManager.getDirectory());
            makeJar(jarFile, directoryManager.getDirectory(), token);
        } finally {
            directoryManager.cleanDirectory();
        }
    }
}