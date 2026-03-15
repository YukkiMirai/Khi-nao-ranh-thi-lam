package com.wibuneverdie.core.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request body để gán danh sách role cho một user.
 * <pre>{ "roleIds": ["ROLE_ADMIN", "ROLE_DOCTOR"] }</pre>
 */
public record UserRoleAssignRequest(
        @NotEmpty List<String> roleIds
) {}
