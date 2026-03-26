package com.project.likelion14thbe.domain.member.dto.request;

import lombok.Getter;
import lombok.Setter;

public class MemberReqDTO {

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
