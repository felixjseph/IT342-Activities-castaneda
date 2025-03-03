package com.castaneda.oauth2login.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GoogleContactsService {

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Map<String, Object>> getContacts(String accessToken) {
        String url = "https://people.googleapis.com/v1/people/me/connections"
                + "?personFields=names,emailAddresses,phoneNumbers"; // Added phoneNumbers

        // Set up HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // Make the HTTP request
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response;

        try {
            response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList(); // Return empty list if API call fails
        }

        return parseContacts(response.getBody());
    }

    private List<Map<String, Object>> parseContacts(String json) {
        List<Map<String, Object>> contactsList = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(json);
            JsonNode connections = root.path("connections");

            for (JsonNode contact : connections) {
                Map<String, Object> contactMap = new HashMap<>();

                // Extract names
                JsonNode names = contact.path("names");
                if (!names.isEmpty()) {
                    JsonNode nameData = names.get(0);
                    contactMap.put("displayName", nameData.path("displayName").asText(""));
                    contactMap.put("givenName", nameData.path("givenName").asText(""));
                    contactMap.put("familyName", nameData.path("familyName").asText(""));
                } else {
                    contactMap.put("displayName", "Unknown");
                }

                // Extract email addresses
                JsonNode emails = contact.path("emailAddresses");
                if (!emails.isEmpty()) {
                    contactMap.put("email", emails.get(0).path("value").asText(""));
                } else {
                    contactMap.put("email", "N/A");
                }

                // Extract phone numbers
                JsonNode phones = contact.path("phoneNumbers");
                if (!phones.isEmpty()) {
                    contactMap.put("phoneNumber", phones.get(0).path("value").asText(""));
                } else {
                    contactMap.put("phoneNumber", "N/A");
                }

                contactsList.add(contactMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contactsList;
    }
}
