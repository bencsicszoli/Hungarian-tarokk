package com.codecool.tarokkgame.controller.restcontroller;

import com.codecool.tarokkgame.model.dto.LocalizedMessage;
import com.codecool.tarokkgame.model.dto.restdto.*;
import com.codecool.tarokkgame.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<LocalizedMessage> addNewPlayer(@RequestBody RegisterDTO request) {
        LocalizedMessage response = authService.createPlayer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public JwtResponseDTO authenticateUser(@RequestBody LoginDTO request) {
        return authService.loginUser(request);
    }

    @GetMapping("/me")
    public UserDTO me() {
        return authService.getMe();
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public LocalizedMessage deleteMe() {
        return authService.deleteMe();
    }

    @PatchMapping("/me")
    public LocalizedMessage editCredentials(@RequestBody EditCredentialsDTO request) {
        return authService.editCredentials(request);
    }

    @GetMapping("/customId")
    public long getCustomId() {
        return authService.getCustomGameId();
    }
}

