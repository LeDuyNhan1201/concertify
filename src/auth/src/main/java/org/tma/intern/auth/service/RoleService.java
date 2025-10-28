package org.tma.intern.auth.service;

import io.smallrye.mutiny.Uni;
import org.tma.intern.auth.dto.request.RoleRequest;

public interface RoleService {

    Uni<String> assignClientRole(RoleRequest.ClientRoleInfo request);

}
