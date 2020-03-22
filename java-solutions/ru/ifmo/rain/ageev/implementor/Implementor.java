package ru.ifmo.rain.ageev.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;

/**
 * Class implementing {@link Impler}. Provides public methods to implement <code>.java</code> files for classes extending given class (or implementing given interface).
 *
 * @author ageev
 * @version 1.0
 */
public class Implementor implements Impler {
    /**
     * Suffix for generated <code>.java</code> class name.
     */
    protected static final String IMPL_SUFFIX = "Impl";
    /**
     * Java extension for generated <code>.java</code> files.
     */
    protected static final String JAVA_EXTENSION = ".java";
    /**
     * Class extension for generated <code>.java</code> files.
     */
    protected static final String CLASS_EXTENSION = ".class";
    /**
     * Line separator for generated <code>.java</code> files.
     */
    protected static final String LINE_SEPARATOR = System.lineSeparator();

    /**
     * Empty string for generated <code>.java</code> files.
     */
    protected static final String EMPTY_STRING = "";

    /**
     * Space for generated <code>.java</code> files.
     */
    protected static final String SPACE = " ";

    /**
     * Definition opening string for generated <code>.java</code> files.
     */
    protected static final String BRACE_OPEN = "{";

    /**
     * Definition closing string for generated <code>.java</code> files.
     */
    protected static final String BRACE_CLOSE = "}";

    /**
     * Argument list opening string for generated <code>.java</code> files.
     */
    protected static final String BRACKET_OPEN = "(";

    /**
     * Argument list closing string for generated <code>.java</code> files.
     */
    protected static final String BRACKET_CLOSE = ")";

    /**
     * Argument separator for generated <code>.java</code> files.
     */
    protected static final String ARG_SEPARATOR = ", ";

    /**
     * End-of-line string for generated <code>.java</code> files.
     */
    protected static final String LINE_END = ";";

    /**
     * Default constructor. Creates new instance of {@link JarImplementor}.
     */
    public Implementor() {
    }

    /**
     * Static function that makes path {@link String} for token of {@link Class}
     *
     * @param token type token to make  {@link Path}
     * @return Path {@link String}
     */
    static private String getPath(Class<?> token) {
        return token.getPackageName().replace('.', File.separatorChar);
    }

    /**
     * Static function that makes name {@link String} for token of {@link Class}
     *
     * @param token type token to make name
     * @return Name {@link String}
     */
    static private String getName(Class<?> token) {
        return token.getSimpleName() + IMPL_SUFFIX;
    }

    /**
     * Unicode coder for resulting <code>.java</code> file.
     * Writes all unicode characters in <code>\\u</code> notation.
     *
     * @param str a {@link String} string to be encoded
     * @return a {@link String} of unicode {@code arg}
     */
    private static String encode(String str) {
        StringBuilder builder = new StringBuilder();
        for (char c : str.toCharArray()) {
            builder.append(c < 128 ? String.valueOf(c) : String.format("\\u%04x", (int) c));
        }
        return builder.toString();
    }

    /**
     * Provides console interface for {@link Implementor}.
     * If 2 arguments provided <code>className outputPath</code> creates <code>.java</code> file using  {@link Impler} method {@link #implement(Class, Path)}
     * All arguments must be not-null. If some arguments are incorrect
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
                new Implementor().implement(Class.forName(args[0]), Path.of(args[1]));
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
     * Creates a <code>.java</code> file containing code of a class extending given class (or implementing given interface).
     *
     * @param token type token to create implementation for
     * @param root  root directory.
     * @throws ImplerException if any error occurs during code generation
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if ((token == null) || (root == null)) {
            throw new ImplerException("Null argument(s) given");
        }
        Path toGo;
        try {
            toGo = Path.of(root.toString(), getPath(token), getName(token) + JAVA_EXTENSION);
        } catch (InvalidPathException e) {
            throw new ImplerException("Invalid path", e);
        }
        ImplementorDirectoryManager.createDirectoriesOnPath(toGo);
        try (BufferedWriter writer = Files.newBufferedWriter(toGo)) {
            if (token.isPrimitive() || token.isArray() || Modifier.isPrivate(token.getModifiers()) || Modifier.isFinal(token.getModifiers()) || token == Enum.class) {
                throw new ImplerException("Unsupported class");
            }
            try {
                writer.write(encode(getClass(token)));
            } catch (IOException e) {
                throw new ImplerException("Error writing in file", e);
            }
        } catch (IOException e) {
            throw new ImplerException("Error opening file", e);
        }
    }

    /**
     * Creates string of implementation for token.
     *
     * @param token type token to create implementation for
     * @return {@link String} of class code for {@code token}
     * @throws ImplerException if any error occurs during code generation
     */
    private String getClass(Class<?> token) throws ImplerException {
        return makeBlockByLineSeparator(makePackage(token), makeClass(token));
    }

