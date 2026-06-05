package com.ne.backend.repository;

import com.ne.backend.entity.ExampleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExampleRepository extends JpaRepository<ExampleEntity, Long> {

    Page<ExampleEntity> findAll(Pageable pageable);

    @Query("SELECT e FROM ExampleEntity e WHERE " +
           "(:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:description IS NULL OR LOWER(e.description) LIKE LOWER(CONCAT('%', :description, '%')))")
    Page<ExampleEntity> searchExamples(
            @Param("name") String name,
            @Param("description") String description,
            Pageable pageable
    );
}
