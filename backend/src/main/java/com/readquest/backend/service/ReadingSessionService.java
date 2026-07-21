package com.readquest.backend.service;

import com.readquest.backend.dto.ReadingSessionEndRequest;
import com.readquest.backend.dto.ReadingSessionStartRequest;
import com.readquest.backend.entity.ReadingSession;

public interface ReadingSessionService {
    ReadingSession startSession(Long userId, ReadingSessionStartRequest request);
    ReadingSession endSession(Long userId, ReadingSessionEndRequest request);
}
