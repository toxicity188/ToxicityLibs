package kor.toxicity.toxicitylibs.api;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ReaderBuilder {
    private ReaderBuilder() {
        throw new RuntimeException();
    }
    public static <T> Builder<T> simple(String s) {
        return new SimpleReaderBuilder<>(s);
    }
    public static Builder<Player> placeholder(String s) {
        return new PlaceholderReaderBuilder(s);
    }
    public static <T> VariableBuilder<T> variable(String s) {
        return new VariableReaderBuilder<>(s);
    }

    public interface Builder<T> {
        ComponentReader<T> build();
    }
    public interface VariableBuilder<T> extends Builder<T> {
        VariableBuilder<T> register(String pattern, Function<T,String> function);
    }

    private record SimpleReaderBuilder<T>(String s) implements Builder<T> {

        @Override
        public ComponentReader<T> build() {
            return new ComponentReader<>(s, (a, b) -> b);
        }
    }
    private record PlaceholderReaderBuilder(String s) implements Builder<Player> {

        @Override
        public ComponentReader<Player> build() {
            return new ComponentReader<>(s, PlaceholderAPI::setPlaceholders);
        }
    }
    private static final class VariableReaderBuilder<T> implements VariableBuilder<T> {
        private final List<Pair> pairs = new ArrayList<>();

        private final String s;
        private VariableReaderBuilder(String s) {
            this.s = s;
        }
        @Override
        public ComponentReader<T> build() {
            return new ComponentReader<>(s, (t,c) -> {
                var r = c;
                for (Pair pair : pairs) {
                    var g = pair.function.apply(t);
                    r = pair.pattern.matcher(r).replaceAll(g);
                }
                return r;
            });
        }

        @Override
        public VariableBuilder<T> register(String pattern, Function<T, String> function) {
            try {
                pairs.add(new Pair(Pattern.compile(pattern, Pattern.UNICODE_CHARACTER_CLASS), function));
            } catch (Exception ignored) {
            }
            return this;
        }

        private class Pair {
            private final Pattern pattern;
            private final Function<T,String> function;
            private Pair(Pattern pattern, Function<T,String> function) {
                this.pattern = pattern;
                this.function = function;
            }
        }
    }
}
