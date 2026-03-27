package com.project.likelion14thbe.domain.review.dto.request;

import lombok.Getter;
import lombok.Setter;

public class ReviewReqDTO {

    public record Test1DTO (
            Long id,
            String content
    ) {
    }

    @Getter
    @Setter
    public class Test2DTO {
        private Long id;
        private String content;
    }
}
