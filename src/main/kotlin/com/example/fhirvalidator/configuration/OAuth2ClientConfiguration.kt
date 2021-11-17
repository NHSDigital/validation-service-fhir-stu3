package com.example.fhirvalidator.configuration

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.*
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod

const val REGISTRATION_ID = "terminology"

@Configuration
@ConditionalOnProperty("terminology.authorization.tokenUrl")
class OAuth2ClientConfiguration(private val terminologyValidationProperties: TerminologyValidationProperties) {
    @Bean
    fun clientRegistration(): ClientRegistration {
        val authorization = terminologyValidationProperties.authorization ?: throw Error("Missing authorization properties")
        return ClientRegistration.withRegistrationId(REGISTRATION_ID)
            .clientId(authorization.clientId)
            .clientSecret(authorization.clientSecret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.POST)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .tokenUri(authorization.tokenUrl)
            .build()
    }

    @Bean
    fun clientRegistrationRepository(clientRegistration: ClientRegistration): ClientRegistrationRepository {
        return InMemoryClientRegistrationRepository(clientRegistration)
    }

    @Bean
    fun authorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientService: OAuth2AuthorizedClientService
    ): OAuth2AuthorizedClientManager {
        return AuthorizedClientServiceOAuth2AuthorizedClientManager(
            clientRegistrationRepository,
            authorizedClientService
        )
    }

    @Bean
    fun authorizedClient(
        authorizedClientManager: OAuth2AuthorizedClientManager
    ): OAuth2AuthorizedClient {
        val authorizeRequest = OAuth2AuthorizeRequest
            .withClientRegistrationId(REGISTRATION_ID)
            .principal("test")
            .build()
        return authorizedClientManager.authorize(authorizeRequest) ?: throw Error("Failed to authorize")
    }
}
