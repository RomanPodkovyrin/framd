package dev.romanempire.framd.frame;

import dev.romanempire.framd.repository.IndexedMedia;
import dev.romanempire.framd.repository.IndexedMediaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class FrameService {

    private final IndexedMediaRepo indexedMediaRepo;

    private final Random random = new Random();

    public String getNextFrame() {
        var list = indexedMediaRepo.findAll(Sort.by(Sort.Direction.DESC, "captureTime"))
                .stream()
                .map(IndexedMedia::getPreviewPath)
                .toList();
        return list.get(random.nextInt(list.size()));
    }
}
