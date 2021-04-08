package com.edomingues.examples.assertions;

import org.junit.jupiter.api.function.Executable;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssertThrows {

    public static Throwable assertThrowsWithMessage(final String message, final Executable executable) {
        try {
            executable.execute();
        } catch (Throwable throwable) {
            assertEquals(message, throwable.getMessage());
            return throwable;
        }
        throw new AssertionFailedError("Throwable expected, but nothing was thrown.");
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> T assertThrowsWithMessage(final Class<T> expectedType, final String message, final Executable executable) {
        try {
            executable.execute();
        } catch (Throwable throwable) {
            if (expectedType.isInstance(throwable)) {
                assertEquals(message, throwable.getMessage());
                return (T) throwable;
            }
        }
        throw new AssertionFailedError("Throwable expected, but not thrown.");
    }

}
