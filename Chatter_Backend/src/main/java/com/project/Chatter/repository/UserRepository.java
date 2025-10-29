package com.project.Chatter.repository;

import com.project.Chatter.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.project.Chatter.entity.Status;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.username = :username")
    void updateStatusByUsername(@Param("username") String username, @Param("status") Status status);
}
