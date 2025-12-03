package com.chating.scheduler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.chating.entity.chat.Chat;
import com.chating.entity.chat.State;
import com.chating.repository.chat.ChatRepository;
import com.chating.util.file.S3FileUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class Scheduler {

    private final ChatRepository chatRepository;
    private final S3FileUtil s3FileUtil;

    @Scheduled(cron = "0 0 3 ? * SUN")
    @Transactional
    public void deleteOldFiles() {

        System.out.println("스케줄러 실행: 오래된 파일 삭제 시작");
        
        LocalDateTime limit = LocalDateTime.now().minusDays(30);
        List<Chat> deletedFiles = chatRepository.findOldFiles(limit);
        List<Long> deleteIds=new ArrayList<Long>();
        for (Chat chat : deletedFiles) {
            String url = chat.getChatFile().getUrl();

            if (url != null) {
                s3FileUtil.delete(url); // S3에서 삭제
                deleteIds.add(chat.getChatId());
            }
        }
        chatRepository.deleteChatBatch(deleteIds,State.DELETED);
        System.out.println("스케줄러 종료: 파일 정리 완료");
    }
}
