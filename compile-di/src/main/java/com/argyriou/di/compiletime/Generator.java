package com.argyriou.di.compiletime;

import com.squareup.javapoet.TypeSpec;
import lombok.NonNull;

import javax.annotation.processing.ProcessingEnvironment;

public sealed interface Generator
        permits
        BeanBucketGenerator,
        BeanIfGenerator,
        ContextGenerator {
    void generate();

    void writeFileOnClasspath(
            @NonNull final ProcessingEnvironment processingEnv,
            @NonNull final TypeSpec clazz);

    default boolean classExists(
            @NonNull final ProcessingEnvironment processingEnv,
            @NonNull final String className) {
        return processingEnv
                .getElementUtils()
                .getTypeElement(className) != null;
    }
}
