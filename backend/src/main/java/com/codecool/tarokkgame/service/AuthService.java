package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.constants.Role;
import com.codecool.tarokkgame.exceptionhandling.customexception.EmailAddressAlreadyExistsException;
import com.codecool.tarokkgame.exceptionhandling.customexception.NotAllowedOperationException;
import com.codecool.tarokkgame.exceptionhandling.customexception.UsernameAlreadyExistsException;
import com.codecool.tarokkgame.model.dto.restdto.*;
import com.codecool.tarokkgame.model.entity.AppUser;
import com.codecool.tarokkgame.model.entity.Player;
import com.codecool.tarokkgame.repository.AppUserRepository;
import com.codecool.tarokkgame.security.jwt.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder encoder, AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.appUserRepository = appUserRepository;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }


    public Map<String, String> createPlayer(RegisterDTO request) {
        if (appUserRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }
        if (appUserRepository.existsByEmail(request.email())) {
            throw new EmailAddressAlreadyExistsException(request.email());
        }
        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setPassword(encoder.encode(request.password()));
        user.setEmail(request.email());
        user.setRoles(EnumSet.of(Role.ROLE_USER));
        appUserRepository.save(user);
        Map<String, String> result = new HashMap<>();
        result.put("message", String.format("Player '%s' created successfully", request.username()));
        return result;
    }

    public JwtResponseDTO loginUser(LoginDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        User userDetails = (User) authentication.getPrincipal();
        assert userDetails != null;

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        return new JwtResponseDTO(jwt, userDetails.getUsername(), roles);
    }

    public UserDTO getMe() {
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getPrincipal();
        assert user != null;
        AppUser currentPlayer = appUserRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new NoSuchElementException(String.format("Player '%s' not found", user.getUsername())));
        return new UserDTO(currentPlayer.getUsername(), currentPlayer.getBalance(), currentPlayer.getGames());
    }

    public Map<String, String> deleteMe() {
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getPrincipal();
        assert user != null;
        AppUser currentPlayer = appUserRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new NoSuchElementException(String.format("User '%s' not found", user.getUsername())));
        appUserRepository.delete(currentPlayer);
        Map<String, String> response = new HashMap<>();
        response.put("message", String.format("User '%s' has been deleted", user.getUsername()));
        return response;
    }

    public Map<String, String> editCredentials(EditCredentialsDTO request) {
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        assert user != null;
        AppUser currentPlayer = appUserRepository.findByUsername(user.getUsername()).orElseThrow(() -> new NoSuchElementException(String.format("User '%s' not found", user.getUsername())));
        Map<String, String> result = new HashMap<>();
        if (!request.username().equals(user.getUsername()) && appUserRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }
        if(!encoder.matches(request.password(), currentPlayer.getPassword())) {
            throw new NotAllowedOperationException(String.format("Invalid password for user '%s'", user.getUsername()));
        }
        currentPlayer.setUsername(request.username());
        if (!request.newPassword().isEmpty()) {
            currentPlayer.setPassword(encoder.encode(request.newPassword()));
        }
        if (!request.email().isEmpty()) {
            currentPlayer.setEmail(request.email());
        }
        appUserRepository.save(currentPlayer);

        if (request.username().equals(user.getUsername())) {
            result.put("message",String.format("User '%s' edited successfully", user.getUsername()));
        } else {
            result.put("message", String.format("User '%s' edited successfully. The new username is '%s'.", user.getUsername(), request.username()));
        }
        return result;
    }
}
