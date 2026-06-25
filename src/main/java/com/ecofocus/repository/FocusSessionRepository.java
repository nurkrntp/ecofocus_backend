package com.ecofocus.repository;
import com.ecofocus.entity.FocusSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {
    @Query("SELECT s FROM FocusSession s WHERE s.user.id = :userId ORDER BY s.completedAt DESC")
    List<FocusSession> findByUserIdOrderByCompletedAtDesc(Long userId);

    /** Kullanıcının tüm odak seanslarının toplam süresi (saniye cinsinden) */
    @Query("SELECT COALESCE(SUM(s.duration), 0) FROM FocusSession s WHERE s.user.id = :userId")
    long sumDurationByUserId(Long userId);
}
