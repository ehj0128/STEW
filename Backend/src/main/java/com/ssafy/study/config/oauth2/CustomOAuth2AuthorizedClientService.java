package com.ssafy.study.config.oauth2;


import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;


public class CustomOAuth2AuthorizedClientService implements OAuth2AuthorizedClientService{

	@Override
	public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId,
			String principalName) {

		return null;
		
	}

	@Override
	public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
		
		
	}

	@Override
	public void removeAuthorizedClient(String clientRegistrationId, String principalName) {

		return;
		
	}
	
	

}
