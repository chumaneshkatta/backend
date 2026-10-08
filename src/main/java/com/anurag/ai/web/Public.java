package com.anurag.ai.web;

import java.lang.annotation.*;

/** Endpoint needs no token (a valid token, if sent, is still resolved). */
@Target(ElementType.METHOD) @Retention(RetentionPolicy.RUNTIME)
public @interface Public {}
