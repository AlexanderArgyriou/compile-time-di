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
import java.util.List;
import java.util.Set;

import static com.argyriou.di.compiletime.Constants.*;

@RequiredArgsConstructor
public final class ContextGenerator
        implements Generator {
    private final ProcessingEnvironment processingEnv;
    private final RoundEnvironment roundEnv;

    @Override
    public void generate() {
        if (classExists(processingEnv, PACKAGE + ".Context")) {
            return;
        }

        FieldSpec beanBucketField = constructBeanBucketField();
        MethodSpec constructor = constructConstructor();
        MethodSpec getBeanBucket = constructGetBeanBucketMethod();
        MethodSpec.Builder initMethodBuilder = constructInitMethodBuilder();

        performDI(initMethodBuilder);

        TypeSpec contextClass =
                constructContextClas(beanBucketField, constructor, getBeanBucket, initMethodBuilder);

        writeFileOnClasspath(processingEnv, contextClass);
    }

    private void performDI(@NonNull final MethodSpec.Builder initMethodBuilder) {
        Set<? extends Element> beanElements =
                roundEnv.getElementsAnnotatedWith(Bean.class);

        initBeans(initMethodBuilder, beanElements);
        addDependencies(initMethodBuilder, beanElements);
    }

    private static void addDependencies(
            @NonNull final MethodSpec.Builder initMethodBuilder,
            @NonNull final Set<? extends Element> beanElements) {
        beanElements.forEach(be -> {
            String lowercasedBeanName
                    = Character.toLowerCase(be.getSimpleName().charAt(0)) + be.getSimpleName().toString().substring(1);

            List<VariableElement> fields =
                    ElementFilter.fieldsIn(be.getEnclosedElements())
                            .stream()
                            .filter(f -> f.getAnnotation(Inject.class) != null)
                            .toList();

            fields.forEach(dep -> {
                TypeName fieldTypeName = ClassName.get(dep.asType());
                String lowercasedDepName = Character.toLowerCase(dep.getSimpleName().charAt(0))
                        + dep.getSimpleName().toString().substring(1);
                initMethodBuilder.addStatement("$L.set$T($L)", lowercasedBeanName, fieldTypeName, lowercasedDepName);
            });
        });
    }

    private static void initBeans(
            @NonNull final MethodSpec.Builder initMethodBuilder,
            @NonNull final Set<? extends Element> beanElements) {
        beanElements.forEach(be -> {
            TypeName typeName = ClassName.get(be.asType());
            String lowercasedBeanName =
                    Character.toLowerCase(be.getSimpleName().charAt(0))
                            + be.getSimpleName().toString().substring(1);

            initMethodBuilder.addStatement("$T $L = new $T()", typeName, lowercasedBeanName, typeName);
            initMethodBuilder.addStatement("beanBucket.add($L)", lowercasedBeanName);
        });
    }

    @Override
    @SneakyThrows
    public void writeFileOnClasspath(
            @NonNull final ProcessingEnvironment processingEnv,
            @NonNull final TypeSpec clazz) {
        JavaFile javaFile = JavaFile.builder(PACKAGE, clazz)
                .build();
        javaFile.writeTo(processingEnv.getFiler());
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
