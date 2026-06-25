package com.ecofocus.repository;

import com.ecofocus.entity.TeamChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TeamChallengeRepository extends JpaRepository<TeamChallenge, Long> {
    List<TeamChallenge> findByTeamId(Long teamId);
}
