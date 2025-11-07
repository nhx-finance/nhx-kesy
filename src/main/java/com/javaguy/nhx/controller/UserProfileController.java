package com.javaguy.nhx.controller;

import com.javaguy.nhx.model.dto.request.UserProfileRequest;
import com.javaguy.nhx.model.dto.response.UserProfileResponse;
import com.javaguy.nhx.security.UserPrincipal;
import com.javaguy.nhx.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Manage user profile details")
public class UserProfileController {

    private final UserProfileService userProfileService;

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
        UserProfileResponse response = userProfileService.saveProfile(currentUser.getId(), request);
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
        UserProfileResponse response = userProfileService.getProfile(currentUser.getId());
        return ResponseEntity.ok(response);
    }
}
