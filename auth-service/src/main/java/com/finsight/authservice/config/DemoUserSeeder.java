package com.finsight.authservice.config;

import com.finsight.authservice.model.ERole;
import com.finsight.authservice.model.User;
import com.finsight.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DemoUserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername("demo")) {
            return;
        }
        User user = User.builder()
                .username("demo")
                .email("demo@finsight.ai")
                .password(passwordEncoder.encode("demo123"))
                .role(ERole.ROLE_ADMIN)
                .build();
        userRepository.save(user);
        log.info("Seeded demo user: demo / demo123");
    }
}
