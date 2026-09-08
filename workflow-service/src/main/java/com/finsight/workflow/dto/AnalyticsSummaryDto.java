package com.finsight.workflow.dto;

import java.math.BigDecimal;
import java.util.Map;

public class AnalyticsSummaryDto {
    private long totalWorkflows;
    private long approvedCount;
    private long rejectedCount;
    private long needsReviewCount;
    private BigDecimal approvalRate;
    private BigDecimal averageLoanAmount;
    private double averageProcessingTimeSeconds;
    private Map<String, Long> statusDistribution;

    public AnalyticsSummaryDto() {}

    public long getTotalWorkflows() { return totalWorkflows; }
    public void setTotalWorkflows(long totalWorkflows) { this.totalWorkflows = totalWorkflows; }
    public long getApprovedCount() { return approvedCount; }
    public void setApprovedCount(long approvedCount) { this.approvedCount = approvedCount; }
    public long getRejectedCount() { return rejectedCount; }
    public void setRejectedCount(long rejectedCount) { this.rejectedCount = rejectedCount; }
    public long getNeedsReviewCount() { return needsReviewCount; }
    public void setNeedsReviewCount(long needsReviewCount) { this.needsReviewCount = needsReviewCount; }
    public BigDecimal getApprovalRate() { return approvalRate; }
    public void setApprovalRate(BigDecimal approvalRate) { this.approvalRate = approvalRate; }
    public BigDecimal getAverageLoanAmount() { return averageLoanAmount; }
    public void setAverageLoanAmount(BigDecimal averageLoanAmount) { this.averageLoanAmount = averageLoanAmount; }
    public double getAverageProcessingTimeSeconds() { return averageProcessingTimeSeconds; }
    public void setAverageProcessingTimeSeconds(double averageProcessingTimeSeconds) { this.averageProcessingTimeSeconds = averageProcessingTimeSeconds; }
    public Map<String, Long> getStatusDistribution() { return statusDistribution; }
    public void setStatusDistribution(Map<String, Long> statusDistribution) { this.statusDistribution = statusDistribution; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private long totalWorkflows;
        private long approvedCount;
        private long rejectedCount;
        private long needsReviewCount;
        private BigDecimal approvalRate;
        private BigDecimal averageLoanAmount;
        private double averageProcessingTimeSeconds;
        private Map<String, Long> statusDistribution;

        public Builder totalWorkflows(long totalWorkflows) { this.totalWorkflows = totalWorkflows; return this; }
        public Builder approvedCount(long approvedCount) { this.approvedCount = approvedCount; return this; }
        public Builder rejectedCount(long rejectedCount) { this.rejectedCount = rejectedCount; return this; }
        public Builder needsReviewCount(long needsReviewCount) { this.needsReviewCount = needsReviewCount; return this; }
        public Builder approvalRate(BigDecimal approvalRate) { this.approvalRate = approvalRate; return this; }
        public Builder averageLoanAmount(BigDecimal averageLoanAmount) { this.averageLoanAmount = averageLoanAmount; return this; }
        public Builder averageProcessingTimeSeconds(double averageProcessingTimeSeconds) { this.averageProcessingTimeSeconds = averageProcessingTimeSeconds; return this; }
        public Builder statusDistribution(Map<String, Long> statusDistribution) { this.statusDistribution = statusDistribution; return this; }

        public AnalyticsSummaryDto build() {
            AnalyticsSummaryDto a = new AnalyticsSummaryDto();
            a.setTotalWorkflows(totalWorkflows);
            a.setApprovedCount(approvedCount);
            a.setRejectedCount(rejectedCount);
            a.setNeedsReviewCount(needsReviewCount);
            a.setApprovalRate(approvalRate);
            a.setAverageLoanAmount(averageLoanAmount);
            a.setAverageProcessingTimeSeconds(averageProcessingTimeSeconds);
            a.setStatusDistribution(statusDistribution);
            return a;
        }
    }
}
