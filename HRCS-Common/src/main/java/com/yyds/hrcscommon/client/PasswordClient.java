package com.yyds.hrcscommon.client;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.yml")
public class PasswordClient {
    @Value("${security.argon2.type}")
    private String argon2Type;

    @Value("${security.argon2.iterations}")
    private int iterations;

    @Value("${security.argon2.memory}")
    private int memory;

    @Value("${security.argon2.parallelism}")
    private int parallelism;

    public String hashPassword(String rawPassword) {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.valueOf(argon2Type));
        return argon2.hash(
                iterations,
                memory,
                parallelism,
                rawPassword.toCharArray()
        );
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.valueOf(argon2Type));
        return argon2.verify(encodedPassword, rawPassword.toCharArray());
    }
}