    /**
     * Creates {@link String} from {@link String}s array with {@code separator}.
     *
     * @param separator string separator
     * @param strings   an array of strings to be written dividing by a separator
     * @return {@link String} of concatenated with {@code separator} {@code strings}
     */
    private String makeBlock(final String separator, String... strings) {
        return pack(separator, strings, Function.identity());
    }

    /**
     * Creates {@link String} from {@code strings} separated with {@link #SPACE}.
     *
     * @param strings an array of {@link String}s to be written dividing by a {@link #SPACE}
     * @return {@link String} of concatenated with {@code separator} {@link #SPACE}
     */
    private String makeBlockBySpace(String... strings) {
        return makeBlock(SPACE, strings);
    }

    /**
     * Creates {@link String} from strings array separated by {@link #LINE_SEPARATOR}.
     *
     * @param strings an array of {@link String}s to be written
     * @return {@link String} of concatenated with {@code separator} {@link #LINE_SEPARATOR}
     */
    private String makeBlockByLineSeparator(String... strings) {
        return makeBlock(LINE_SEPARATOR, strings);
    }

    /**
     * Custom two {@link String}s joiner. Returns their combination using {@link #makeBlockBySpace(String...)}
     * if {@code item} is not empty and an empty string otherwise.
     *
     * @param name        a prefix for concatenation
     * @param needToCheck a string
     * @return an empty {@link String} if the {@code needToCheck} is empty
     * and concatenation of {@code name} and {@code needToCheck} with {@link #makeBlockBySpace}
     */
    private String checkNotEmpty(final String name, final String needToCheck) {
        return needToCheck.isEmpty() ? EMPTY_STRING : makeBlockBySpace(name, needToCheck);
    }

    /**
     * Full method builder. Makes new class method code using {@link #makeBlockBySpace(String...)}
     * to combine together {@link #makeExecutableModifiers(Executable)}
     * in {@link #makeMethodConstructorSamePart(Executable, String, String)}.
     *
     * @param method an instance of {@link Method}
     * @return a {@link String} code of method declaration and body
     * @see #makeMethodConstructorSamePart(Executable, String, String)
     */
    private String makeMethod(Method method) {
        return makeMethodConstructorSamePart(method, makeBlockBySpace(method.getReturnType().getCanonicalName(),
                method.getName() + makeArguments(method)), makeMethodBody(method));
    }

    /**
     * Same part of method and constructor makers.
     *
     * @param executable an instance of {@link Method} or {@link Constructor} for which it makes code
     * @param signature  a signature of building code
     * @param body       a body of building code
     * @return a {@link String} code of method or constructor declaration and body
     */
    private String makeMethodConstructorSamePart(Executable executable, String signature, String body) {
        return makeBlockBySpace(
                makeExecutableModifiers(executable),
                signature,
                makeExceptions(executable),
                makeBlockByLineSeparator(
                        BRACE_OPEN,
                        body,
                        BRACE_CLOSE
                )
        );
    }

    /**
     * Makes code of class from {@code token}. Modifiers, class name, superclass,
     * constructors (if token is {@link Class}) and methods
     * using {@link #makeBlockByLineSeparator(String...)}.
     *
     * @param token instance of given class {@link Class} object
     * @return a {@link String} containing new class code
     * @throws ImplerException if error occurred while generating code
     */
    private String makeClass(Class<?> token) throws ImplerException {
        return makeBlockByLineSeparator(
                makeClassDef(token),
                BRACE_OPEN,
                makeConstructors(token),
                makeAbstractMethods(token),
                BRACE_CLOSE);
    }

    /**
     * Makes a code of new class full declaration line containing class name and superclass
     * using {@link #makeBlockBySpace(String...)}.
     *
     * @param token instance of given class {@link Class} object
     * @return a {@link String} containing new class declaration
     */
    private String makeClassDef(Class<?> token) {
        return makeBlockBySpace(
                "public class",
                makeFullName(token));
    }

    /**
     * Makes a class name of {@code token} with Impl suffix.
     *
     * @param token instance of given class {@link Class} object
     * @return a {@link String} containing new class name
     */
    private String makeName(Class<?> token) {
        return token.getSimpleName() + IMPL_SUFFIX;
    }

