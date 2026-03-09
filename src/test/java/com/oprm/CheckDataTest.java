package com.oprm;

import com.oprm.repository.UserRepository;
import com.oprm.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CheckDataTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    public void testDataSeeded() {
        long userCount = userRepository.count();
        long projectCount = projectRepository.count();

        System.out.println("====== SEEDING RESULTS ======");
        System.out.println("Users generated: " + userCount);
        userRepository.findAll().forEach(u -> System.out.println("Email: " + u.getEmail() + " | Role: " + u.getRole()));
        System.out.println("Projects generated: " + projectCount);
        System.out.println("=============================");

        assertThat(userCount).isGreaterThanOrEqualTo(20);
        assertThat(projectCount).isGreaterThanOrEqualTo(20);
    }
}
