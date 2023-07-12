package com.github.junit.params.parser.common;

import com.github.junit.params.parser.common.Resources.ResourceStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.junit.params.parser.common.Resources.ResourceStorage.namedStorage;
import static com.github.junit.params.parser.common.Resources.loadResource;
import static com.github.junit.params.parser.common.Resources.shouldNotBeCalled;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.HamcrestCondition.matching;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;

class ResourcesTest {

    @ParameterizedTest(name = "[{index}] Should successful load `{0}`")
    @CsvSource({
        "name.txt,Расположен в корне ресурсов",
        "/name.txt,Расположен в корне ресурсов",
    })
    void success_default(@Nonnull String resource, @Nonnull String expected) {
        assertThat(loadResource(resource))
            .isEqualTo(expected);
    }

    @ParameterizedTest(name = "[{index}] Should successful load `{0}`")
    @CsvSource({
        "name.txt,Расположен в корне ресурсов",
        "/name.txt,Расположен в корне ресурсов",
    })
    void successFromClassLoader(@Nonnull String resource, @Nonnull String expected) {
        assertThat(loadResource(getClass().getClassLoader(), resource))
            .isEqualTo(expected);
    }

    @ParameterizedTest(name = "[{index}] Should successful load `{1}` from `{0}` with `{2}`-encoding")
    @CsvSource({
        ",name.txt,,Расположен в корне ресурсов",
        ",/name.txt,,Расположен в корне ресурсов",
        ",name.txt,UTF-8,Расположен в корне ресурсов",
        ",/name.txt,UTF-8,Расположен в корне ресурсов",
        "com.github.junit.params.parser.common.ResourcesTest,name.txt,UTF-8,Расположен в пакете рядом с классом",
        "com.github.junit.params.parser.common.ResourcesTest,/name.txt,UTF-8,Расположен в корне ресурсов",
    })
    void successFromClass(@Nullable Class<?> storageSource, @Nonnull String resource, @Nullable Charset encoding, @Nonnull String expected) {
        assertThat(loadResource(storageSource, resource, encoding))
            .isEqualTo(expected);
    }

    @ParameterizedTest(name = "[{index}] Should fail to load `{1}` from `{0}`")
    @CsvSource({
        " " +
            ",unknown.txt" +
            ",Resource 'unknown.txt' not found from 'sun.misc.Launcher$AppClassLoader'" +
            ",Resource '/unknown.txt' not found from 'sun.misc.Launcher$AppClassLoader'" +
            "|Resource 'unknown.txt' not found from 'com.github.junit.params.parser.common.Resources'" +
            "|Resource '/unknown.txt' not found from 'com.github.junit.params.parser.common.Resources'",
        " " +
            ",/unknown.txt" +
            ",Resource '/unknown.txt' not found from 'sun.misc.Launcher$AppClassLoader'" +
            ",Resource '//unknown.txt' not found from 'sun.misc.Launcher$AppClassLoader'" +
            "|Resource '/unknown.txt' not found from 'com.github.junit.params.parser.common.Resources'" +
            "|Resource '//unknown.txt' not found from 'com.github.junit.params.parser.common.Resources'",
        "com.github.junit.params.parser.common.ResourcesTest" +
            ",unknown.txt" +
            ",Resource 'unknown.txt' not found from 'com.github.junit.params.parser.common.ResourcesTest'" +
            ",Resource '/unknown.txt' not found from 'com.github.junit.params.parser.common.ResourcesTest'" +
            "|Resource 'unknown.txt' not found from 'sun.misc.Launcher$AppClassLoader'" +
            "|Resource '/unknown.txt' not found from 'sun.misc.Launcher$AppClassLoader'" +
            "|Resource 'unknown.txt' not found from 'com.github.junit.params.parser.common.Resources'" +
            "|Resource '/unknown.txt' not found from 'com.github.junit.params.parser.common.Resources'",
        "com.github.junit.params.parser.common.ResourcesTest" +
            ",/unknown.txt" +
            ",Resource '/unknown.txt' not found from 'com.github.junit.params.parser.common.ResourcesTest'" +
            ",Resource '//unknown.txt' not found from 'com.github.junit.params.parser.common.ResourcesTest'" +
            "|Resource '/unknown.txt' not found from 'sun.misc.Launcher$AppClassLoader'" +
            "|Resource '//unknown.txt' not found from 'sun.misc.Launcher$AppClassLoader'" +
            "|Resource '/unknown.txt' not found from 'com.github.junit.params.parser.common.Resources'" +
            "|Resource '//unknown.txt' not found from 'com.github.junit.params.parser.common.Resources'",
    })
    void failureFromClass(@Nullable Class<?> storageSource, @Nonnull String resource, @Nonnull String message, @Nullable String suppressedJoined) {
        String[] suppressed = suppressedJoined != null ? suppressedJoined.split("\\|") : new String[]{};
        assertThatThrownBy(() -> loadResource(storageSource, resource))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Invalid resource")
            .cause().hasNoCause()
            .hasMessage(message)
            .satisfies(cause -> assertThat(cause.getSuppressed())
                .allMatch(IOException.class::isInstance)
                .extracting(Throwable::getMessage)
                .containsExactly(suppressed)
            );
    }

    @Test
    void failureWithIoExceptionOnSearch() throws IOException {
        String name = "unknown.txt";
        Path path = Paths.get(name).toAbsolutePath().normalize();
        URL url = path.toUri().toURL();

        ResourceStorage storage = namedStorage("demo", any -> url);

        assertThatThrownBy(() -> loadResource(storage, name, null))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Invalid resource")
            .cause().hasNoCause()
            .isExactlyInstanceOf(FileNotFoundException.class)
            .hasMessageContaining(path.toString())
            .satisfies(cause -> assertThat(cause.getSuppressed())
                .allMatch(IOException.class::isInstance)
                .haveExactly(1, matching(instanceOf(FileNotFoundException.class)))

                .extracting(Throwable::getMessage)
                .allMatch(it -> it.contains(name))
                .haveExactly(1, matching(containsString(path.toString())))
            );
    }

    @ParameterizedTest
    @CsvSource({
        ",",
        "x,",
        ",x",
        "x,x",
        "x,y",
        "y,x",
    })
    void testShouldNotBeCalled(String lhs, String rhs) {
        assertThatThrownBy(() -> shouldNotBeCalled(lhs, rhs))
            .isExactlyInstanceOf(IllegalStateException.class);
    }

}
