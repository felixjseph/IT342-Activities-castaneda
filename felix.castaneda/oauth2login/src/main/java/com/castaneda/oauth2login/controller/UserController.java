package com.castaneda.oauth2login.controller;

import com.castaneda.oauth2login.service.GoogleContactsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;


import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final GoogleContactsService googleContactsService;

    public UserController(OAuth2AuthorizedClientService authorizedClientService, GoogleContactsService googleContactsService) {
        this.authorizedClientService = authorizedClientService;
        this.googleContactsService = googleContactsService;
    }

    @ModelAttribute("contacts")
    public List<Map<String, Object>> fetchContacts(OAuth2AuthenticationToken authentication) {
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        if (client == null || client.getAccessToken() == null) {
            return Collections.emptyList(); // Return empty list if token is missing
        }

        return googleContactsService.getContacts(client.getAccessToken().getTokenValue());
    }

    @GetMapping("/contacts")
    public String getGoogleContactsPage() {
        return "contacts"; // Matches contacts.html
    }

    @GetMapping("/user-info")
    public String getUser(@AuthenticationPrincipal OAuth2User principal, Model model) {
        model.addAttribute("userInfo", principal.getAttributes());
        return "user-info"; // This should map to 'user-info.html'
    }
}