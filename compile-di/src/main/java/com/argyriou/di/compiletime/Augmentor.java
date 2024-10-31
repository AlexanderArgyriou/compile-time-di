package com.argyriou.di.compiletime;

import com.argyriou.di.beans.definitions.Bean;
import com.argyriou.di.beans.definitions.Inject;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

public class Augmentor {
    public static void main(String[] args) {
        var reflections = new Reflections("com.argyriou.di.compiletime.beans",
                Scanners.FieldsAnnotated, org.reflections.scanners.Scanners.TypesAnnotated);

        var allClasses = reflections.getTypesAnnotatedWith(Bean.class);
        var fieldsToInject = reflections.getFieldsAnnotatedWith(Inject.class);

        Generator beanIf = new BeanIfGenerator();
        Generator bucket = new BeanBucketGenerator();
        Generator context = new ContextGenerator();

        beanIf.generate();
        bucket.generate();
        context.generate(fieldsToInject, allClasses);
    }
}
