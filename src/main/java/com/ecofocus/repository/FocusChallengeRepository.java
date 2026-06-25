package com.ecofocus.repository;
import com.ecofocus.entity.FocusChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FocusChallengeRepository extends JpaRepository<FocusChallenge, Long> {
    @Query("SELECT c FROM FocusChallenge c WHERE c.receiver.id = :receiverId AND c.status = :status")
    List<FocusChallenge> findByReceiverIdAndStatus(Long receiverId, String status);

    @Query("SELECT c FROM FocusChallenge c WHERE c.sender.id = :senderId OR c.receiver.id = :receiverId")
    List<FocusChallenge> findBySenderIdOrReceiverId(Long senderId, Long receiverId);

    @Modifying
    @Query("DELETE FROM FocusChallenge c WHERE c.status = 'PENDING' AND c.createdAt < :limit")
    void deleteExpired(LocalDateTime limit);

    /** ACCEPTED ama 24 saat içinde tamamlanmamış challenge'ları sil */
    @Modifying
    @Query("DELETE FROM FocusChallenge c WHERE c.status = 'ACCEPTED' AND c.acceptedAt < :limit")
    void deleteExpiredAccepted(LocalDateTime limit);

    @Query("SELECT COUNT(c) FROM FocusChallenge c WHERE c.sender.id = :senderId AND c.createdAt >= :since")
    long countSentSince(Long senderId, LocalDateTime since);

    @Query("SELECT COUNT(c) FROM FocusChallenge c WHERE c.sender.id = :senderId AND c.receiver.id = :receiverId AND c.duration = :duration AND c.createdAt >= :since")
    long countSentToReceiverWithDurationSince(Long senderId, Long receiverId, Integer duration, LocalDateTime since);

    /** Kullanıcının tamamladığı challenge'lardan kazandığı toplam dakika (saniye cinsinden) */
    @Query("SELECT COALESCE(SUM(c.duration), 0) FROM FocusChallenge c WHERE c.status = 'COMPLETED' AND (c.sender.id = :userId OR c.receiver.id = :userId)")
    long sumCompletedDurationByUserId(Long userId);
}