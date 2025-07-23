package com.NotePilot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class NotePilotService {
    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public NotePilotService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String processContent(NotePilotRequest request) {


        //Build prompt
        String prompt =buildPrompt(request);
        //query the API AI model
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );
        String response = webClient.post()
                .uri(geminiApiUrl + geminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        //Parse the response

        //Return the Response
        return extractTextFromResponse(response);
    }

    private String extractTextFromResponse(String response) {
        try{
            GeminiResponse geminiResponse = objectMapper.readValue(response, GeminiResponse.class);
            if (geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()){
                GeminiResponse.Candidate firstCandidate = geminiResponse.getCandidates().get(0);
                if(firstCandidate.getContent() != null &&
                            firstCandidate.getContent().getParts() !=null &&
                            !firstCandidate.getContent().getParts().isEmpty()) {
                    return firstCandidate.getContent().getParts().get(0).getText();
                }
            }
            return "No content found in the response.";
        } catch (Exception e){
            return "Error processing:" + e.getMessage();
        }
    }

    private String buildPrompt(NotePilotRequest request){
        StringBuilder prompt = new StringBuilder();;
        switch (request.getOperation()){
            case "summarize":
                prompt.append("Provide a clear and concise summary of the following text in few sentences which is important:\n\n");
                break;
            case "suggest":
                prompt.append("Based on the following content: suggest related topics and further reading. Format the response with clear headings and bullet points:\n\n");
                break;
            default:
                throw new IllegalArgumentException("Unknown operation:" + request.getOperation());
        }
        prompt.append(request.getContent());
        return prompt.toString();
    }
}
