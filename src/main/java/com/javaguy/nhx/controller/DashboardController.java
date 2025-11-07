//package com.javaguy.nhx.controller;
//
//import com.javaguy.nhx.model.dto.response.UserDashboardResponse;
//import com.javaguy.nhx.security.UserPrincipal;
//import com.javaguy.nhx.service.UserService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.tags.Tag;
//
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/dashboard")
//@RequiredArgsConstructor
//@Slf4j
//@Tag(name = "User Dashboard", description = "APIs for user dashboard data")
//public class DashboardController {
//
//    private final UserService userService;
//
//    @Operation(summary = "Get dashboard data", description = "Retrieves aggregated data for the user's dashboard, including minting activity and pending requests.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully",
//                    content = @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = UserDashboardResponse.class))),
//            @ApiResponse(responseCode = "404", description = "User not found",
//                    content = @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = Map.class))),
//            @ApiResponse(responseCode = "403", description = "Forbidden - not authenticated",
//                    content = @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = Map.class)))
//    })
//    @GetMapping
//    @PreAuthorize("isAuthenticated()")
//    public ResponseEntity<UserDashboardResponse> getDashboard(
//            @AuthenticationPrincipal UserPrincipal currentUser) {
//        UserDashboardResponse response = userService.getDashboardData(currentUser.getId());
//        return ResponseEntity.ok(response);
//    }
//}
