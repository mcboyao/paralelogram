package com.paralelogram.auth;

import com.paralelogram.auth.controller.TokenController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles(value = { "test" })
@TestPropertySource(locations = "/test.properties")
@SpringBootTest
class AuthApplicationTests {

	@Autowired
	private TokenController tokenController;

	@Test
	void contextLoads() {
		Assertions.assertNotNull(tokenController, "Context loading test failed");
	}
}
