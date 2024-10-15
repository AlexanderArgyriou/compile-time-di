package com.argyriou.di.context;

import com.argyriou.di.beans.definitions.Bean;
import com.argyriou.di.beans.definitions.Inject;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.Set;

public class Context {
    private final BeanBucket beanBucket;

    public Context() {
        this.beanBucket = new BeanBucket();
        init();
    }

    public Bucket getBeanBucket() {
        return beanBucket;
    }

    public void init() {
        var reflections = new Reflections("com.argyriou.di.beans");
        var allClasses = reflections.getTypesAnnotatedWith(Bean.class);
        var fieldsToInject = reflections.getFieldsAnnotatedWith(Inject.class);

        // fill bucket
        allClasses.forEach(clazz -> {
            var defConstructor = Arrays.stream(clazz.getDeclaredConstructors())
                    .filter(constructor -> constructor.getParameterCount() == 0)
                    .findFirst().orElseThrow(() -> new RuntimeException("no def constructor"));
            try {
                beanBucket.add(defConstructor.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // connect components
        fieldsToInject.forEach(field -> {
            field.setAccessible(true);
            var beanToInject = beanBucket.get(field.getType());
            var whereToInjectTheBean = beanBucket.get(field.getDeclaringClass());
            try {
                field.set(whereToInjectTheBean, beanToInject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
