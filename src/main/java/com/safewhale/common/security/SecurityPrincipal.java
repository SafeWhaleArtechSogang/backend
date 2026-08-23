package com.safewhale.common.security;

public record SecurityPrincipal(Long id, PrincipalType type, String role) {}
