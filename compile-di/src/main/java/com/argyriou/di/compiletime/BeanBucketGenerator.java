package com.argyriou.di.compiletime;

import com.squareup.javapoet.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.argyriou.di.compiletime.Constants.*;

@RequiredArgsConstructor
public final class BeanBucketGenerator
        implements Generator {
    private final ProcessingEnvironment processingEnv;

    @Override
    public void generate() {
        if (classExists(processingEnv, PACKAGE + ".BeanBucket")) {
            return;
        }

        FieldSpec beansMap = constructBeansMap();
        MethodSpec getMethod = constructGetBeanGenericMethodImpl();
        MethodSpec addMethod = constructGenericAddBeanMethodImpl();
        TypeSpec beanBucketClass = constructBeanBucktClass(beansMap, getMethod, addMethod);

        writeFileOnClasspath(processingEnv, beanBucketClass);
    }

    @Override
    public void writeFileOnClasspath(
            @NonNull final ProcessingEnvironment processingEnv,
            @NonNull final TypeSpec beanBucketClass) {
        try {
            JavaFile javaFile = JavaFile.builder(PACKAGE, beanBucketClass)
                    .build();
            Filer filer = processingEnv.getFiler();
            javaFile.writeTo(filer);
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    private static TypeSpec constructBeanBucktClass(
            @NonNull final FieldSpec beansMap,
            @NonNull final MethodSpec getMethod,
            @NonNull final MethodSpec addMethod) {
        return TypeSpec.classBuilder("BeanBucket")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ClassName.get(PACKAGE, "Bucket"))
                .addField(beansMap)
                .addMethod(getMethod)
                .addMethod(addMethod)
                .build();
    }

    private static MethodSpec constructGenericAddBeanMethodImpl() {
        return MethodSpec.methodBuilder("add")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addTypeVariable(TypeVariableName.get("T"))
                .returns(void.class)
                .addParameter(TypeVariableName.get("T"), "bean")
                .addStatement("beans.put(bean.getClass().getName(), bean)")
                .build();
    }

    private static MethodSpec constructGetBeanGenericMethodImpl() {
        return MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addTypeVariable(TypeVariableName.get("T"))
                .returns(TypeVariableName.get("T"))
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class),
                        TypeVariableName.get("T")), "type")
                .addStatement("return (T) beans.get(type.getName())")
                .build();
    }

    private static FieldSpec constructBeansMap() {
        return FieldSpec.builder(ParameterizedTypeName.get(Map.class, String.class, Object.class),
                        "beans", Modifier.PRIVATE)
                .initializer("new $T<>()", HashMap.class)
                .build();
    }
}
