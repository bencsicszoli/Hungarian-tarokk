package com.codecool.tarokkgame.controller.restcontroller;

import com.codecool.tarokkgame.model.dto.restdto.*;
import com.codecool.tarokkgame.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String , String>> addNewPlayer(@RequestBody RegisterDTO request) {
        Map<String , String> response = authService.createPlayer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public JwtResponseDTO authenticateUser(@RequestBody LoginDTO request) {
        System.out.println("LOGIN ENDPOINT HIT");
        return authService.loginUser(request);
    }

    @GetMapping("/me")
    public UserDTO me() {
        return authService.getMe();
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Map<String, String> deleteMe() {
        return authService.deleteMe();
    }

    @PatchMapping("/me")
    public Map<String, String> editCredentials(@RequestBody EditCredentialsDTO request) {
        return authService.editCredentials(request);
    }
}

