package com.wordwang.daily;

import java.time.LocalDate;

public record DailyHistoryEntryView(LocalDate date, String solutionWord) {
}
