package com.ecofocus.repository;

import com.ecofocus.entity.TeamChallengeCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TeamChallengeCompletionRepository extends JpaRepository<TeamChallengeCompletion, Long> {
    Optional<TeamChallengeCompletion> findByTeamChallengeIdAndUserId(Long teamChallengeId, Long userId);
    int countByTeamChallengeId(Long teamChallengeId);
    void deleteByTeamChallengeId(Long teamChallengeId);

    @Query("SELECT COUNT(c) FROM TeamChallengeCompletion c WHERE c.user.id = :userId")
    int countByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(c.teamChallenge.duration), 0) FROM TeamChallengeCompletion c WHERE c.user.id = :userId")
    long sumDurationByUserId(Long userId);

    void deleteByUserId(Long userId);

    @Query("SELECT c.teamChallenge FROM TeamChallengeCompletion c WHERE c.user.id = :userId")
    java.util.List<com.ecofocus.entity.TeamChallenge> findChallengesByUserId(Long userId);
}
