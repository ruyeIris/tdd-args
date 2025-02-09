package com.kuan.tdd.args;

import com.kuan.tdd.args.exceptions.IllegalValueException;
import com.kuan.tdd.args.exceptions.InsufficientArgumentsException;
import com.kuan.tdd.args.exceptions.TooManyArgumentsException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

/**
 * @author qinxuekuan
 * @date 2022/5/27
 */
class OptionParsers {

    public static OptionParser<Boolean> bool() {
        return ((arguments, option) -> values(arguments, option, 0).isPresent());
    }

    public static <T> OptionParser<T> unary(T defaultValue, Function<String, T> valueParser) {
        return (arguments, option) -> values(arguments, option, 1)
                .map(it -> parseValue(it.get(0), option, valueParser))
                .orElse(defaultValue);
    }

    public static <T> OptionParser<T[]> list(IntFunction<T[]> generator, Function<String, T> valueParser) {
        return (arguments, option) -> values(arguments, option)
                .map(it -> it.stream().map(value -> parseValue(value, option, valueParser)).toArray(generator))
                .orElse(generator.apply(0));
    }

    private static <T> T parseValue(String value, Option option, Function<String, T> valueParser) {
        try {
            return valueParser.apply(value);
        } catch (Exception e) {
            throw new IllegalValueException(value, option.value());
        }
    }

    static Optional<List<String>> values(List<String> arguments, Option option) {
        int index = arguments.indexOf("-" + option.value());
        return Optional.ofNullable(index == -1 ? null : values(arguments, index));
    }

    static Optional<List<String>> values(List<String> arguments, Option option, int expectedSize) {
        return values(arguments, option).map(it -> {
            checkSize(option, expectedSize, it);
            return it;
        });
    }

    private static void checkSize(Option option, int expectedSize, List<String> values) {
        if (values.size() < expectedSize) {
            throw new InsufficientArgumentsException(option.value());
        }
        if (values.size() > expectedSize) {
            throw new TooManyArgumentsException(option.value());
        }
    }

    private static List<String> values(List<String> arguments, int index) {
        int followingFlag = IntStream.range(index + 1, arguments.size())
                .filter(it -> arguments.get(it).matches("^-[a-zA-Z]+$"))
                .findFirst().orElse(arguments.size());
        return arguments.subList(index + 1, followingFlag);
    }

}
