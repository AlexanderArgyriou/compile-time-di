package com.argyriou.di.compiletime;

public sealed interface Generator
        permits
        BeanBucketGenerator,
        BeanIfGenerator,
        ContextGenerator {
}
