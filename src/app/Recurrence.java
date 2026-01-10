package app;

import java.time.LocalDate;

public class Recurrence {
    public int eventId;
    public String recurrentInterval;
    public int recurrentTimes;
    public String recurrentEndDate;

    public Recurrence(int eventId, String interval, int times, String endDate) {
        this.eventId = eventId;
        this.recurrentInterval = interval;
        this.recurrentTimes = times;
        this.recurrentEndDate = endDate;
    }

    public Recurrence(int id, String interval, int count, LocalDate endDate) {
        this.eventId = id;
        this.recurrentInterval = interval;
        this.recurrentTimes = count;
        this.recurrentEndDate = (endDate == null) ? "0" : endDate.toString();
    }
}
