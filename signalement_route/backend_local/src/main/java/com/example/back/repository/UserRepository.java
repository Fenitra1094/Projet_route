package com.example.back.repository;


import com.example.back.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Méthode personnalisée pour login
    User findByEmailAndPassword(String email, String password);

    // Optionnel : pour vérifier si l’email existe lors de l’inscription
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
    // find users by status_blocage id (e.g., 2 = bloqué)
    @org.springframework.data.jpa.repository.Query("select u from User u where u.idStatusBlocage = :statusId")
    java.util.List<User> findByStatusBlocage(@org.springframework.data.repository.query.Param("statusId") Integer statusId);

}