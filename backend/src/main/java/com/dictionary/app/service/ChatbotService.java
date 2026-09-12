package com.dictionary.app.service;

import com.dictionary.app.dto.request.ChatRequest;
import com.dictionary.app.dto.response.ChatResponse;
import com.dictionary.app.entity.ChatConversation;
import com.dictionary.app.entity.ChatMessage;
import com.dictionary.app.entity.User;
import com.dictionary.app.exception.ResourceNotFoundException;
import com.dictionary.app.repository.ChatConversationRepository;
import com.dictionary.app.repository.ChatMessageRepository;
import com.dictionary.app.repository.WordRepository;
import com.dictionary.app.security.CurrentUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ChatbotService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final CurrentUserProvider currentUserProvider;
    private final GeminiClient geminiClient;
    private final WordRepository wordRepository;

    private static final int MAX_HISTORY_TURNS = 12;
    private static final Pattern WORD_PATTERN = Pattern.compile("[A-Za-z]+(?:'[A-Za-z]+)?");
    private static final int MIN_LINKABLE_LENGTH = 3;

    public ChatbotService(ChatConversationRepository conversationRepository, ChatMessageRepository messageRepository,
                           CurrentUserProvider currentUserProvider, GeminiClient geminiClient,
                           WordRepository wordRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.currentUserProvider = currentUserProvider;
        this.geminiClient = geminiClient;
        this.wordRepository = wordRepository;
    }

    @Transactional
    public ChatResponse sendMessage(ChatRequest request) {
        User user = currentUserProvider.getCurrentUser();

        ChatConversation conversation;
        if (request.getConversationId() != null) {
            conversation = conversationRepository.findByIdAndUser(request.getConversationId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc trò chuyện"));
        } else {
            String mode = "PRACTICE".equalsIgnoreCase(request.getMode()) ? "PRACTICE" : "QA";
            conversation = new ChatConversation();
            conversation.setUser(user);
            conversation.setTitle(buildTitle(request.getMessage()));
            conversation.setMode(mode);
            conversation = conversationRepository.save(conversation);
        }

        ChatMessage userMessage = new ChatMessage();
        userMessage.setConversation(conversation);
        userMessage.setSender(ChatMessage.Sender.USER);
        userMessage.setContent(request.getMessage());
        messageRepository.save(userMessage);

        List<ChatMessage> history = messageRepository.findByConversationOrderByCreatedAtAsc(conversation);
        List<GeminiClient.ChatTurn> turns = history.stream()
                .skip(Math.max(0, history.size() - MAX_HISTORY_TURNS))
                .map(m -> m.getSender() == ChatMessage.Sender.USER
                        ? GeminiClient.ChatTurn.user(m.getContent())
                        : GeminiClient.ChatTurn.model(m.getContent()))
                .collect(Collectors.toList());

        String rawReply = geminiClient.chat(turns, conversation.getMode());
        String linkedReply = linkifyDictionaryWords(rawReply);

        ChatMessage botMessage = new ChatMessage();
        botMessage.setConversation(conversation);
        botMessage.setSender(ChatMessage.Sender.BOT);
        botMessage.setContent(linkedReply);
        messageRepository.save(botMessage);

        return new ChatResponse(conversation.getId(), conversation.getMode(), linkedReply);
    }

    @Transactional(readOnly = true)
    public List<ChatConversation> getMyConversations() {
        User user = currentUserProvider.getCurrentUser();
        return conversationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<ChatMessage> getMessages(Long conversationId) {
        User user = currentUserProvider.getCurrentUser();
        ChatConversation conversation = conversationRepository.findByIdAndUser(conversationId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cuộc trò chuyện"));
        return messageRepository.findByConversationOrderByCreatedAtAsc(conversation);
    }

    private String buildTitle(String firstMessage) {
        if (firstMessage == null) return "Cuộc trò chuyện mới";
        String trimmed = firstMessage.trim();
        return trimmed.length() > 50 ? trimmed.substring(0, 50) + "..." : trimmed;
    }

    /**
     * Quét câu trả lời của chatbot, bọc các từ tiếng Anh có trong từ điển bằng cú pháp
     * [[word]] để frontend hiển thị thành link bấm-để-tra-từ. Chỉ bọc lần xuất hiện
     * đầu tiên của mỗi từ trong một câu trả lời để tránh rối mắt.
     */
    private String linkifyDictionaryWords(String text) {
        if (text == null || text.isBlank()) return text;

        Set<String> dictionaryWords = wordRepository.findAllEnglishWords().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(HashSet::new));

        if (dictionaryWords.isEmpty()) return text;

        Set<String> alreadyLinked = new HashSet<>();
        Matcher matcher = WORD_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String token = matcher.group();
            String lower = token.toLowerCase();

            if (token.length() >= MIN_LINKABLE_LENGTH
                    && dictionaryWords.contains(lower)
                    && !alreadyLinked.contains(lower)) {
                
                // Matcher.quoteReplacement để tránh lỗi với các ký tự đặc biệt như $ hoặc \
                matcher.appendReplacement(sb, Matcher.quoteReplacement("[[" + token + "]]"));
                alreadyLinked.add(lower);
            } else {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(token));
            }
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
