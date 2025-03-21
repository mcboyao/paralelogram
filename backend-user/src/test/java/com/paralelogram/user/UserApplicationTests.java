package com.paralelogram.user;

import com.paralelogram.user.controller.UserController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@Disabled
@ActiveProfiles(value = { "test" })
@TestPropertySource(locations = "/test.properties")
@SpringBootTest
class UserApplicationTests {

	@Autowired
	private UserController userController;

	@Test
	void contextLoads() {
		Assertions.assertNotNull(userController, "Context loading test failed");
	}

}
