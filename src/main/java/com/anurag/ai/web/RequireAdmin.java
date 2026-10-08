package com.anurag.ai.web;

import java.lang.annotation.*;

/** Restricts a controller or method to users whose role is "admin". */
@Target({ElementType.METHOD, ElementType.TYPE}) @Retention(RetentionPolicy.RUNTIME)
public @interface RequireAdmin {}
