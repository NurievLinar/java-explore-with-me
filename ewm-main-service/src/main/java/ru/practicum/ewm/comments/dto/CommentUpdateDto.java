package ru.practicum.ewm.comments.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentUpdateDto {

    @NotBlank
    private String text;
}
