package com.ecofocus.repository;

import com.ecofocus.entity.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserItemRepository extends JpaRepository<UserItem, Long> {

    List<UserItem> findByUserId(Long userId);

    Optional<UserItem> findByUserIdAndItemType(Long userId, String itemType);

    /** Belirli bir kullanıcının tamamladığı challenge sayısını döner */
    @Query("SELECT COUNT(c) FROM FocusChallenge c " +
           "WHERE (c.sender.id = :userId OR c.receiver.id = :userId) " +
           "AND c.status = 'COMPLETED'")
    int countCompletedChallenges(Long userId);
}
