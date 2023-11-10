package com.example.spector.repositories;

import com.example.spector.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> { //Интерфейс в котором
                                                                    //происходит обращение к базе данных
    Optional<User> findByUsername(String username);
}
