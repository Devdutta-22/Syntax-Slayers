
package io;

import model.Record;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {
    private static final String FILE_NAME = "records.txt";

    public static void initializeFile() throws IOException {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public static void saveAll(List<Record> records) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Record record : records) {
                writer.write(record.toString());
                writer.newLine();
            }
        }
    }

    public static List<Record> loadAll() throws IOException {
        List<Record> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                records.add(Record.fromString(line));
            }
        }
        return records;
    }
}
