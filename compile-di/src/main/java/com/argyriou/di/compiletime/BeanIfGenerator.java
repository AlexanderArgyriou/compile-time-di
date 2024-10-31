package com.argyriou.di.compiletime;

import com.squareup.javapoet.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;

import static com.argyriou.di.compiletime.Constants.*;

@RequiredArgsConstructor
public final class BeanIfGenerator
        implements Generator {
    @Override
    public void generate() {
        MethodSpec getMethod = constructGenericGetBeanIfMethod();
        MethodSpec addMethod = constructGenericAddBeanIfMethod();
        TypeSpec beanBucketIf = constructBeanInterface(getMethod, addMethod);

        writeFileOnClasspath(beanBucketIf);
    }

    @Override
    public void writeFileOnClasspath(
            @NonNull final TypeSpec clazz) {
        try {
            JavaFile javaFile = JavaFile.builder(PACKAGE, clazz).build();
            javaFile.writeTo(Paths.get("target/generated-sources/java"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static TypeSpec constructBeanInterface(
            @NonNull final MethodSpec getMethod,
            @NonNull final MethodSpec addMethod) {
        return TypeSpec.interfaceBuilder("Bucket")
                .addModifiers(Modifier.PUBLIC)
                .addMethod(getMethod)
                .addMethod(addMethod)
                .build();
    }

    private static MethodSpec constructGenericAddBeanIfMethod() {
        return MethodSpec.methodBuilder("add")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addTypeVariable(TypeVariableName.get("T"))
                .returns(void.class)
                .addParameter(TypeVariableName.get("T"), "bean")
                .build();
    }

    private static MethodSpec constructGenericGetBeanIfMethod() {
        return MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addTypeVariable(TypeVariableName.get("T"))
                .returns(TypeVariableName.get("T"))
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class),
                        TypeVariableName.get("T")), "type")
                .build();
    }
}
