package com.naisa.util;

import java.beans.JavaBean;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

/**
 * A utility class containing a collection of helper functions for JavaBeans.
 *
 * @author James Chen
 */
public final class Beans {
	private Beans() {
	}

	/**
	 * Creates a JavaBean instance by the given JavaBean class, which must be annotated by
	 * {@code JavaBean}.
	 *
	 * <p>
	 * This function passes the created instance to {@code initializer} before returning the
	 * instance. It catches all reflection-related exceptions and wraps wraps them with a
	 * {@code RuntimeException}, but it doesn't catch runtime exceptions.
	 *
	 * <p>
	 * This function should be used to create JavaBean instances throughout the codebase.
	 *
	 * @param clazz       the JavaBean class to create an instance from
	 * @param initializer a function that takes the created instance as an argument
	 * @return the created JavaBean instance
	 * @author James Chen
	 */
	public static <T extends Object> T create(final Class<T> clazz, final Consumer<T> initializer) {
		if (!clazz.isAnnotationPresent(JavaBean.class)) {
			throw new RuntimeException(
				"Beans.create() failed to create an object for a class that is not a Java Bean: "
					+ clazz.getName());
		}

		try {
			final Constructor<T> constructor = clazz.getConstructor();
			final T instance = constructor.newInstance();
			initializer.accept(instance);
			return instance;
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Public constructor does not exist in " + clazz.getName(),
				e);
		} catch (IllegalAccessException | IllegalArgumentException | InstantiationException
			| InvocationTargetException | ExceptionInInitializerError e) {
			throw new RuntimeException(
				"Beans.create() failed to create an object for " + clazz.getName(), e);
		}
	}
}
