package com.argyriou.di.compiletime;

import com.argyriou.di.beans.definitions.Bean;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes({
        "com.argyriou.di.beans.definitions.Bean",
        "com.argyriou.di.beans.definitions.Inject"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class DIProcessor
        extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            generateBeanBucketIf();
            generateBeanBucket();
            generateContext(roundEnv);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void generateContext(RoundEnvironment roundEnv)
            throws IOException {
        if (classExists("com.argyriou.di.compiletime.Context")) {
            // Don't recreate the file if it already exists
            return;
        }
        // Define the package
        String packageName = "com.argyriou.di.compiletime";

        // Create the BeanBucket field
        FieldSpec beanBucketField = FieldSpec.builder(ClassName.get(packageName, "BeanBucket"), "beanBucket", Modifier.PRIVATE, Modifier.FINAL)
                .build();

        // Create the constructor
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.beanBucket = new $T()", ClassName.get(packageName, "BeanBucket"))
                .addStatement("init()")
                .build();

        // Create the getBeanBucket method
        MethodSpec getBeanBucket = MethodSpec.methodBuilder("getBeanBucket")
                .addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(packageName, "Bucket"))
                .addStatement("return beanBucket")
                .build();

        // Create the init method
        MethodSpec.Builder initMethodBuilder = MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("// add beans");

        Set<? extends Element> beanElements = roundEnv.getElementsAnnotatedWith(Bean.class);


        beanElements.forEach(be -> {
            TypeName typeName = ClassName.get(be.asType());
            String lowercasedBeanName = Character.toLowerCase(be.getSimpleName().charAt(0)) + be.getSimpleName().toString().substring(1);

            initMethodBuilder.addStatement("$T $L = new $T()", typeName, lowercasedBeanName, typeName);
            initMethodBuilder.addStatement("beanBucket.add($L)", lowercasedBeanName);
        });

        beanElements.forEach(be -> {
            String lowercasedBeanName
                    = Character.toLowerCase(be.getSimpleName().charAt(0)) + be.getSimpleName().toString().substring(1);

            List<VariableElement> fields =
                    ElementFilter.fieldsIn(be.getEnclosedElements());// find one with inject
            fields.forEach(dep -> {
                TypeName fieldTypeName = ClassName.get(dep.asType());
                String lowercasedDepName = Character.toLowerCase(dep.getSimpleName().charAt(0)) + dep.getSimpleName().toString().substring(1);
                initMethodBuilder.addStatement("$L.set$T($L)", lowercasedBeanName, fieldTypeName, lowercasedDepName);
            });
        });


//        beanElements.forEach(element -> {
//            TypeName typeName = ClassName.get(element.asType());
//            initMethodBuilder.addStatement("beanBucket.add(new $T())", typeName);
//        });


        // Build the Context class
        TypeSpec contextClass = TypeSpec.classBuilder("Context")
                .addModifiers(Modifier.PUBLIC)
                .addField(beanBucketField)
                .addMethod(constructor)
                .addMethod(getBeanBucket)
                .addMethod(initMethodBuilder.build())
                .build();

        // Generate the Java file
        JavaFile javaFile = JavaFile.builder(packageName, contextClass)
                .build();

        // Write the Java file to the specified location
        javaFile.writeTo(processingEnv.getFiler());
    }

    private boolean classExists(String className) {
        return processingEnv.getElementUtils().getTypeElement(className) != null;
    }

    private void generateBeanBucketIf() {
        // Check if the class is already created
        if (classExists("com.argyriou.di.compiletime.Bucket")) {
            // Don't recreate the file if it already exists
            return;
        }

        MethodSpec getMethod = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addTypeVariable(TypeVariableName.get("T"))
                .returns(TypeVariableName.get("T"))
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), TypeVariableName.get("T")), "type")
                .build();

        MethodSpec addMethod = MethodSpec.methodBuilder("add")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addTypeVariable(TypeVariableName.get("T"))
                .returns(void.class)
                .addParameter(TypeVariableName.get("T"), "bean")
                .build();

        TypeSpec beanBucketIf = TypeSpec.interfaceBuilder("Bucket")
                .addModifiers(Modifier.PUBLIC, Modifier.SEALED)
                .addMethod(getMethod)
                .addMethod(addMethod)
                .build();

        try {
            // Use JavaPoet to generate the initial code (without the permits clause)
            JavaFile javaFile = JavaFile.builder("com.argyriou.di.compiletime", beanBucketIf).build();

            // Convert the generated code to a string
            String generatedCode = javaFile.toString();

            // Manually append the permits clause
            String finalCode = generatedCode.replaceFirst("Bucket \\{", "Bucket permits BeanBucket {");

            // Use Filer to write the file in the correct directory
            Filer filer = processingEnv.getFiler();

            JavaFileObject sourceFile = filer.createSourceFile("com.argyriou.di.compiletime" + "." + "Bucket");

            // Write the final code into the source file
            try (Writer writer = sourceFile.openWriter()) {
                writer.write(finalCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateBeanBucket() {
        // Check if the class is already created
        if (classExists("com.argyriou.di.compiletime.BeanBucket")) {
            // Don't recreate the file if it already exists
            return;
        }
        FieldSpec beansField = FieldSpec.builder(ParameterizedTypeName.get(Map.class, String.class, Object.class),
                        "beans", Modifier.PRIVATE)
                .initializer("new $T<>()", HashMap.class)
                .build();

        // Create the get method: <T> T get(Class<T> type)
        MethodSpec getMethod = MethodSpec.methodBuilder("get")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addTypeVariable(TypeVariableName.get("T"))
                .returns(TypeVariableName.get("T"))
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), TypeVariableName.get("T")), "type")
                .addStatement("return (T) beans.get(type.getName())")
                .build();

        // Create the add method: <T> void add(T bean)
        MethodSpec addMethod = MethodSpec.methodBuilder("add")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addTypeVariable(TypeVariableName.get("T"))
                .returns(void.class)
                .addParameter(TypeVariableName.get("T"), "bean")
                .addStatement("beans.put(bean.getClass().getName(), bean)")
                .build();

        // Build the BeanBucket class
        TypeSpec beanBucketClass = TypeSpec.classBuilder("BeanBucket")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ClassName.get("com.argyriou.di.compiletime", "Bucket"))
                .addField(beansField)
                .addMethod(getMethod)
                .addMethod(addMethod)
                .build();

        try {
            // Write the class to a file
            JavaFile javaFile = JavaFile.builder("com.argyriou.di.compiletime", beanBucketClass)
                    .build();

            // Use Filer to write the file in the correct directory
            Filer filer = processingEnv.getFiler();
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
