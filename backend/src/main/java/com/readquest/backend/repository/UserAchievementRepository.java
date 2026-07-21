package com.readquest.backend.repository;

import com.readquest.backend.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUserId(Long userId);
    
    boolean existsByUserIdAndAchievementId(Long userId, Long achievementId);
    
    @Query("SELECT COUNT(ua) > 0 FROM UserAchievement ua WHERE ua.user.id = :userId AND ua.achievement.code = :code")
    boolean existsByUserIdAndAchievementCode(@Param("userId") Long userId, @Param("code") String code);
}
