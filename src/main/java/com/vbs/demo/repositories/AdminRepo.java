package com.vbs.demo.repositories;

import com.vbs.demo.models.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepo extends JpaRepository<Admin,Integer> {
    Admin findByUsername(String username);

    boolean existsByUsername(String username);
}
