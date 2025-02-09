package com.kuan.tdd.args;

import com.kuan.tdd.args.exceptions.IllegalOptionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author qinxuekuan
 * @date 2022/5/19
 */
public class Args {

    public static <T> T parse(Class<T> optionsClass, String... args) {


        List<String> arguments = Arrays.asList(args);
        Constructor<?> constructor = optionsClass.getDeclaredConstructors()[0];

        Object[] values = Arrays.stream(constructor.getParameters())
                .map(it -> parseOption(arguments, it)).toArray();
        try {
            return (T) constructor.newInstance(values);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object parseOption(List<String> arguments, Parameter parameter) {
        if (!parameter.isAnnotationPresent(Option.class)) {
            throw new IllegalOptionException(parameter.getName());
        }

        Class<?> parameterType = parameter.getType();
        Option option = parameter.getAnnotation(Option.class);

        return getParser(parameterType).parse(arguments, option);
    }

    static OptionParser<?> getParser(Class<?> parameterType) {
        return parsers.get(parameterType);
    }

    static Map<Class<?>, OptionParser<?>> parsers = Map.of(
            boolean.class, OptionParsers.bool(),
            int.class, OptionParsers.unary(0, Integer::valueOf),
            String.class, OptionParsers.unary("", String::valueOf),
            String[].class, OptionParsers.list(String[]::new, String::valueOf),
            Integer[].class, OptionParsers.list(Integer[]::new, Integer::valueOf)
    );

}
