package com.dtstack.taier.datasource.plugin.common.reflect;

 


import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

public class ReflectUtils {

    /**
     * Determines whether the provided class is loadable
     * @param className the class name
     * @param cl the class loader
     * @return is the class name loadable with the class loader
     */
    public static boolean isClassLoadable(String className, ClassLoader cl) {
        if (cl == null) {
            cl = Thread.currentThread().getContextClassLoader();
        }
        try {
            DynClasses.builder().loader(cl).impl(className).buildChecked();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * get the field value of the given object
     * @param target the target object
     * @param fieldName the field name from declared field names
     * @param <T> the expected return class type
     * @return the field value
     */
    @SuppressWarnings("unchecked")
    public static <T> T getField(Object target, String fieldName) {
        Pair<Class<?>, Object> pair = getTargetClass(target);
        Class<?> clz = pair.getLeft();
        Object obj = pair.getRight();
        try {
            DynFields.UnboundField<T> field = DynFields.builder()
                    .hiddenImpl(clz, fieldName)
                    .impl(clz, fieldName)
                    .build();
            if (field.isStatic()) {
                return field.asStatic().get();
            } else {
                return field.bind(obj).get();
            }
        } catch (Exception e) {
            throw new RuntimeException(clz + " does not have " + fieldName + " field", e);
        }
    }

    /**
     * Invoke a method with the given name and arguments on the given target object.
     * @param target the target object
     * @param methodName the method name from declared field names
     * @param args pairs of class and values for the arguments
     * @param <T> the expected return class type
     * @return the result of the method invocation
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeAs(Object target, String methodName, Pair<Class<?>, Object>... args) {
        Pair<Class<?>, Object> pair = getTargetClass(target);
        Class<?> clz = pair.getLeft();
        Object obj = pair.getRight();
        Class<?>[] argClasses = new Class<?>[args.length];
        Object[] argValues = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            argClasses[i] = args[i].getLeft();
            argValues[i] = args[i].getRight();
        }
        try {
            DynMethods.UnboundMethod method = DynMethods.builder(methodName)
                    .hiddenImpl(clz, argClasses)
                    .impl(clz, argClasses)
                    .buildChecked();
            if (method.isStatic()) {
                return (T) method.asStatic().invoke(argValues);
            } else {
                return (T) method.bind(obj).invoke(argValues);
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    formatErrorMessage(clz, methodName, argClasses),
                    e);
        }
    }

    private static String formatErrorMessage(Class<?> clz, String methodName, Class<?>[] argClasses) {
        StringBuilder sb = new StringBuilder();
        sb.append(clz.getName()).append(" does not have ").append(methodName).append("(");
        for (int i = 0; i < argClasses.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(argClasses[i].getSimpleName());
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Creates a iterator for with a new service loader for the given service type and class
     * loader.
     *
     * @param cl The class loader to be used to load provider-configuration files
     *           and provider classes
     * @param serviceClass class of the service type
     * @param <T> the class of the service type
     * @return an iterator of the service instances
     */
    public static <T> Iterator<T> loadFromServiceLoader(ClassLoader cl, Class<T> serviceClass) {
        if (cl == null) {
            cl = Thread.currentThread().getContextClassLoader();
        }
        return StreamSupport.stream(ServiceLoader.load(serviceClass, cl).spliterator(), false)
                .iterator();
    }

    private static Pair<Class<?>, Object> getTargetClass(Object target) {
        if (target instanceof Class) {
            return new Pair<>((Class<?>) target, null);
        } else if (target instanceof String) {
            try {
                return new Pair<>(DynClasses.builder().impl((String) target).buildChecked(), null);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Class not found: " + target, e);
            }
        } else if (target instanceof Pair) {
            Pair<?, ?> pair = (Pair<?, ?>) target;
            if (pair.getLeft() instanceof Class && pair.getRight() != null) {
                return new Pair<>((Class<?>) pair.getLeft(), pair.getRight());
            } else if (pair.getLeft() instanceof String && pair.getRight() != null) {
                try {
                    return new Pair<>(DynClasses.builder().impl((String) pair.getLeft()).buildChecked(), pair.getRight());
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Class not found: " + pair.getLeft(), e);
                }
            }
        }
        return new Pair<>(target.getClass(), target);
    }


    private static class Pair<L, R> {
        private final L left;
        private final R right;

        Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }

        L getLeft() {
            return left;
        }

        R getRight() {
            return right;
        }
    }
}
