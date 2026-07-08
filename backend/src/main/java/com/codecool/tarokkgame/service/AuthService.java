package com.codecool.tarokkgame.service;

import com.codecool.tarokkgame.constants.MessageKey;
import com.codecool.tarokkgame.constants.Role;
import com.codecool.tarokkgame.exceptionhandling.customexception.EmailAddressAlreadyExistsException;
import com.codecool.tarokkgame.exceptionhandling.customexception.NotAllowedOperationException;
import com.codecool.tarokkgame.exceptionhandling.customexception.UsernameAlreadyExistsException;
import com.codecool.tarokkgame.model.dto.LocalizedMessage;
import com.codecool.tarokkgame.model.dto.restdto.*;
import com.codecool.tarokkgame.model.entity.AppUser;
import com.codecool.tarokkgame.model.entity.Game;
import com.codecool.tarokkgame.model.entity.Player;
import com.codecool.tarokkgame.repository.AppUserRepository;
import com.codecool.tarokkgame.repository.GameRepository;
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
    private final GameRepository gameRepository;

    public AuthService(AppUserRepository appUserRepository, PasswordEncoder encoder, AuthenticationManager authenticationManager, JwtUtils jwtUtils, GameRepository gameRepository) {
        this.appUserRepository = appUserRepository;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.gameRepository = gameRepository;
    }

    public LocalizedMessage createPlayer(RegisterDTO request) {
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
        return new LocalizedMessage(MessageKey.AUTH_PLAYER_CREATED, Map.of("username", request.username()));
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

    public LocalizedMessage deleteMe() {
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext()
                .getAuthentication()).getPrincipal();
        assert user != null;
        AppUser currentPlayer = appUserRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new NoSuchElementException(String.format("User '%s' not found", user.getUsername())));
        appUserRepository.delete(currentPlayer);
        return new LocalizedMessage(MessageKey.AUTH_USER_DELETED, Map.of("username", user.getUsername()));
    }

    public LocalizedMessage editCredentials(EditCredentialsDTO request) {
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        assert user != null;
        AppUser currentPlayer = appUserRepository.findByUsername(user.getUsername()).orElseThrow(() -> new NoSuchElementException(String.format("User '%s' not found", user.getUsername())));
        if (!request.username().equals(user.getUsername()) && appUserRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }
        if(!encoder.matches(request.password(), currentPlayer.getPassword())) {
            throw new NotAllowedOperationException(new LocalizedMessage(MessageKey.ERROR_INVALID_PASSWORD, Map.of("username", user.getUsername())));
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
            return new LocalizedMessage(MessageKey.AUTH_CREDENTIALS_EDITED, Map.of("username", user.getUsername()));
        }
        return new LocalizedMessage(MessageKey.AUTH_CREDENTIALS_EDITED_WITH_NEW_USERNAME,
                Map.of("username", user.getUsername(), "newUsername", request.username()));
    }

    public long getCustomGameId() {
        Game game = new Game();
        game.setPrivateGame(true);
        Game savedGame = gameRepository.save(game);
        return savedGame.getId();
    }
}
