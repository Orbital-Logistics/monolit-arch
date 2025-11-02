package org.orbitalLogistic.controllers;

import org.orbitalLogistic.dto.request.RoleRequestDTO;
import org.orbitalLogistic.dto.response.RoleResponseDTO;
import org.orbitalLogistic.services.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Validated
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<RoleResponseDTO> createRole(@Valid @RequestBody RoleRequestDTO request) {
        RoleResponseDTO response = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponseDTO> getRoleById(@PathVariable Long id) {
        RoleResponseDTO role = roleService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

}