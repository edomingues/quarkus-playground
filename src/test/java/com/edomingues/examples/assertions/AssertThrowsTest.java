package com.edomingues.examples.assertions;

import org.junit.jupiter.api.Test;

import static com.edomingues.examples.assertions.AssertThrows.assertThrowsWithMessage;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class AssertThrowsTest {

    @Test
    void assertExceptionType() {

        assertThrows(RuntimeException.class,
                     () -> {throw new RuntimeException("test message");}
                    );
    }

    @Test
    void assertExceptionMessage() {

        final Throwable throwableThrown = new RuntimeException("test message");

        Throwable throwableCatched = assertThrowsWithMessage("test message",
                                () -> {throw throwableThrown;});

        assertEquals(throwableThrown, throwableCatched);
    }

    @Test
    void assertExceptionTypeAndMessage() {

        final Throwable throwableThrown = new RuntimeException("test message");

        Throwable throwableCatched = assertThrowsWithMessage(RuntimeException.class,
                                                             "test message",
                                                             () -> {throw throwableThrown;});

        assertEquals(throwableThrown, throwableCatched);
    }

    @Test
    void assertThatThrowsTypeWithMessage() {
        // Given
        // When
        Throwable thrown = catchThrowable(() -> {throw new RuntimeException("test message");});
        // Then
        then(thrown).isInstanceOf(RuntimeException.class)
                    .hasMessage("test message");
    }
}
