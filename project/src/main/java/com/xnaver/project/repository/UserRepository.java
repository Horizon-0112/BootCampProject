package com.xnaver.project.repository;

import com.xnaver.project.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from UserEntity u where u.email = :email")
    Optional<UserEntity> findForUpdateByEmail(@org.springframework.data.repository.query.Param("email") String email);
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByNickName(String nickName);
    boolean existsByNickNameAndIdxNot(String nickName, Long idx);
}
