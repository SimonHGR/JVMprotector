package org.dancingcloudservices.whenthingsbreak;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Either<L, R> {

    public static interface ExFunction<A, B> {
        B apply(A a) throws Throwable;
    }

    private final L left;
    private final R right;
    private Either(L left, R right) {
        this.left = left;
        this.right = right;
    }
    public static <L, R> Either<L, R> success(R right) {
        return new Either(null, right);
    }
    public static <L, R> Either<L, R> failure(L left) {
        return new Either(left, null);
    }

    public void ifSuccess(Consumer<R> op) {
        if (left == null) op.accept(right);
    }

    public void ifFailed(Consumer<L> op) {
        if (left != null) op.accept(left);
    }

    public <L1, R1> Either<L1, R1> flatMap(BiFunction<L, R, Either<L1, R1>> op) {
        return op.apply(left, right);
    }

    public static <T, U> Function<T, Either<Throwable, U>> wrap(ExFunction<T, U> op) {
        return t -> {
            try {
                return Either.success(op.apply(t));
            } catch (Throwable ex) {
                return Either.failure(ex);
            }
        };
    }
}
