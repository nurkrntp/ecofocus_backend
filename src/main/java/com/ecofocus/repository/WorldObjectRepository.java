package com.ecofocus.repository;

import com.ecofocus.entity.WorldObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorldObjectRepository extends JpaRepository<WorldObject, Long> {
}
