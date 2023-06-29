package ru.practicum.ewm.request.repository;

import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByRequester(User user);

    List<Request> findByEventId(Long eventId);
}
