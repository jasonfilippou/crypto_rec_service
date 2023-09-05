package com.xm.cryptorecservice.persistence;

import com.xm.cryptorecservice.model.user.UserEntity;
import com.xm.cryptorecservice.util.logger.Logged;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * A {@link JpaRepository} used to persist {@link UserEntity} instances.
 *
 * @author jason
 */
@Logged
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Derived query method used to find a {@link UserEntity} by their unique e-mail field.
     * @param email The unique e-mail to find the user by.
     * @return An {@link Optional} over the found {@link UserEntity} if a user by the provided e-mail 
     * is found in the DB, {@link Optional#empty()} otherwise.
     */
    Optional<UserEntity> findByEmail(String email);
}
