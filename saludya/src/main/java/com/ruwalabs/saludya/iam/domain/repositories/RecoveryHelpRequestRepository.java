package com.ruwalabs.saludya.iam.domain.repositories;

import com.ruwalabs.saludya.iam.domain.model.entities.RecoveryHelpRequest;
import java.util.*;
public interface RecoveryHelpRequestRepository {
    RecoveryHelpRequest save(RecoveryHelpRequest request);
    Optional<RecoveryHelpRequest> lockById(UUID id);
    List<RecoveryHelpRequest> findOpen();
}
