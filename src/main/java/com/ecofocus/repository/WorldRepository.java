package com.ecofocus.repository;
import com.ecofocus.entity.World;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WorldRepository extends JpaRepository<World, Long> {
    @Query("SELECT w FROM World w WHERE w.user.id = :userId")
    Optional<World> findByUserId(Long userId);
}
