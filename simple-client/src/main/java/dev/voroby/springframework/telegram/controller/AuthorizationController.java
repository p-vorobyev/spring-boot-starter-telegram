package dev.voroby.springframework.telegram.controller;

import dev.voroby.springframework.telegram.client.updates.ClientAuthorizationState;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/authorization")
public class AuthorizationController {

    private final ClientAuthorizationState clientAuthorizationState;

    public AuthorizationController(ClientAuthorizationState clientAuthorizationState) {
        this.clientAuthorizationState = clientAuthorizationState;
    }

    @PostMapping(value = "/code/{codeValue}")
    public void updateConfirmationCode(@PathVariable("codeValue") String code) {
        clientAuthorizationState.checkAuthenticationCode(code);
    }

    @PostMapping(value = "/password/{passwordValue}")
    public void updatePassword(@PathVariable("passwordValue") String password) {
        clientAuthorizationState.checkAuthenticationPassword(password);
    }

    @GetMapping(value = "/status")
    public String authorizationStatus() {
        return clientAuthorizationState.haveAuthorization() ? "AUTHORIZED" : "NOT_AUTHORIZED";
    }

}
