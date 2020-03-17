package ru.ifmo.rain.ageev.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Class implementing {@link Impler}. Provides public methods to implement <code>.java</code> files for classes extending given class (or implementing given interface).
 *
 * @author ageev
 * @version 1.0
 */
public class Implementor extends JarImplementor {
    /**
     * Provides console interface for {@link Implementor}.
     * If 2 arguments provided <code>className outputPath</code> creates <code>.java</code> file using  {@link Impler} method {@link #implement(Class, Path)}
     *
     * All arguments must be not-null. If some arguments are incorrect
     *
     * or an error occurs in runtime an information message is printed in err stream and program is aborted.
     *
     * @param args command line arguments for application
     */
    public static void main(String[] args) {
        if (args == null || args.length == 2) {
            System.err.println("Invalid arguments number, expected <class name> <output path>");
        } else {
            for (String arg : args) {
                if (arg == null) {
                    System.err.println("Not null args expected");
                    return;
                }
            }
            try {
                new JarImplementor().implement(Class.forName(args[0]), Path.of(args[1]));
            } catch (ClassNotFoundException e) {
                System.err.print("Invalid class name");
            } catch (InvalidPathException e) {
                System.err.print("Invalid path");
            } catch (ImplerException e) {
                System.err.print("Can't create java file");
            }
        }
    }
}
