package com.nutrifit.repository;

import com.nutrifit.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Kullanıcı adına göre bir kullanıcı bul
    Optional<User> findByUsername(String username);

    // Bu kullanıcı adı veritabanında var mı?
    Boolean existsByUsername(String username);

    // Bu email veritabanında var mı?
    Boolean existsByEmail(String email);
}