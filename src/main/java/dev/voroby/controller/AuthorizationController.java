package dev.voroby.controller;

import dev.voroby.client.TelegramClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/authorization")
public class AuthorizationController {

    private final TelegramClient telegramClient;

    public AuthorizationController(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @PostMapping(value = "/code/{codeValue}")
    public void updateConfirmationCode(@PathVariable("codeValue") String code) {
        telegramClient.updateConfirmationCode(code);
    }

    @PostMapping(value = "/password/{passwordValue}")
    public void updatePassword(@PathVariable("passwordValue") String password) {
        telegramClient.updatePassword(password);
    }

    @GetMapping(value = "/status")
    public String authorizationStatus() {
        return telegramClient.haveAuthorization() ? "AUTHORIZED" : "NOT_AUTHORIZED";
    }

}
