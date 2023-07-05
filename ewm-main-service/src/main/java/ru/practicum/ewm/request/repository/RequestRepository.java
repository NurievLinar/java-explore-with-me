package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.event.model.State;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.user.model.User;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByRequester(User user);

    List<Request> findByEventId(Long eventId);

    //List<Request> findAllByEventId(Long eventId);

    Long countByEventIdAndStatus(Long id, State confirmed);
}
