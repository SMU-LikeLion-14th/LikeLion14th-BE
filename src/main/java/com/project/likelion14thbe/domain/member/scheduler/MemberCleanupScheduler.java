package com.project.likelion14thbe.domain.member.scheduler;

import com.project.likelion14thbe.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberCleanupScheduler {

    private final MemberRepository memberRepository;

    // 매일 새벽 세시에
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void deleteExpiredMembers() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        int deletedCount = memberRepository.deleteByDeletedAtBefore(thirtyDaysAgo);

        if (deletedCount > 0) {
            log.info("30일 이상 지난 회원 {}명 영구 삭제 완료", deletedCount);
        }
    }
}
