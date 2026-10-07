package com.caeliusconsulting.jobqueuesim.domain;

public record ExecutionSummary(int submitted, int completed, int failed, int retries, int totalAttempts) {
}
