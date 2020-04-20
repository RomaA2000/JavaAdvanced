package ru.ifmo.rain.ageev.walk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RecursiveWalk {
    public static void main(final String[] args) {
        try {
            if (args == null || args.length != 2 || args[0] == null || args[1] == null) {
                throw new RecursiveWalkException("Expected 2 arguments");
            }
            startRecursiveWalk(args[0], args[1]);
        } catch (RecursiveWalkException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void createParentDirectories(final Path outputFilePath) throws RecursiveWalkException {
        Path parent = outputFilePath.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new RecursiveWalkException("Program can't create folder for output file path: " + e.getMessage(), e);
            }
        }
    }

    private static Path setUpFilePath(final String filePath, final String name) throws RecursiveWalkException {
        try {
            return Paths.get(filePath);
        } catch (InvalidPathException e) {
            throw new RecursiveWalkException("Invalid " + name + " file path: " + e.getMessage(), e);
        }
    }

    public static void startRecursiveWalk(final String inputFile, final String outputFile) throws RecursiveWalkException {
        Path inputFilePath = setUpFilePath(inputFile, "input");
        Path outputFilePath = setUpFilePath(outputFile, "output");
        createParentDirectories(outputFilePath);
        try (BufferedReader bufferedReader = Files.newBufferedReader(inputFilePath)) {
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(outputFilePath)) {
                FileHasher fileHasher = new FileHasher(bufferedWriter);
                while (true) {
                    String nowPath;
                    try {
                        nowPath = bufferedReader.readLine();
                    } catch (IOException e) {
                        throw new RecursiveWalkException("Error while reading next line from input file: " + e.getMessage(), e);
                    }
                    if (nowPath == null) {
                        break;
                    }
                    try {
                        Files.walkFileTree(Paths.get(nowPath), fileHasher);
                    } catch (InvalidPathException e) {
                        fileHasher.writeToFile(0, nowPath);
                    }
                }
            } catch (FileNotFoundException e) {
                throw new RecursiveWalkException("Output file not found: " + e.getMessage(), e);
            } catch (SecurityException e) {
                throw new RecursiveWalkException("Security error while processing output file: " + e.getMessage(), e);
            } catch (IOException e) {
                throw new RecursiveWalkException("IOError while processing output file: " + e.getMessage(), e);
            }
        } catch (FileNotFoundException e) {
            throw new RecursiveWalkException("Input file not found: " + e.getMessage(), e);
        } catch (SecurityException e) {
            throw new RecursiveWalkException("Security error while opening input file: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RecursiveWalkException("IOError while processing input file: " + e.getMessage(), e);
        }
    }
}
