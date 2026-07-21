package com.readquest.backend.repository;

import com.readquest.backend.entity.ReaderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReaderTypeRepository extends JpaRepository<ReaderType, Long> {
    Optional<ReaderType> findByCode(String code);
}
