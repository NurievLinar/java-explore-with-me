package ru.practicum.ewm.location.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.location.model.Location;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByLatAndLon(Float lat, Float lon);
}
