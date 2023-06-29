package ru.practicum.ewm.location.repository;

import ru.practicum.ewm.location.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByLatAndLon(Float lat, Float lon);
}
