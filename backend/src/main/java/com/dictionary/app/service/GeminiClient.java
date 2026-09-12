package com.dictionary.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-2.5-flash}")
    private String model;

    @Value("${gemini.api.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    public GeminiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    // =========================
    // PROMPT CHO USER
    // =========================

    private static final String SYSTEM_PROMPT_QA = """
            Bạn là Dictly Bot, trợ lý học tiếng Anh của website từ điển Anh - Việt Dictly.

            Nhiệm vụ:
            - Giải thích nghĩa từ tiếng Anh bằng tiếng Việt.
            - Cho ví dụ câu và dịch nghĩa.
            - Giải thích ngữ pháp.
            - Phân biệt các từ dễ nhầm.
            - Dịch Anh - Việt hoặc Việt - Anh.
            - Gợi ý từ đồng nghĩa, trái nghĩa và cụm từ liên quan.

            Trả lời ngắn gọn, dễ hiểu, thân thiện.
            """;

    // =========================
    // PROMPT LUYỆN HỘI THOẠI
    // =========================

    private static final String SYSTEM_PROMPT_PRACTICE = """
            Bạn là Dictly Bot, người bạn luyện nói tiếng Anh.

            Quy tắc:
            - Trò chuyện chủ yếu bằng tiếng Anh đơn giản.
            - Chủ động đặt câu hỏi tiếp theo.
            - Nếu người dùng sai ngữ pháp hoặc dùng từ chưa tự nhiên:
              + Vẫn tiếp tục hội thoại.
              + Sau đó thêm dòng "💡 Sửa lỗi:" bằng tiếng Việt.
            - Nếu người dùng nói đúng, hãy khích lệ ngắn gọn.
            """;

    // =========================
    // PROMPT ADMIN
    // =========================

    private static final String SYSTEM_PROMPT_ADMIN = """
            Bạn là "Dictly Admin Assistant", trợ lý AI dành riêng cho QUẢN TRỊ VIÊN của website Dictly.

            QUY TẮC BẮT BUỘC:
            1. LUÔN trả lời bằng TIẾNG VIỆT 100%.
            2. XỬ LÝ THEO Ý ĐỊNH CỦA QUẢN TRỊ VIÊN:
               - Nếu Admin yêu cầu tạo/gợi ý nội dung (từ vựng, quiz, ví dụ, câu hỏi...): Trả lời TRỰC TIẾP và ĐẦY ĐỦ nội dung đó theo yêu cầu. Không ép vào khuôn mẫu báo cáo thống kê.
               - Nếu Admin hỏi về dữ liệu, phân tích hoặc báo cáo hệ thống: Mới sử dụng định dạng 📊 THỐNG KÊ, 🔎 NHẬN XÉT, 💡 ĐỀ XUẤT.
            3. Khi phân tích số liệu: Chỉ dựa trên dữ liệu thực tế được cung cấp, không tự bịa số liệu.
            """;

    private String getSystemPrompt(String mode) {

        if ("ADMIN".equalsIgnoreCase(mode)) {
            return SYSTEM_PROMPT_ADMIN;
        }

        if ("PRACTICE".equalsIgnoreCase(mode)) {
            return SYSTEM_PROMPT_PRACTICE;
        }

        return SYSTEM_PROMPT_QA;
    }

    // =========================
    // KIỂM TRA API KEY
    // =========================

    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    // =========================
    // CHAT GEMINI
    // =========================

    public String chat(List<ChatTurn> history, String mode) {

        if (!isConfigured()) {
            return """
                    ⚠️ AI Admin chưa được cấu hình API Key.

                    Vui lòng mở:
                    backend/src/main/resources/application.properties

                    và thêm:

                    gemini.api.key=API_KEY_CUA_BAN
                    """;
        }

        try {

            String systemPrompt = getSystemPrompt(mode);

            ObjectNode root = objectMapper.createObjectNode();

            // =========================
            // SYSTEM INSTRUCTION
            // =========================

            ObjectNode systemInstruction =
                    root.putObject("systemInstruction");

            ArrayNode systemParts =
                    systemInstruction.putArray("parts");

            systemParts.addObject()
                    .put("text", systemPrompt);

            // =========================
            // CONTENTS
            // =========================

            ArrayNode contents = root.putArray("contents");

            for (ChatTurn turn : history) {

                ObjectNode contentNode = contents.addObject();

                /*
                 * QUAN TRỌNG:
                 * Gemini chỉ chấp nhận:
                 *
                 * user
                 * model
                 *
                 * Không được gửi assistant.
                 */

                String geminiRole = "user";

                if ("model".equalsIgnoreCase(turn.role())
                        || "assistant".equalsIgnoreCase(turn.role())) {

                    geminiRole = "model";
                }

                // BUG CŨ:
                // contentNode.put("role", turn.role());

                // CODE ĐÚNG:
                contentNode.put("role", geminiRole);

                ArrayNode parts =
                        contentNode.putArray("parts");

                parts.addObject()
                        .put("text", turn.content());
            }

            // =========================
            // GENERATION CONFIG
            // =========================

            ObjectNode generationConfig =
                    root.putObject("generationConfig");

            generationConfig.put("temperature", 0.4);
            generationConfig.put("maxOutputTokens", 2048);

            // =========================
            // API URL
            // =========================

            String url =
                    baseUrl
                            + "/models/"
                            + model
                            + ":generateContent?key="
                            + apiKey;

            log.info("Đang gọi Gemini model: {}", model);

            // =========================
            // CALL API
            // =========================

            String responseBody = webClient
                    .post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(root.toString())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractText(responseBody);

        } catch (org.springframework.web.reactive.function.client.WebClientResponseException e) {

            log.error(
                    "Gemini API lỗi. HTTP status: {} - {}",
                    e.getStatusCode(),
                    e.getResponseBodyAsString()
            );

            int status = e.getStatusCode().value();

            if (status == 400) {
                return "❌ Gemini từ chối yêu cầu. Vui lòng kiểm tra cấu hình request.";
            }

            if (status == 401 || status == 403) {
                return "❌ API Key Gemini không hợp lệ hoặc không có quyền sử dụng.";
            }

            if (status == 404) {
                return "❌ Không tìm thấy model Gemini: " + model
                        + ". Hãy kiểm tra lại gemini.api.model.";
            }

            if (status == 429) {
                return "⚠️ Gemini đang giới hạn số lượt gọi. Vui lòng thử lại sau.";
            }

            return "❌ Gemini API đang gặp lỗi. Mã lỗi HTTP: " + status;

        } catch (Exception e) {

            log.error("Lỗi khi gọi Gemini API", e);

            return """
                    ❌ Không thể kết nối tới Gemini AI.

                    Vui lòng kiểm tra:
                    1. Backend Spring Boot đã chạy chưa.
                    2. API Key Gemini có hợp lệ không.
                    3. Tên model Gemini có đúng không.
                    4. Máy tính có kết nối Internet không.
                    """;
        }
    }

    // =========================
    // ĐỌC RESPONSE
    // =========================

    private String extractText(String responseBody) throws Exception {

        JsonNode root =
                objectMapper.readTree(responseBody);

        // =========================
        // ERROR
        // =========================

        JsonNode errorNode =
                root.path("error");

        if (!errorNode.isMissingNode()) {

            String message =
                    errorNode.path("message")
                            .asText("Lỗi không xác định từ Gemini API");

            log.error("Gemini error: {}", message);

            return "❌ Gemini API báo lỗi: " + message;
        }

        // =========================
        // CANDIDATES
        // =========================

        JsonNode candidates =
                root.path("candidates");

        if (!candidates.isArray()
                || candidates.isEmpty()) {

            return "❌ Gemini không trả về câu trả lời.";
        }

        JsonNode firstCandidate =
                candidates.get(0);

        JsonNode content =
                firstCandidate.path("content");

        JsonNode parts =
                content.path("parts");

        if (!parts.isArray()
                || parts.isEmpty()) {

            String finishReason =
                    firstCandidate
                            .path("finishReason")
                            .asText("");

            if (!finishReason.isEmpty()) {
                return "⚠️ AI không thể hoàn thành câu trả lời. "
                        + "Lý do: " + finishReason;
            }

            return "❌ Gemini không tạo được nội dung.";
        }

        StringBuilder result =
                new StringBuilder();

        for (JsonNode part : parts) {

            JsonNode text =
                    part.path("text");

            if (!text.isMissingNode()) {
                result.append(text.asText());
            }
        }

        String answer =
                result.toString().trim();

        if (answer.isEmpty()) {
            return "❌ Gemini trả về nội dung rỗng.";
        }

        return answer;
    }

    // =========================
    // SỬA CÂU
    // =========================

    public String correctSentence(String sentence) {

        if (!isConfigured()) {
            return "⚠️ AI chưa được cấu hình API Key.";
        }

        String prompt = """
                Bạn là giáo viên tiếng Anh.

                Hãy kiểm tra câu sau:

                Câu gốc: %s

                Trả lời bằng tiếng Việt theo mẫu:

                Câu sửa: ...
                Đánh giá: Đúng hoặc Cần sửa
                Giải thích: ...
                Cách nói tự nhiên hơn: ...

                Chỉ sửa ngữa pháp, chính tả và cách dùng từ; không thay đổi ý nghĩa ban đầu.
                """.formatted(sentence);

        return chat(
                List.of(ChatTurn.user(prompt)),
                "QA"
        );
    }

    // =========================
    // CHAT TURN
    // =========================

    public record ChatTurn(
            String role,
            String content
    ) {

        public static ChatTurn user(String content) {
            return new ChatTurn("user", content);
        }

        public static ChatTurn model(String content) {
            return new ChatTurn("model", content);
        }
    }
}
