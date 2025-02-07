package com.resumebuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ResumeServiceImpl implements ResumeService {

    private static final Logger logger = LoggerFactory.getLogger(ResumeServiceImpl.class);
    private final ChatClient chatClient;

    public ResumeServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public Map<String, Object> generateResumeResponse(String userResumeDescription) throws IOException {
        logger.info("Generating resume response for user description: {}", userResumeDescription);

        String promptString = this.loadPromptFromFile("resume_prompt.txt");
        String promptContent = this.putValuesToTemplate(promptString, Map.of(
                "userDescription", userResumeDescription
        ));
        Prompt prompt = new Prompt(promptContent);
        String response = chatClient.prompt(prompt).call().content();
        Map<String, Object> stringObjectMap = parseMultipleResponses(response);

        logger.info("Generated resume response: {}", stringObjectMap);
        return stringObjectMap;
    }

    private String loadPromptFromFile(String filename) throws IOException {
        logger.info("Loading prompt from file: {}", filename);
        Path path = new ClassPathResource(filename).getFile().toPath();
        String content = Files.readString(path);
        logger.info("Loaded prompt content: {}", content);
        return content;
    }

    private String putValuesToTemplate(String template, Map<String, String> values) {
        logger.info("Putting values into template: {}", template);
        for (Map.Entry<String, String> entry : values.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        logger.info("Updated template: {}", template);
        return template;
    }

    private static Map<String, Object> parseMultipleResponses(String response) {
        Map<String, Object> jsonResponse = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        logger.info("Parsing response: {}", response);

        try {
            // Extract JSON content between ```json and ```
            int jsonStart = response.indexOf("```json") + 7;
            int jsonEnd = response.lastIndexOf("```");

            if (jsonStart > 6 && jsonEnd > jsonStart) {
                String jsonContent = response.substring(jsonStart, jsonEnd).trim();
                jsonResponse = objectMapper.readValue(jsonContent, Map.class);
            }
        } catch (Exception e) {
            logger.error("Error parsing JSON response: {}", e.getMessage());
        }

        logger.info("Parsed JSON response: {}", jsonResponse);
        return jsonResponse;
    }
}
