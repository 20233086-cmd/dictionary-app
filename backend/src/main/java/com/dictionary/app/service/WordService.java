package com.dictionary.app.service;

import com.dictionary.app.dto.request.MeaningRequest;
import com.dictionary.app.dto.request.WordRequest;
import com.dictionary.app.dto.response.WordResponse;
import com.dictionary.app.entity.Meaning;
import com.dictionary.app.entity.Word;
import com.dictionary.app.exception.BadRequestException;
import com.dictionary.app.exception.ResourceNotFoundException;
import com.dictionary.app.repository.WordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WordService {

    private final WordRepository wordRepository;

    public WordService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    @Transactional
    public WordResponse lookup(String english) {
        Word word = wordRepository.findByEnglishIgnoreCase(english.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy từ \"" + english + "\" trong từ điển"));
        word.setViewCount(word.getViewCount() == null ? 1 : word.getViewCount() + 1);
        wordRepository.save(word);
        return WordResponse.fromEntity(word);
    }

    @Transactional(readOnly = true)
    public WordResponse getById(Long id) {
        Word word = wordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy từ vựng"));
        return WordResponse.fromEntity(word);
    }

    @Transactional(readOnly = true)
    public Page<WordResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("english").ascending());
        Page<Word> result = (keyword == null || keyword.isBlank())
                ? wordRepository.findAll(pageable)
                : wordRepository.findByEnglishContainingIgnoreCase(keyword.trim(), pageable);
        return result.map(WordResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<WordResponse> reverseSearch(String vietnameseKeyword, int page, int size) {
        if (vietnameseKeyword == null || vietnameseKeyword.isBlank()) {
            throw new BadRequestException("Vui lòng nhập từ khoá tiếng Việt cần tra");
        }
        Pageable pageable = PageRequest.of(page, size);
        return wordRepository.searchByVietnameseMeaning(vietnameseKeyword.trim(), pageable)
                .map(WordResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public List<String> suggest(String prefix) {
        if (prefix == null || prefix.isBlank()) return List.of();
        return wordRepository.findSuggestions(prefix.trim(), PageRequest.of(0, 8))
                .stream()
                .map(Word::getEnglish)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public WordResponse wordOfTheDay() {
        Word word = wordRepository.findRandomWord();
        if (word == null) throw new ResourceNotFoundException("Từ điển hiện chưa có dữ liệu");
        return WordResponse.fromEntity(word);
    }

    @Transactional(readOnly = true)
    public List<WordResponse> mostViewed(int limit) {
        return wordRepository.findTopViewed(PageRequest.of(0, limit))
                .stream().map(WordResponse::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> topics() {
        return wordRepository.findDistinctTopics();
    }

    @Transactional(readOnly = true)
    public Page<WordResponse> searchByTopic(String topic, int page, int size) {
        if (topic == null || topic.isBlank()) throw new BadRequestException("Chủ đề không hợp lệ");
        return wordRepository.findByTopicIgnoreCase(topic.trim(), PageRequest.of(page, size, Sort.by("english").ascending()))
                .map(WordResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public long countWords() {
        return wordRepository.count();
    }

    // ---------- ADMIN CRUD ----------

    @Transactional
    public WordResponse createWord(WordRequest request) {
        wordRepository.findByEnglishIgnoreCase(request.getEnglish()).ifPresent(w -> {
            throw new BadRequestException("Từ \"" + request.getEnglish() + "\" đã tồn tại trong từ điển");
        });

        Word word = new Word();
        word.setEnglish(request.getEnglish().trim());
        word.setPhonetic(request.getPhonetic());
        word.setLevel(request.getLevel());
        word.setTopic(request.getTopic());
        word.setViewCount(0L);
        word.setMeanings(new ArrayList<>());

        attachMeanings(word, request.getMeanings());

        Word saved = wordRepository.save(word);
        return WordResponse.fromEntity(saved);
    }

    @Transactional
    public WordResponse updateWord(Long id, WordRequest request) {
        Word word = wordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy từ vựng"));

        word.setEnglish(request.getEnglish().trim());
        word.setPhonetic(request.getPhonetic());
        word.setLevel(request.getLevel());
        word.setTopic(request.getTopic());

        word.getMeanings().clear();
        attachMeanings(word, request.getMeanings());

        Word saved = wordRepository.save(word);
        return WordResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteWord(Long id) {
        if (!wordRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy từ vựng");
        }
        wordRepository.deleteById(id);
    }

    private void attachMeanings(Word word, List<MeaningRequest> meaningRequests) {
        if (meaningRequests == null || meaningRequests.isEmpty()) {
            throw new BadRequestException("Từ vựng cần ít nhất một nghĩa");
        }
        int order = 1;
        for (MeaningRequest mr : meaningRequests) {
            Meaning meaning = new Meaning();
            meaning.setWord(word);
            meaning.setPartOfSpeech(mr.getPartOfSpeech());
            meaning.setVietnamese(mr.getVietnamese());
            meaning.setExampleEn(mr.getExampleEn());
            meaning.setExampleVi(mr.getExampleVi());
            meaning.setSynonyms(mr.getSynonyms());
            meaning.setOrderIndex(mr.getOrderIndex() != null ? mr.getOrderIndex() : order);
            word.getMeanings().add(meaning);
            order++;
        }
    }
}
