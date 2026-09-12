package com.dictionary.app.config;

import com.dictionary.app.entity.*;
import com.dictionary.app.repository.UserRepository;
import com.dictionary.app.repository.WordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WordRepository wordRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, WordRepository wordRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.wordRepository = wordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedWords();
        backfillTopics();
    }

    private void seedAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setFullName("Quản trị viên");
            admin.setUsername("admin");
            admin.setEmail("admin@dictionary.local");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            System.out.println(">>> Đã tạo tài khoản admin mặc định: admin / admin123");
        }
    }

    private void seedWords() {
        if (wordRepository.count() > 0) return;

        List<Word> sampleWords = List.of(
                buildWord("hello", "/həˈloʊ/", "A1", "Daily English", List.of(
                        m("exclamation", "xin chào", "Hello, how are you?", "Xin chào, bạn khỏe không?", "hi, greetings", 1)
                )),
                buildWord("dictionary", "/ˈdɪkʃəneri/", "A2", "Education", List.of(
                        m("noun", "từ điển", "I looked up the word in a dictionary.", "Tôi đã tra từ này trong từ điển.", "lexicon", 1)
                )),
                buildWord("beautiful", "/ˈbjuːtɪfl/", "A2", "Daily English", List.of(
                        m("adjective", "xinh đẹp, tuyệt đẹp", "She has a beautiful smile.", "Cô ấy có nụ cười tuyệt đẹp.", "pretty, gorgeous, lovely", 1)
                )),
                buildWord("achieve", "/əˈtʃiːv/", "B1", "Work & Career", List.of(
                        m("verb", "đạt được, hoàn thành", "He achieved his goal after years of hard work.", "Anh ấy đã đạt được mục tiêu sau nhiều năm nỗ lực.", "accomplish, attain", 1)
                )),
                buildWord("environment", "/ɪnˈvaɪrənmənt/", "B1", "Environment", List.of(
                        m("noun", "môi trường", "We must protect the environment.", "Chúng ta phải bảo vệ môi trường.", "surroundings, ecosystem", 1)
                )),
                buildWord("challenge", "/ˈtʃælɪndʒ/", "B1", "Personal Development", List.of(
                        m("noun", "thử thách, thách thức", "Learning a new language is a big challenge.", "Học một ngôn ngữ mới là một thử thách lớn.", "difficulty, obstacle", 1),
                        m("verb", "thách thức", "She challenged him to a race.", "Cô ấy thách anh ấy một cuộc đua.", "dare, defy", 2)
                )),
                buildWord("resilient", "/rɪˈzɪliənt/", "C1", "Personal Development", List.of(
                        m("adjective", "kiên cường, có khả năng phục hồi", "Children are often remarkably resilient.", "Trẻ em thường có khả năng phục hồi đáng kinh ngạc.", "tough, adaptable", 1)
                )),
                buildWord("ambitious", "/æmˈbɪʃəs/", "B2", "Work & Career", List.of(
                        m("adjective", "đầy tham vọng", "She is an ambitious young entrepreneur.", "Cô ấy là một doanh nhân trẻ đầy tham vọng.", "aspiring, driven", 1)
                )),
                buildWord("appreciate", "/əˈpriːʃieɪt/", "B2", "Communication", List.of(
                        m("verb", "trân trọng, đánh giá cao", "I really appreciate your help.", "Tôi thực sự trân trọng sự giúp đỡ của bạn.", "value, treasure", 1)
                )),
                buildWord("opportunity", "/ˌɒpərˈtjuːnəti/", "B1", "Work & Career", List.of(
                        m("noun", "cơ hội", "This job is a great opportunity for you.", "Công việc này là một cơ hội tuyệt vời cho bạn.", "chance, opening", 1)
                ))
        );

        wordRepository.saveAll(sampleWords);
        System.out.println(">>> Đã seed " + sampleWords.size() + " từ vựng mẫu.");
    }

    private void backfillTopics() {
        var topicMap = java.util.Map.ofEntries(
                java.util.Map.entry("hello", "Daily English"),
                java.util.Map.entry("dictionary", "Education"),
                java.util.Map.entry("beautiful", "Daily English"),
                java.util.Map.entry("achieve", "Work & Career"),
                java.util.Map.entry("environment", "Environment"),
                java.util.Map.entry("challenge", "Personal Development"),
                java.util.Map.entry("resilient", "Personal Development"),
                java.util.Map.entry("ambitious", "Work & Career"),
                java.util.Map.entry("appreciate", "Communication"),
                java.util.Map.entry("opportunity", "Work & Career")
        );
        topicMap.forEach((english, topic) -> wordRepository.findByEnglishIgnoreCase(english).ifPresent(word -> {
            if (word.getTopic() == null || word.getTopic().isBlank()) {
                word.setTopic(topic);
                wordRepository.save(word);
            }
        }));
    }

    private Word buildWord(String english, String phonetic, String level, String topic, List<Meaning> meanings) {
        Word word = new Word();
        word.setEnglish(english);
        word.setPhonetic(phonetic);
        word.setLevel(level);
        word.setTopic(topic);
        word.setViewCount(0L);
        meanings.forEach(mn -> mn.setWord(word));
        word.setMeanings(new java.util.ArrayList<>(meanings));
        return word;
    }

    private Meaning m(String pos, String vi, String exEn, String exVi, String synonyms, int order) {
        Meaning meaning = new Meaning();
        meaning.setPartOfSpeech(pos);
        meaning.setVietnamese(vi);
        meaning.setExampleEn(exEn);
        meaning.setExampleVi(exVi);
        meaning.setSynonyms(synonyms);
        meaning.setOrderIndex(order);
        return meaning;
    }
}
