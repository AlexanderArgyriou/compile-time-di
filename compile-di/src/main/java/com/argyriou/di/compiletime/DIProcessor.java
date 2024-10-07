package com.argyriou.di.compiletime;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes({
        "com.argyriou.di.beans.definitions.Bean",
        "com.argyriou.di.beans.definitions.Inject"
})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class DIProcessor
        extends AbstractProcessor {
    @Override
    public boolean process(
            Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv) {
        try {
            new BeanIfGenerator(processingEnv).generate();
            new BeanBucketGenerator(processingEnv).generate();
            new ContextGenerator(processingEnv, roundEnv).generate();
        } catch (Exception e) {
            processingEnv.getMessager()
                    .printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
        return true;
    }
}
