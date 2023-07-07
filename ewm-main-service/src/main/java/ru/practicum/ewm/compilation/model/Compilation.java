package ru.practicum.ewm.compilation.model;

import lombok.*;
import ru.practicum.ewm.event.model.Event;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Compilation")
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "pinned")
    private Boolean pinned;
    @Column(name = "title")
    private String title;
    @ManyToMany
    @JoinTable(name = "Compilations_Events",
            joinColumns = {@JoinColumn(name = "compilation_id")},
            inverseJoinColumns = {@JoinColumn(name = "event_id")})
    Set<Event> events;
}
