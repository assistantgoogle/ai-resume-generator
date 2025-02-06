package com.resumebuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ResumeServiceImpl implements ResumeService {

    private final ChatClient chatClient;

    public ResumeServiceImpl(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public String generateResumeresponse(String userResumeDescription) {
        try {
            String promptString = this.loadPromptFromFile("resume_prompt.txt");
            String promptContent = this.putValuesToTemplate(promptString, Map.of(
                    "userDescription", userResumeDescription
            ));

            Prompt prompt = new Prompt(promptContent);
            return chatClient.prompt(prompt).call().content();
        } catch (IOException e) {
            e.printStackTrace();
            return "Error generating resume response.";
        }
    }

    private String loadPromptFromFile(String filename) throws IOException {
        Path path = new ClassPathResource(filename).getFile().toPath();
        return Files.readString(path);
    }

    private String putValuesToTemplate(String template, Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            template = template.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return template;
    }
    
    
    public static Map<String, Object> parseMultipleResponses(String response) {
        Map<String, Object> jsonResponse = new HashMap<>();

        // Extract content inside <think> tags
        int thinkStart = response.indexOf("<think>") + 7;
        int thinkEnd = response.indexOf("</think>");
        if (thinkStart != -1 && thinkEnd != -1) {
            String thinkContent = response.substring(thinkStart, thinkEnd).trim();
            jsonResponse.put("think", thinkContent);
        } else {
            jsonResponse.put("think", null); // Handle missing <think> tags
        }

        // Extract content that is in JSON format
        int jsonStart = response.indexOf("```json") + 7; // Start after ```json
        int jsonEnd = response.lastIndexOf("```");       // End before ```
        if (jsonStart != -1 && jsonEnd != -1 && jsonStart < jsonEnd) {
            String jsonContent = response.substring(jsonStart, jsonEnd).trim();
            try {
                // Convert JSON string to Map using Jackson ObjectMapper
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> dataContent = objectMapper.readValue(jsonContent, Map.class);
                jsonResponse.put("data", dataContent);
            } catch (Exception e) {
                jsonResponse.put("data", null); // Handle invalid JSON
                System.err.println("Invalid JSON format in the response: " + e.getMessage());
            }
        } else {
            jsonResponse.put("data", null); // Handle missing JSON
        }

        return jsonResponse;
    }
}
