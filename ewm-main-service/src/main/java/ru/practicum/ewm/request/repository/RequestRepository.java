package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.StatusRequest;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    Request findByEventIdAndRequesterId(Long eventId, Long userId);

    List<Request> findAllByRequesterId(Long userId);

    Optional<Request> findByIdAndRequesterId(Long requestId, Long userId);

    List<Request> findAllByEventIdAndEventInitiatorId(Long eventId, Long userId);

    Integer countParticipationByEventIdAndStatus(Long eventId, StatusRequest confirmed);

    Optional<Request> findByIdAndEvent_Id(Long id, Long eventId);

}
