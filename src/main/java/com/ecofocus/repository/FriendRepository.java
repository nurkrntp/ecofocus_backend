package com.ecofocus.repository;
import com.ecofocus.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    @Query("SELECT f FROM Friend f WHERE f.user.id = :userId")
    List<Friend> findByUserId(Long userId);

    @Query("SELECT f FROM Friend f WHERE f.friend.id = :friendId")
    List<Friend> findByFriendId(Long friendId);

    @Query("SELECT f FROM Friend f WHERE (f.user.id = :userId AND f.friend.id = :friendId) OR (f.user.id = :friendId AND f.friend.id = :userId)")
    Optional<Friend> findByUserIdAndFriendId(Long userId, Long friendId);
}
