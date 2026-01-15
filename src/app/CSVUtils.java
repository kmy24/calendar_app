package app;

import java.io.*;
import java.util.*;

public class CSVUtils {
    public static void rewriteEventCSV(List<Event> events) {
        File finalFile = new File("event.csv");
        File tempFile = new File("event_temp.csv");

        try (PrintWriter pw = new PrintWriter(new FileWriter(tempFile))) {
            pw.println("id,title,desc,start,end,location,priority");
            for (Event e : events) {
                pw.println(e.id + "," + sanitize(e.title) + "," + sanitize(e.description) + "," +
                           e.startDateTime + "," + e.endDateTime + "," + 
                           sanitize(e.location) + "," + sanitize(e.priority));
            }
            pw.flush();
            pw.close();
            if (finalFile.exists()) finalFile.delete();
            tempFile.renameTo(finalFile);
        } catch (IOException e) {
            System.out.println("Error saving events.");
        }
    }

    public static void rewriteRecurrenceCSV(Map<Integer, Recurrence> map) {
        File finalFile = new File("recurrent.csv");
        File tempFile = new File("recurrent_temp.csv");
        try (PrintWriter pw = new PrintWriter(new FileWriter(tempFile))) {
            pw.println("id,interval,times,endDate");
            for (Recurrence r : map.values()) {
                pw.println(r.eventId + "," + r.recurrentInterval + "," + r.recurrentTimes + "," + r.recurrentEndDate);
            }
            pw.flush(); pw.close();
            if (finalFile.exists()) finalFile.delete();
            tempFile.renameTo(finalFile);
        } catch (IOException e) { System.out.println("Error saving recurrence."); }
    }

    private static String sanitize(String s) {
        return (s == null) ? "" : s.replace(",", ";");
    }
    
    public static void rewriteAdditionalCSV(Map<Integer, AdditionalInfo> map) {
    File finalFile = new File("additional.csv");
    File tempFile = new File("additional_temp.csv");

    try (PrintWriter pw = new PrintWriter(new FileWriter(tempFile))) {
        pw.println("id,category,attendees");
        for (AdditionalInfo info : map.values()) {
            pw.println(info.eventId + "," + sanitize(info.category) + "," + sanitize(info.attendees));
        }
        pw.flush();
        pw.close();
        if (finalFile.exists()) finalFile.delete();
        tempFile.renameTo(finalFile);
    } catch (IOException e) {
        System.out.println("Error saving additional info.");
    }
    }   
}