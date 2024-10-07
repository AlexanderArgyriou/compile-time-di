package com.argyriou.di.compiletime;

import javax.annotation.processing.ProcessingEnvironment;

public sealed interface Generator
        permits
        BeanBucketGenerator,
        BeanIfGenerator,
        ContextGenerator {
    void generate(ProcessingEnvironment processingEnv);

    default boolean classExists(ProcessingEnvironment processingEnv,
                                String className) {
        return processingEnv.getElementUtils().getTypeElement(className) != null;
    }
}
