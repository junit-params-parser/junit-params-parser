package com.github.junit.params.parser.common;

import org.jetbrains.annotations.Contract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.github.junit.params.parser.common.Resources.ResourceStorage.namedStorage;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public interface Resources {

    ResourceStorage CLASSLOADER_STORAGE = namedStorage(Resources.class.getClassLoader());
    ResourceStorage CLASS_STORAGE = namedStorage(Resources.class);

    /**
     * Gets the contents of a classpath resource as a String using the {@link java.nio.charset.StandardCharsets#UTF_8 UTF-8} character encoding.
     * <p>
     * It is expected the given {@code name} to be absolute, but it can start from {@code /}.
     *
     * @param name name of the desired resource
     * @return content of requested resource
     * @throws IllegalStateException if an I/O error occurs
     */
    @Nonnull
    static String loadResource(@Nonnull String name) {
        return loadResource(name, null);
    }

    /**
     * Gets the contents of a classpath resource as a String using the specified character encoding.
     * <p>
     * It is expected the given {@code name} to be absolute, but it can start from {@code /}.
     *
     * @param name     name of the desired resource
     * @param encoding charset to use, null means {@link java.nio.charset.StandardCharsets#UTF_8 UTF-8}
     * @return content of requested resource
     * @throws IllegalStateException if an I/O error occurs
     */
    @Nonnull
    static String loadResource(@Nonnull String name, @Nullable Charset encoding) {
        return loadResource((ClassLoader) null, name, encoding);
    }

    @Nonnull
    static String loadResource(@Nullable ClassLoader storage, @Nonnull String name) {
        return loadResource(storage, name, null);
    }

    @Nonnull
    static String loadResource(@Nullable ClassLoader storage, @Nonnull String name, @Nullable Charset encoding) {
        return loadResource(namedStorage(storage), name, encoding);
    }

    @Nonnull
    static String loadResource(@Nullable Class<?> storage, @Nonnull String name) {
        return loadResource(storage, name, null);
    }

    @Nonnull
    static String loadResource(@Nullable Class<?> storage, @Nonnull String name, @Nullable Charset encoding) {
        return loadResource(namedStorage(storage), name, encoding);
    }

    @Nonnull
    static String loadResource(@Nullable ResourceStorage storage, @Nonnull String name, @Nullable Charset encoding) {
        requireNonNull(name, "name");
        Stream<ResourceStorage> storages = Stream.of(storage, CLASSLOADER_STORAGE, CLASS_STORAGE).filter(Objects::nonNull);
        Supplier<Stream<String>> names = () -> Stream.of(name, "/" + name).distinct();
        BinaryOperator<Exception> combiner = (next, prev) -> {
            if (prev != null) {
                prev.addSuppressed(next);
                return prev;
            }
            return next;
        };
        Map.Entry<Exception, InputStream> found = storages
            .flatMap(s -> names.get().map(n -> tuple(tuple(s, n), s.apply(n))))
            .reduce(
                tuple(null, null),
                (result, resource) -> {
                    if (result.getValue() != null) {
                        //Already found resource - return it
                        return result;
                    } else if (resource.getValue() == null) {
                        //Storage don't contain resource for such name - throw
                        return left(combiner.apply(
                            new IOException("Resource '" + resource.getKey().getValue() + "' not found from '" + resource.getKey().getKey() + "'"),
                            result.getKey()
                        ));
                    } else {
                        //Storage contain resource for such name - trying to open it
                        try {
                            return right(resource.getValue().openStream());
                        } catch (IOException e) {
                            //Handle some exception on opening
                            return left(combiner.apply(e, result.getKey()));
                        }
                    }
                },
                Resources::shouldNotBeCalled
            );
        if (found.getValue() != null) {
            //Read from first found stream
            try (
                InputStream stream = found.getValue();
                Reader reader = new InputStreamReader(stream, fallbackToUtf8(encoding));
                BufferedReader buffer = new BufferedReader(reader)
            ) {
                return buffer.lines().collect(joining(lineSeparator()));
            } catch (IOException e) {
                found = left(e);
            }
        }
        throw new IllegalStateException("Invalid resource", found.getKey());
    }

    @Nonnull
    static Charset fallbackToUtf8(@Nullable Charset encoding) {
        return encoding != null ? encoding : UTF_8;
    }

    @Contract("_, _ -> fail")
    static <T> T shouldNotBeCalled(T lhs, T rhs) {
        throw new IllegalStateException("Should not be called");
    }

    @Nonnull
    static <L, R> Map.Entry<L, R> left(@Nullable L value) {
        return tuple(value, null);
    }

    @Nonnull
    static <L, R> Map.Entry<L, R> right(@Nullable R value) {
        return tuple(null, value);
    }

    @Nonnull
    static <K, V> Map.Entry<K, V> tuple(@Nullable K key, @Nullable V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    @FunctionalInterface
    interface ResourceStorage extends Function<String, URL> {

        @Contract("null -> null; !null -> !null")
        static ResourceStorage namedStorage(@Nullable Class<?> source) {
            return source != null ? namedStorage(source.getName(), source::getResource) : null;
        }

        @Contract("null -> null; !null -> !null")
        static ResourceStorage namedStorage(@Nullable ClassLoader source) {
            return source != null ? namedStorage(source.getClass().getName(), source::getResource) : null;
        }

        @Nonnull
        static ResourceStorage namedStorage(@Nonnull String name, @Nonnull ResourceStorage impl) {
            return new ResourceStorage() {
                @Override
                public URL apply(String s) {
                    return impl.apply(s);
                }

                @Override
                public String toString() {
                    return name;
                }
            };
        }

    }

}
