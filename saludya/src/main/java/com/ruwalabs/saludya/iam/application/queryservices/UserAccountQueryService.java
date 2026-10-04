package com.ruwalabs.saludya.iam.application.queryservices;

import com.ruwalabs.saludya.iam.application.results.AccountProfile;
public interface UserAccountQueryService { AccountProfile getById(Long actorId, Long userId); }
