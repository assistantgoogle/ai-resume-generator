package com.resumebuilder;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SmartEmails1ApplicationTests {
	@Autowired
	private ResumeService resumeservice;
	

	@Test
	void contextLoads()throws IOException {
		resumeservice.generateResumeResponse("Iam sachin markalle with 2 years of java experience");
	}

}
