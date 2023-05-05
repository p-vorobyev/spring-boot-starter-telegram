package dev.voroby.springframework.telegram.controller;

import dev.voroby.springframework.telegram.client.updates.ClientAuthorizationState;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/authorization")
public class AuthorizationController {

    private final ClientAuthorizationState authorizationState;

    public AuthorizationController(ClientAuthorizationState authorizationState) {
        this.authorizationState = authorizationState;
    }

    record Credential(@NotBlank String value){}

    @PostMapping(value = "/code", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateConfirmationCode(@RequestBody @Valid Credential credential) {
        authorizationState.checkAuthenticationCode(credential.value);
    }

    @PostMapping(value = "/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updatePassword(@RequestBody @Valid Credential credential) {
        authorizationState.checkAuthenticationPassword(credential.value);
    }

    @GetMapping(value = "/status")
    public String authorizationStatus() {
        return authorizationState.haveAuthorization() ? "AUTHORIZED" : "NOT_AUTHORIZED";
    }

}
