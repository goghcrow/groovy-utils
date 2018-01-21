package com.youzan.et.groovy.rulex

import java.lang.annotation.Annotation
import java.lang.annotation.Retention

import static java.lang.annotation.RetentionPolicy.RUNTIME

static <A extends Annotation> A findAnnotation(final Class<A> targetAnnotation, final Class<?> annotatedType) {
    A foundAnnotation = annotatedType.getAnnotation(targetAnnotation);
    if (foundAnnotation == null) {
        for (Annotation annotation : annotatedType.getAnnotations()) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (annotationType.isAnnotationPresent(targetAnnotation)) {
                foundAnnotation = annotationType.getAnnotation(targetAnnotation);
                break;
            }
        }
    }
    return foundAnnotation;
}

static boolean isAnnotationPresent(final Class<? extends Annotation> targetAnnotation, final Class<?> annotatedType) {
    return findAnnotation(targetAnnotation, annotatedType) != null;
}

@Retention(RUNTIME)
@interface AnnoX {}

@Retention(RUNTIME)
@AnnoX
@interface AnnoY {}


@AnnoX
class ClassA {}

@AnnoY
class ClassB {}

println findAnnotation(AnnoX.class, ClassA.class)

println findAnnotation(AnnoX.class, ClassB.class)