package com.eroom.erooja.features.auth.controller;

import com.eroom.erooja.common.constants.ErrorEnum;
import com.eroom.erooja.common.exception.EroojaException;
import com.eroom.erooja.domain.model.MemberAuth;
import com.eroom.erooja.features.auth.jwt.JwtResponse;
import com.eroom.erooja.features.auth.jwt.JwtTokenProvider;
import com.eroom.erooja.features.auth.service.MemberAuthService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.eroom.erooja.common.constants.ErrorEnum.AUTH_ACCESS_DENIED;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberAuthService memberAuthService;

    @GetMapping("/error")
    public ResponseEntity<?> error() {
        throw new EroojaException(AUTH_ACCESS_DENIED);
    }

    @GetMapping("/token/error")
    public ResponseEntity<?> tokenError(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        jwtTokenProvider.getUidFromHeader(authorizationHeader);
        throw new JwtException("알 수 없는 JWT 오류입니다.");
    }

    @GetMapping("/token/refresh")
    public ResponseEntity<?> refresh(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        String token = extractToken(authorizationHeader);

        if (token == null) {
            throw new MalformedJwtException("JWT token 이 널입니다.");
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);

        MemberAuth memberAuth = (MemberAuth) memberAuthService.loadUserByUsername(username);

        if (!jwtTokenProvider.validateToken(token, memberAuth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorEnum.JWT_EXPIRED.getErrorResponse());
        }

        return ResponseEntity.ok(
                JwtResponse.builder()
                    .token(jwtTokenProvider.generateToken(memberAuth))
                    .refreshToken(jwtTokenProvider.generateRefreshToken(memberAuth))
                    .isAdditionalInfoNeeded(memberAuth.isAdditionalInfoNeeded())
                .build());
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader.contains("Bearer ")) {
            return authorizationHeader.replace("Bearer ", "");
        } else {
            return null;
        }
    }
}