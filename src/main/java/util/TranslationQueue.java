package util;

import com.post.service.TranslationService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@RequiredArgsConstructor
public class TranslationQueue {
    private final TranslationService translationService;
    private final BlockingQueue<TranslationJob> queue = new LinkedBlockingQueue<>();

    @PostConstruct
    public void startWorker() { // 큐 작업
        new Thread(() -> {
            while (true) {
                try {
                    TranslationJob job = queue.take(); // 대기
                    //translationService.handleJob(job);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void enqueue(TranslationJob job) {
        queue.offer(job);
    }
}
