package com.chenliang.chat.service;

import com.chenliang.chat.entity.RequirementConversationTurn;
import com.chenliang.chat.entity.RequirementDocResult;
import com.chenliang.chat.entity.RequirementDocSection;
import com.chenliang.chat.entity.RequirementGuidance;
import com.chenliang.chat.entity.RequirementSessionDraft;
import com.chenliang.chat.entity.RequirementStructuredAnalysis;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RequirementWorkshopService {

    private static final String MSG_INPUT_REQUIRED = "\\u8bf7\\u8f93\\u5165\\u9700\\u6c42\\u5185\\u5bb9\\u540e\\u518d\\u7ee7\\u7eed\\u3002";
    private static final String MSG_SESSION_REQUIRED = "sessionId \\u4e0d\\u80fd\\u4e3a\\u7a7a\\u3002";
    private static final String MSG_SESSION_NOT_FOUND = "\\u672a\\u627e\\u5230\\u5bf9\\u5e94\\u7684\\u9700\\u6c42\\u4f1a\\u8bdd\\uff0c\\u8bf7\\u91cd\\u65b0\\u521b\\u5efa\\u3002";
    private static final String TXT_UNNAMED = "\\u672a\\u547d\\u540d\\u9700\\u6c42";
    private static final String TXT_EMPTY_UNDERSTANDING = "\\u5f53\\u524d\\u8fd8\\u6ca1\\u6709\\u8f93\\u5165\\u9700\\u6c42\\u5185\\u5bb9\\u3002";
    private static final String TXT_NO_SUMMARY = "\\u6682\\u672a\\u5f62\\u6210\\u6709\\u6548\\u603b\\u7ed3\\u3002";
    private static final String TXT_DOC_OVERVIEW = "\\u672c\\u6587\\u6863\\u57fa\\u4e8e\\u9700\\u6c42\\u6f84\\u6e05\\u5bf9\\u8bdd\\u81ea\\u52a8\\u6574\\u7406\\uff0c\\u9762\\u5411 Java \\u7814\\u53d1\\u7528\\u4e8e\\u7406\\u89e3\\u4e1a\\u52a1\\u76ee\\u6807\\u3001\\u529f\\u80fd\\u8303\\u56f4\\u3001\\u6d41\\u7a0b\\u4e0e\\u5173\\u952e\\u5f85\\u786e\\u8ba4\\u9879\\u3002\\n\\u5f53\\u524d\\u5185\\u5bb9\\u9002\\u5408\\u4f5c\\u4e3a\\u9700\\u6c42\\u8bc4\\u5ba1\\u524d\\u7684\\u7814\\u53d1\\u8f93\\u5165\\u7a3f\\uff0c\\u4ecd\\u9700\\u7ed3\\u5408\\u5f85\\u786e\\u8ba4\\u95ee\\u9898\\u7ee7\\u7eed\\u6536\\u655b\\u3002";

    private final RequirementClarificationAiService clarificationAiService;
    private final Map<String, WorkshopSession> sessions = new ConcurrentHashMap<>();

    public RequirementWorkshopService(RequirementClarificationAiService clarificationAiService) {
        this.clarificationAiService = clarificationAiService;
    }

    public RequirementSessionDraft startSession() {
        String sessionId = UUID.randomUUID().toString();
        WorkshopSession session = new WorkshopSession();
        session.analysis = emptyAnalysis();
        session.guidance = buildInitialGuidance();
        sessions.put(sessionId, session);
        return toDraft(sessionId, session);
    }

    public RequirementSessionDraft continueConversation(String sessionId, String message) {
        WorkshopSession session = getSession(sessionId);
        if (!StringUtils.hasText(message)) {
            throw new IllegalArgumentException(MSG_INPUT_REQUIRED);
        }

        session.turns.add(new RequirementConversationTurn("user", message.trim()));
        RequirementStructuredAnalysis analysis = clarificationAiService.analyze(buildTranscript(session.turns));
        session.analysis = mergeAnalysis(analysis);
        session.guidance = buildGuidance(session.analysis);
        session.turns.add(new RequirementConversationTurn("assistant", session.guidance.getAssistantReply()));
        return toDraft(sessionId, session);
    }

    public RequirementSessionDraft generateDocument(String sessionId) {
        WorkshopSession session = getSession(sessionId);
        RequirementStructuredAnalysis latest = clarificationAiService.analyze(buildTranscript(session.turns));
        session.analysis = mergeAnalysis(latest);
        session.guidance = buildGuidance(session.analysis);
        session.document = buildDocument(session.analysis);
        return toDraft(sessionId, session);
    }

    public RequirementSessionDraft getSessionDraft(String sessionId) {
        return toDraft(sessionId, getSession(sessionId));
    }

    private WorkshopSession getSession(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            throw new IllegalArgumentException(MSG_SESSION_REQUIRED);
        }
        WorkshopSession session = sessions.get(sessionId);
        if (session == null) {
            throw new IllegalArgumentException(MSG_SESSION_NOT_FOUND);
        }
        return session;
    }

    private RequirementStructuredAnalysis emptyAnalysis() {
        return new RequirementStructuredAnalysis(
                TXT_UNNAMED,
                TXT_EMPTY_UNDERSTANDING,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(
                        "\\u4e1a\\u52a1\\u76ee\\u6807\\u5c1a\\u672a\\u8bf4\\u660e",
                        "\\u76ee\\u6807\\u4f7f\\u7528\\u8005\\u5c1a\\u672a\\u8bf4\\u660e",
                        "\\u6700\\u7ec8\\u4ea4\\u4ed8\\u7269\\u5c1a\\u672a\\u8bf4\\u660e"
                ),
                List.of(
                        "\\u8fd9\\u4e2a\\u9875\\u9762\\u6700\\u7ec8\\u8981\\u5e2e\\u8c01\\u5b8c\\u6210\\u4ec0\\u4e48\\u5de5\\u4f5c\\uff1f",
                        "AI \\u53cd\\u95ee\\u540e\\u6700\\u7ec8\\u8981\\u4ea7\\u51fa\\u4ec0\\u4e48\\u683c\\u5f0f\\u7684\\u7814\\u53d1\\u6587\\u6863\\uff1f",
                        "\\u7b2c\\u4e00\\u7248\\u6700\\u5148\\u8981\\u5b9e\\u73b0\\u7684\\u6700\\u5c0f\\u95ed\\u73af\\u662f\\u4ec0\\u4e48\\uff1f"
                )
        );
    }

    private RequirementGuidance buildInitialGuidance() {
        List<String> questions = List.of(
                "\\u8fd9\\u4e2a\\u9875\\u9762\\u7684\\u6838\\u5fc3\\u4f7f\\u7528\\u8005\\u662f\\u8c01\\uff1f\\u662f\\u4ea7\\u54c1\\u7ecf\\u7406\\u3001\\u8fd0\\u8425\\uff0c\\u8fd8\\u662f\\u7814\\u53d1\\uff1f",
                "\\u4f60\\u5e0c\\u671b\\u6700\\u7ec8\\u4ea7\\u51fa\\u7684\\u7814\\u53d1\\u6587\\u6863\\u81f3\\u5c11\\u5305\\u542b\\u54ea\\u4e9b\\u7ae0\\u8282\\uff1f",
                "\\u7b2c\\u4e00\\u7248\\u6700\\u5173\\u952e\\u7684\\u95ed\\u73af\\uff0c\\u662f\\u6536\\u96c6\\u9700\\u6c42\\u3001\\u6301\\u7eed\\u8ffd\\u95ee\\uff0c\\u8fd8\\u662f\\u5bfc\\u51fa\\u6587\\u6863\\uff1f"
        );
        List<String> missing = List.of(
                "\\u4e1a\\u52a1\\u76ee\\u6807",
                "\\u76ee\\u6807\\u89d2\\u8272",
                "\\u4ea4\\u4ed8\\u6587\\u6863\\u7ed3\\u6784"
        );
        return new RequirementGuidance(
                "\\u5f53\\u524d\\u8fd8\\u6ca1\\u6709\\u6536\\u96c6\\u5230\\u6709\\u6548\\u9700\\u6c42\\uff0c\\u8bf7\\u5148\\u8f93\\u5165\\u96f6\\u788e\\u7684\\u4ea7\\u54c1\\u60f3\\u6cd5\\u3001\\u573a\\u666f\\u6216\\u76ee\\u6807\\u3002",
                missing,
                questions,
                formatAssistantReply(
                        "\\u5f53\\u524d\\u6211\\u8fd8\\u6ca1\\u6709\\u62ff\\u5230\\u5177\\u4f53\\u9700\\u6c42\\u3002",
                        missing,
                        questions
                )
        );
    }

    private RequirementStructuredAnalysis mergeAnalysis(RequirementStructuredAnalysis analysis) {
        return new RequirementStructuredAnalysis(
                defaultIfBlank(analysis.getTitle(), TXT_UNNAMED),
                defaultIfBlank(analysis.getCurrentUnderstanding(), TXT_NO_SUMMARY),
                safeList(analysis.getFunctionalScope()),
                safeList(analysis.getBusinessFlow()),
                safeList(analysis.getDataPoints()),
                safeList(analysis.getNonFunctionalRequirements()),
                safeList(analysis.getRisks()),
                safeList(analysis.getMissingInformation()),
                trimQuestions(analysis.getNextQuestions())
        );
    }

    private RequirementGuidance buildGuidance(RequirementStructuredAnalysis analysis) {
        List<String> nextQuestions = trimQuestions(analysis.getNextQuestions());
        List<String> missing = safeList(analysis.getMissingInformation());
        return new RequirementGuidance(
                analysis.getCurrentUnderstanding(),
                missing,
                nextQuestions,
                formatAssistantReply(analysis.getCurrentUnderstanding(), missing, nextQuestions)
        );
    }

    private RequirementDocResult buildDocument(RequirementStructuredAnalysis analysis) {
        List<RequirementDocSection> sections = new ArrayList<>();
        sections.add(new RequirementDocSection("1. \\u80cc\\u666f\\u4e0e\\u76ee\\u6807", analysis.getCurrentUnderstanding()));
        sections.add(new RequirementDocSection("2. \\u529f\\u80fd\\u8303\\u56f4", joinAsNumberedLines(analysis.getFunctionalScope(), "\\u5f85\\u8865\\u5145\\u529f\\u80fd\\u8303\\u56f4")));
        sections.add(new RequirementDocSection("3. \\u4e1a\\u52a1\\u6d41\\u7a0b", joinAsNumberedLines(analysis.getBusinessFlow(), "\\u5f85\\u8865\\u5145\\u4e1a\\u52a1\\u6d41\\u7a0b")));
        sections.add(new RequirementDocSection("4. \\u5173\\u952e\\u6570\\u636e\\u4e0e\\u5bf9\\u8c61", joinAsNumberedLines(analysis.getDataPoints(), "\\u5f85\\u8865\\u5145\\u5173\\u952e\\u6570\\u636e\\u5b9a\\u4e49")));
        sections.add(new RequirementDocSection("5. \\u975e\\u529f\\u80fd\\u8981\\u6c42", joinAsNumberedLines(analysis.getNonFunctionalRequirements(), "\\u6682\\u672a\\u660e\\u786e\\u975e\\u529f\\u80fd\\u8981\\u6c42")));
        sections.add(new RequirementDocSection("6. \\u98ce\\u9669\\u4e0e\\u8fb9\\u754c", joinAsNumberedLines(analysis.getRisks(), "\\u6682\\u672a\\u8bc6\\u522b\\u660e\\u786e\\u98ce\\u9669")));
        return new RequirementDocResult(analysis.getTitle(), TXT_DOC_OVERVIEW, sections, safeList(analysis.getMissingInformation()));
    }

    private RequirementSessionDraft toDraft(String sessionId, WorkshopSession session) {
        String stage = session.document == null ? "clarifying" : "document_ready";
        return new RequirementSessionDraft(sessionId, stage, List.copyOf(session.turns), session.guidance, session.document);
    }

    private String buildTranscript(List<RequirementConversationTurn> turns) {
        StringBuilder builder = new StringBuilder();
        for (RequirementConversationTurn turn : turns) {
            builder.append(turn.getRole()).append(": ").append(turn.getContent()).append("\n");
        }
        return builder.toString();
    }

    private String formatAssistantReply(String currentUnderstanding, List<String> missing, List<String> questions) {
        StringBuilder builder = new StringBuilder();
        builder.append("\\u6211\\u5148\\u540c\\u6b65\\u5f53\\u524d\\u7406\\u89e3\\uff1a\n");
        builder.append(currentUnderstanding).append("\n\n");

        if (!missing.isEmpty()) {
            builder.append("\\u5f53\\u524d\\u8fd8\\u7f3a\\u8fd9\\u4e9b\\u5173\\u952e\\u4fe1\\u606f\\uff1a\n");
            for (int i = 0; i < missing.size(); i++) {
                builder.append(i + 1).append(". ").append(missing.get(i)).append("\n");
            }
            builder.append("\n");
        }

        if (!questions.isEmpty()) {
            builder.append("\\u4e0b\\u4e00\\u6b65\\u6211\\u5efa\\u8bae\\u5148\\u786e\\u8ba4\\u8fd9\\u51e0\\u4e2a\\u95ee\\u9898\\uff1a\n");
            for (int i = 0; i < questions.size(); i++) {
                builder.append(i + 1).append(". ").append(questions.get(i)).append("\n");
            }
        }
        return builder.toString().trim();
    }

    private List<String> trimQuestions(List<String> questions) {
        return safeList(questions).stream().limit(3).toList();
    }

    private List<String> safeList(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
    }

    private String joinAsNumberedLines(List<String> items, String fallback) {
        List<String> safeItems = safeList(items);
        if (safeItems.isEmpty()) {
            return fallback;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < safeItems.size(); i++) {
            if (i > 0) {
                builder.append("\n");
            }
            builder.append(i + 1).append(". ").append(safeItems.get(i));
        }
        return builder.toString();
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private static class WorkshopSession {
        private final List<RequirementConversationTurn> turns = new ArrayList<>();
        private RequirementStructuredAnalysis analysis;
        private RequirementGuidance guidance;
        private RequirementDocResult document;
    }
}
