package com.project.likelion14thbe.domain.member.service.query;

import com.project.likelion14thbe.domain.member.dto.response.MemberResDTO;
import org.jspecify.annotations.Nullable;

public interface MemberQueryService {

    MemberResDTO.@Nullable MemberPreviewResDTO getMember(Long id);

    MemberResDTO.MemberPreviewResDTO getMyInfo(Long memberId);
}
