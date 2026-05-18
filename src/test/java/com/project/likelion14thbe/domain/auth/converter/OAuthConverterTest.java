package com.project.likelion14thbe.domain.auth.converter;

import com.project.likelion14thbe.domain.auth.dto.oauth.OAuthUserInfo;
import com.project.likelion14thbe.domain.auth.entity.SocialAccount;
import com.project.likelion14thbe.domain.auth.enums.Provider;
import com.project.likelion14thbe.domain.member.entity.Member;
import com.project.likelion14thbe.domain.member.enums.Role;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OAuthConverterTest {

    @Test
    void toMember_nickname이_있으면_name에_nickname을_쓴다() {
        OAuthUserInfo info = OAuthUserInfo.builder()
                .provider(Provider.KAKAO).providerId("123")
                .email("user@kakao.com").nickname("규언").profileImage("img").build();

        Member member = OAuthConverter.toMember(info, "ENCODED_PW");

        assertThat(member.getName()).isEqualTo("규언");
        assertThat(member.getEmail()).isEqualTo("user@kakao.com");
        assertThat(member.getPassword()).isEqualTo("ENCODED_PW");
        assertThat(member.getProfileImage()).isEqualTo("img");
        assertThat(member.getRole()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    void toMember_nickname이_null이면_email_로컬파트를_name으로_쓴다() {
        OAuthUserInfo info = OAuthUserInfo.builder()
                .provider(Provider.NAVER).providerId("hash")
                .email("abc@naver.com").nickname(null).profileImage(null).build();

        Member member = OAuthConverter.toMember(info, "ENCODED_PW");

        assertThat(member.getName()).isEqualTo("abc");
    }

    @Test
    void toSocialAccount_provider와_providerId와_member를_매핑한다() {
        OAuthUserInfo info = OAuthUserInfo.builder()
                .provider(Provider.KAKAO).providerId("123")
                .email("user@kakao.com").nickname("규언").profileImage("img").build();
        Member member = OAuthConverter.toMember(info, "ENCODED_PW");

        SocialAccount sa = OAuthConverter.toSocialAccount(info, member);

        assertThat(sa.getProvider()).isEqualTo(Provider.KAKAO);
        assertThat(sa.getProviderId()).isEqualTo("123");
        assertThat(sa.getMember()).isSameAs(member);
    }
}
