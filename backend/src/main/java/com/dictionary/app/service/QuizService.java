package com.dictionary.app.service;

import com.dictionary.app.dto.response.QuizQuestionResponse;
import com.dictionary.app.entity.Meaning;
import com.dictionary.app.entity.User;
import com.dictionary.app.entity.Word;
import com.dictionary.app.exception.BadRequestException;
import com.dictionary.app.repository.FavoriteRepository;
import com.dictionary.app.repository.FlashcardProgressRepository;
import com.dictionary.app.repository.WordRepository;
import com.dictionary.app.security.CurrentUserProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sinh quiz trắc nghiệm hàng ngày: ưu tiên các từ đến hạn ôn tập (flashcard),
 * sau đó tới từ yêu thích, cuối cùng mới lấy ngẫu nhiên từ toàn bộ từ điển.
 *
 * Lưu ý: vì đây là từ điển học tập quy mô vừa/nhỏ nên việc lấy toàn bộ danh sách từ
 * để chọn ngẫu nhiên trong bộ nhớ là chấp nhận được; nếu từ điển lên tới hàng trăm nghìn
 * từ, nên thay bằng truy vấn lấy ngẫu nhiên ở tầng database.
 */
@Service
public class QuizService {

    private final WordRepository wordRepository;
    private final FlashcardProgressRepository flashcardProgressRepository;
    private final FavoriteRepository favoriteRepository;
    private final CurrentUserProvider currentUserProvider;

    private static final int OPTIONS_PER_QUESTION = 4;

    public QuizService(WordRepository wordRepository, FlashcardProgressRepository flashcardProgressRepository,
                        FavoriteRepository favoriteRepository, CurrentUserProvider currentUserProvider) {
        this.wordRepository = wordRepository;
        this.flashcardProgressRepository = flashcardProgressRepository;
        this.favoriteRepository = favoriteRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public List<QuizQuestionResponse> generateDaily(int count) {
        User user = currentUserProvider.getCurrentUser();

        LinkedHashSet<Word> pool = new LinkedHashSet<>();

        flashcardProgressRepository.findDue(user, LocalDate.now(), PageRequest.of(0, count))
                .forEach(p -> pool.add(p.getWord()));

        if (pool.size() < count) {
            favoriteRepository.findByUserOrderByCreatedAtDesc(user)
                    .forEach(f -> pool.add(f.getWord()));
        }

        List<Word> allWords = wordRepository.findAll();
        if (allWords.size() < 2) {
            throw new BadRequestException("Từ điển cần có ít nhất 2 từ để tạo quiz");
        }

        if (pool.size() < count) {
            List<Word> shuffled = new ArrayList<>(allWords);
            Collections.shuffle(shuffled);
            for (Word w : shuffled) {
                if (pool.size() >= count) break;
                pool.add(w);
            }
        }

        List<Word> questionWords = new ArrayList<>(pool);
        Collections.shuffle(questionWords);
        questionWords = questionWords.stream()
                .filter(w -> !w.getMeanings().isEmpty())
                .limit(count)
                .collect(Collectors.toList());

        // Ngân hàng đáp án nhiễu: toàn bộ nghĩa tiếng Việt có trong từ điển
        List<String> allMeaningsPool = allWords.stream()
                .flatMap(w -> w.getMeanings().stream())
                .map(Meaning::getVietnamese)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Random random = new Random();
        List<QuizQuestionResponse> questions = new ArrayList<>();

        for (Word word : questionWords) {
            String correctAnswer = word.getMeanings().get(0).getVietnamese();

            Set<String> options = new LinkedHashSet<>();
            options.add(correctAnswer);

            List<String> distractorCandidates = new ArrayList<>(allMeaningsPool);
            Collections.shuffle(distractorCandidates, random);
            for (String candidate : distractorCandidates) {
                if (options.size() >= OPTIONS_PER_QUESTION) break;
                if (!candidate.equalsIgnoreCase(correctAnswer)) {
                    options.add(candidate);
                }
            }

            List<String> optionList = new ArrayList<>(options);
            Collections.shuffle(optionList, random);
            int correctIndex = optionList.indexOf(correctAnswer);

            questions.add(new QuizQuestionResponse(
                    word.getId(),
                    word.getEnglish(),
                    word.getPhonetic(),
                    optionList,
                    correctIndex
            ));
        }

        return questions;
    }
}
