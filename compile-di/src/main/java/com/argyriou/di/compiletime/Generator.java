package com.argyriou.di.compiletime;

import com.squareup.javapoet.TypeSpec;
import lombok.NonNull;

import javax.annotation.processing.ProcessingEnvironment;
import java.lang.reflect.Field;
import java.util.Set;

public sealed interface Generator
        permits
        BeanBucketGenerator,
        BeanIfGenerator,
        ContextGenerator {
    default void generate() {
        throw new UnsupportedOperationException();
    }

    default void generate(Set<Field> fields, Set<Class<?>> beans) {
        throw new UnsupportedOperationException();
    }

    void writeFileOnClasspath(@NonNull final TypeSpec clazz);
}
