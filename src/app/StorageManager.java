package app;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class StorageManager {

    private static final String FILE_NAME = "events.txt";

    // Locate the file properly even when running inside a JAR
    private File getFile() {
        try {
            URL resource = getClass().getClassLoader().getResource(FILE_NAME);

            if (resource == null) {
                // File doesn't exist → create it inside /resources/
                File newFile = new File("src/main/resources/" + FILE_NAME);
                newFile.getParentFile().mkdirs();
                newFile.createNewFile();
                return newFile;
            } else {
                return new File(resource.toURI());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /** Save a single event (append mode) */
    public void saveEvent(String eventText) {
        File file = getFile();
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(eventText + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Load all events */
    public List<String> loadEvents() {
        List<String> events = new ArrayList<>();
        File file = getFile();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) events.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return events;
    }

    /** Delete event */
    public void removeEvent(String eventText) {
        List<String> events = loadEvents();
        events.remove(eventText);
        saveAll(events);
    }

    /** Update event */
    public void updateEvent(String oldEvent, String newEvent) {
        List<String> events = loadEvents();
        int index = events.indexOf(oldEvent);
        if (index != -1) {
            events.set(index, newEvent);
        }
        saveAll(events);
    }

    /** Rewrite the entire file */
    private void saveAll(List<String> events) {
        File file = getFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String e : events) {
                writer.write(e);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
