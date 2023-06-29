package ru.practicum.ewm.compilation.repository;

import ru.practicum.ewm.compilation.model.Compilation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query("select c from Compilation c " +
            "where (:pinned is null or c.pinned = :pinned)")
    List<Compilation> findByPinned(@Param("pinned") Boolean pinned, Pageable pageable);
}