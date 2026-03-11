package com.urlshortener.dto.response;

import java.util.List;

public class AnalyticsResponse {

    private Long urlId;
    private String shortCode;
    private long totalClicks;
    private List<DailyCount> clicksPerDay;

    public static class DailyCount {
        private String date;   // "2024-03-01"
        private long count;

        public DailyCount(String date, long count) {
            this.date = date;
            this.count = count;
        }

        public String getDate() { return date; }
        public long getCount() { return count; }
    }

    public Long getUrlId() { return urlId; }
    public void setUrlId(Long urlId) { this.urlId = urlId; }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public long getTotalClicks() { return totalClicks; }
    public void setTotalClicks(long totalClicks) { this.totalClicks = totalClicks; }

    public List<DailyCount> getClicksPerDay() { return clicksPerDay; }
    public void setClicksPerDay(List<DailyCount> clicksPerDay) { this.clicksPerDay = clicksPerDay; }
}
