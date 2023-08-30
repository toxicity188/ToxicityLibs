package kor.toxicity.toxicitylibs.util;

import java.util.function.BiConsumer;

public class FunctionUtil {
    public static <T> void forEachIndexed(Iterable<T> iterable, BiConsumer<Integer,T> consumer) {
        int i = 0;
        for (T t : iterable) {
            consumer.accept(i++, t);
        }
    }
    public static <T> void forEachIndexed(T[] iterable, BiConsumer<Integer,T> consumer) {
        int i = 0;
        for (T t : iterable) {
            consumer.accept(i++, t);
        }
    }
}
