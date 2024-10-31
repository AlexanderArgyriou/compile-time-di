package com.argyriou.di.compiletime;

import com.argyriou.di.beans.definitions.Bean;
import com.argyriou.di.beans.definitions.Inject;
import com.squareup.javapoet.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static com.argyriou.di.compiletime.Constants.*;

@RequiredArgsConstructor
public final class ContextGenerator
        implements Generator {

    @Override
    public void generate(Set<Field> fields, Set<Class<?>> beans) {
        FieldSpec beanBucketField = constructBeanBucketField();
        MethodSpec constructor = constructConstructor();
        MethodSpec getBeanBucket = constructGetBeanBucketMethod();
        MethodSpec.Builder initMethodBuilder = constructInitMethodBuilder();

        performDI(initMethodBuilder, fields, beans);

        TypeSpec contextClass =
                constructContextClas(beanBucketField, constructor, getBeanBucket, initMethodBuilder);

        writeFileOnClasspath(contextClass);
    }

    private void performDI(@NonNull final MethodSpec.Builder initMethodBuilder,
                           Set<Field> fields,
                           Set<Class<?>> beans) {
        initBeans(initMethodBuilder, beans);
        addDependencies(initMethodBuilder, fields);
    }

    private static void addDependencies(
            @NonNull final MethodSpec.Builder initMethodBuilder,
            @NonNull final Set<Field> fields) {
        fields.forEach(dep -> {
            String typeName = dep.getDeclaringClass().getSimpleName();
            String lowercasedBeanName = Character.toLowerCase(typeName.charAt(0))
                    + typeName.substring(1);

            TypeName tn = TypeName.get(dep.getType());
            initMethodBuilder.addStatement("$L.set$T($L)", lowercasedBeanName, tn, dep.getName());
        });
    }

    private static void initBeans(
            @NonNull final MethodSpec.Builder initMethodBuilder,
            @NonNull final Set<Class<?>> beans) {
        beans.forEach(be -> {
            String typeName = be.getSimpleName();
            TypeName tn = TypeName.get(be);
            String lowercasedBeanName =
                    Character.toLowerCase(typeName.charAt(0))
                            + typeName.substring(1);

            initMethodBuilder.addStatement("$T $L = new $T()", tn, lowercasedBeanName, tn);
            initMethodBuilder.addStatement("beanBucket.add($L)", lowercasedBeanName);
        });
    }

    @Override
    @SneakyThrows
    public void writeFileOnClasspath(
            @NonNull final TypeSpec clazz) {
        try {
            JavaFile javaFile = JavaFile.builder(PACKAGE, clazz).build();
            javaFile.writeTo(Paths.get("target/generated-sources/java"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static TypeSpec constructContextClas(
            @NonNull final FieldSpec beanBucketField,
            @NonNull final MethodSpec constructor,
            @NonNull final MethodSpec getBeanBucket,
            @NonNull final MethodSpec.Builder initMethodBuilder) {
        return TypeSpec.classBuilder("Context")
                .addModifiers(Modifier.PUBLIC)
                .addField(beanBucketField)
                .addMethod(constructor)
                .addMethod(getBeanBucket)
                .addMethod(initMethodBuilder.build())
                .build();
    }

    private static MethodSpec.Builder constructInitMethodBuilder() {
        return MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("// add beans");
    }

    private static MethodSpec constructGetBeanBucketMethod() {
        return MethodSpec.methodBuilder("getBeanBucket")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(PACKAGE, "Bucket"))
                .addStatement("return beanBucket")
                .build();
    }

    private static MethodSpec constructConstructor() {
        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.beanBucket = new $T()",
                        ClassName.get(PACKAGE, "BeanBucket"))
                .addStatement("init()")
                .build();
    }

    private static FieldSpec constructBeanBucketField() {
        return FieldSpec.builder(
                        ClassName.get(PACKAGE, "BeanBucket"),
                        "beanBucket", Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }
}
