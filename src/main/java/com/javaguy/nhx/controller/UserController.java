package com.javaguy.nhx.controller;

import com.javaguy.nhx.model.dto.request.DetailsRequest;
import com.javaguy.nhx.model.dto.request.UserProfileRequest;
import com.javaguy.nhx.model.dto.response.UserProfileResponse;
import com.javaguy.nhx.security.UserPrincipal;
import com.javaguy.nhx.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for user details management")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Submit user details", description = "Allows an authenticated user to submit their personal details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Details saved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))), 
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - not authenticated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/details")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> submitDetails(
            @Valid @RequestBody DetailsRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        userService.submitDetails(currentUser.getId(), request);
        return ResponseEntity.ok(Map.of("message", "Details saved"));
    }

    @Operation(summary = "Complete user profile details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile details saved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileResponse.class)))
    })
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> saveProfile(@Valid @RequestBody UserProfileRequest request,
                                         @AuthenticationPrincipal UserPrincipal currentUser) {
        UserProfileResponse response = userService.saveProfile(currentUser.getId(), request);
        return ResponseEntity.ok(
                java.util.Map.of(
                        "message", "Profile details saved successfully",
                        "profileComplete", response.isProfileComplete()
                )
        );
    }

    @Operation(summary = "Retrieve user profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileResponse.class)))
    })
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserProfileResponse response = userService.getProfile(currentUser.getId());
        return ResponseEntity.ok(response);
    }
}