    /**
     * Makes string from class name made by {@link #makeName} and <code>extends</code> or <code>implements</code> declaration of new class
     * depending on given base class {@code token} using {@link #makeBlockBySpace(String...)}.
     *
     * @param token instance of given class {@link Class} object
     * @return a {@link String} containing class name with superclass declaration
     */
    private String makeFullName(Class<?> token) {
        return makeBlockBySpace(makeName(token), token.isInterface() ? "implements" : "extends", token.getCanonicalName());
    }

    /**
     * Makes a code for {@link Class}, {@link Method} or {@link Constructor} common modifiers. Without
     * {@link Modifier#ABSTRACT}.
     *
     * @param modifiers integer modifiers mask
     * @return a {@link String} representing all modifiers but not abstract
     */
    private String makeModifiers(int modifiers) {
        return Modifier.toString(modifiers & ~Modifier.ABSTRACT);
    }

    /**
     * Makes a code for {@link Class} constructors or {@link #EMPTY_STRING} if {@code token} is interface.
     *
     * @param token instance of given class {@link Class} object
     * @return a {@link String} representing all non private constructors
     * @throws ImplerException if there is no non-private constructors
     */
    private String makeConstructors(Class<?> token) throws ImplerException {
        if (token.isInterface()) {
            return EMPTY_STRING;
        }
        Constructor<?>[] constructors = token.getDeclaredConstructors();
        List<Constructor<?>> constructorsList =
                Arrays.stream(constructors)
                        .filter(p -> !Modifier.isPrivate(p.getModifiers()))
                        .collect(Collectors.toList());
        if (constructorsList.isEmpty()) {
            throw new ImplerException("Class with only private constructors can't be extended");
        }
        return constructorsList.stream()
                .map(this::constructorMaker)
                .collect(Collectors.joining(LINE_SEPARATOR));
    }

    /**
     * Makes a code of given {@link Method} or {@link Constructor} modifiers without
     * {@link Modifier#NATIVE}, {@link Modifier#ABSTRACT} and {@link Modifier#TRANSIENT}.
     *
     * @param executable an instance of {@link Executable} which is a method or a constructor
     * @return a {@link String} representing all modifiers of given {@code executable} if they are
     * not abstract, native or transient
     */
    private String makeExecutableModifiers(Executable executable) {
        return makeModifiers(executable.getModifiers() & ~Modifier.NATIVE & ~Modifier.TRANSIENT);
    }

    /**
     * Returns a package declaration for implemented <code>.java</code> class file if it's needed.
     *
     * @param token instance of given class {@link Class} object
     * @return a {@link String} containing new class package declaration or {@link #EMPTY_STRING}
     */
    private String makePackage(Class<?> token) {
        String packageName = token.getPackageName();
        return packageName.isEmpty() ? EMPTY_STRING : makeBlockBySpace("package", packageName) + LINE_END;
    }

    /**
     * Full constructor builder. Makes new class constructor code using {@link #makeBlockBySpace(String...)}
     * to combine together {@link #makeExecutableModifiers(Executable)}
     * in {@link #makeMethodConstructorSamePart(Executable, String, String)}.
     *
     * @param constructor an instance of {@link Constructor}
     * @return a {@link String} code of constructor declaration and body
     * @see #makeMethodConstructorSamePart(Executable, String, String)
     */
    private String constructorMaker(Constructor<?> constructor) {
        return makeMethodConstructorSamePart(constructor,
                makeName(constructor.getDeclaringClass()) + makeArguments(constructor),
                makeConstructorBody(constructor));
    }

    /**
     * Makes a code of super constructor call in new class constructor body.
     *
     * @param constructor an instance of {@link Constructor}
     * @return a {@link String} code of new class constructor body
     */
    private String makeConstructorBody(Constructor<?> constructor) {
        return "super" + makeArgumentsNames(constructor) + LINE_END;
    }

    /**
     * Makes a code of {@link Executable} exceptions {@link String} using {@link #packItems(Object[], Function)}.
     *
     * @param executable an instance of {@link Executable}
     * @return a {@link String} representing all throws from this {@code executable}
     */
    private String makeExceptions(Executable executable) {
        return checkNotEmpty("throws", packItems(executable.getExceptionTypes(), Class::getCanonicalName));
    }

    /**
     * Elements packer. Maps given elements with given transform to {@link String}s
     * and concatenates them with given separator.
     *
     * @param separator delimiter separating given values
     * @param elements  array of values to be concatenated
     * @param transform transforming to {@link String} function
     * @param <T>       type of given elements
     * @return a {@link String} containing all transformed {@code elements} separated by {@code separator}
     */
    private <T> String pack(String separator, T[] elements, Function<T, String> transform) {
        String[] strings = new String[elements.length];
        IntStream.range(0, elements.length).forEachOrdered(i ->
                strings[i] = transform.apply(elements[i])
        );
        return String.join(separator, strings);
    }

