package com.wibuneverdie.core.generator;

import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@IdGeneratorType(ProcGenIdGenerator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface ProcGenId {
}
