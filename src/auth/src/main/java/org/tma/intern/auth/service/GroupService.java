package org.tma.intern.auth.service;

import io.smallrye.mutiny.Uni;
import org.tma.intern.auth.dto.request.GroupRequest;

public interface GroupService {

    Uni<String> createGroup(GroupRequest.GroupCreation request);

}