    /**
     * Elements packer. Packs them with function {@link #pack} and  {@link #ARG_SEPARATOR} .
     *
     * @param items     array of values to be concatenated
     * @param transform transforming to {@link String} function
     * @param <T>       type of given elements
     * @return a {@link String} containing all transformed {@code elements} separated by {@link #ARG_SEPARATOR}
     */
    private <T> String packItems(T[] items, Function<T, String> transform) {
        return pack(ARG_SEPARATOR, items, transform);
    }

    /**
     * Makes a code of {@link Executable} arguments with types and names
     * using {@link #SPACE} and {@link #packItems(Object[], Function)}.
     * <p>
     * Argument names are generated by {@link ArgumentNumberMaker}.
     *
     * @param executable an instance of {@link Executable} which is a method or a constructor
     * @return a {@link String} representing this {@code executable} arguments
     */
    private String makeArgumentsNames(Executable executable) {
        return makeArgumentsNamesByFunc(executable, t -> SPACE);
    }

    /**
     * Makes a code of {@link Executable} arguments with types and names
     * using transform and {@link #packItems(Object[], Function)}.
     * <p>
     * Argument names are generated by {@link ArgumentNumberMaker}.
     *
     * @param transform  transformation which used in {@link #packItems(Object[], Function)}
     * @param executable an instance of {@link Executable} which is a method or a constructor
     * @return a {@link String} representing this {@code executable} arguments
     */
    private String makeArgumentsNamesByFunc(Executable executable, Function<Class<?>, String> transform) {
        ArgumentNumberMaker number = new ArgumentNumberMaker();
        return BRACKET_OPEN +
                packItems(executable.getParameterTypes(), t -> makeBlockBySpace(transform.apply(t), "variable" + number.get()))
                + BRACKET_CLOSE;
    }

    /**
     * Makes a code of {@link Executable} arguments with types and names
     * using {@link #packItems(Object[], Function)}.
     * <p>
     * Argument names are generated by {@link ArgumentNumberMaker}.
     *
     * @param executable an instance of {@link Executable} which is a method or a constructor
     * @return a {@link String} representing this {@code executable} arguments
     */
    private String makeArguments(Executable executable) {
        return makeArgumentsNamesByFunc(executable, Class::getCanonicalName);
    }

    /**
     * Abstract methods maker.
     * Makes class of abstract method representations returned by {@link #makeMethod(Method)}.
     * Collects all superclasses' methods using {@link #makeAbstractMethodsOneClass(Method[], HashSet)} and then
     * filters them by {@link Modifier#isAbstract(int)}.
     *
     * @param token type token to create implementation for
     * @return a {@link String} code of all superclasses' abstract methods separated by {@link #LINE_SEPARATOR}
     */
    private String makeAbstractMethods(Class<?> token) {
        HashSet<MethodHasher> methods = new HashSet<>();
        makeAbstractMethodsOneClass(token.getMethods(), methods);
        for (; token != null; token = token.getSuperclass()) {
            makeAbstractMethodsOneClass(token.getDeclaredMethods(), methods);
        }
        return methods.stream().filter(m -> Modifier.isAbstract(m.get().getModifiers())).map(m -> makeMethod(m.get()))
                .collect(Collectors.joining(LINE_SEPARATOR));
    }

    /**
     * Methods collector. Collects all methods from given {@code methods} array
     * to {@code collector} wrapping them in {@link MethodHasher}.
     *
     * @param methods       an array of {@link Method}s
     * @param collectorNeed a {@link HashSet} of {@link MethodHasher}
     */
    private void makeAbstractMethodsOneClass(Method[] methods, HashSet<MethodHasher> collectorNeed) {
        Arrays.stream(methods)
                .map(MethodHasher::new)
                .collect(Collectors.toCollection(() -> collectorNeed));

    }

    /**
     * Makes a code of simple default return value in new class method body.
     *
     * @param method an instance of {@link Method}
     * @return a {@link String} representation of new class method body
     */
    private String makeMethodBody(Method method) {
        return makeBlockBySpace("return", makeValue(method.getReturnType()), LINE_END);
    }

    /**
     * Makes {@link String} of default return value for method with given {@link Method#getReturnType()}.
     *
     * @param token some method return value type
     * @return a {@link String} default return value of this type
     */
    private String makeValue(Class<?> token) {
        if (!token.isPrimitive()) {
            return "null";
        } else if (token.equals(boolean.class)) {
            return "false";
        } else if (token.equals(void.class)) {
            return "";
        } else {
            return "0";
        }
    }
}