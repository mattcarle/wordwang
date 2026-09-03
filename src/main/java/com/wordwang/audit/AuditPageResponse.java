package com.wordwang.audit;

import java.util.List;

public record AuditPageResponse(List<AuditGameView> games, int page, int size, long totalElements, int totalPages) {
}
