package com.oexil.studentreg.service;

import org.springframework.security.crypto.factory.PasswordEncoderFactories;

public class PasswordGen {
    public static void main(String[] args) {
        System.out.println(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("gf!HDY#46"));
    }
}