package com.dictionary.app.repository;

import com.dictionary.app.entity.Meaning;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeaningRepository extends JpaRepository<Meaning, Long> {
}
