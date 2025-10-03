package org.orbitalLogistic.controllers;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.orbitalLogistic.dto.request.LoginRequestDTO;
import org.orbitalLogistic.dto.response.AuthResponseDTO;
import org.orbitalLogistic.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
