
package service;

import model.Record;
import io.FileHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RecordService {
    private List<Record> records = new ArrayList<>();

    public RecordService() {
        try {
            FileHandler.initializeFile();
            records = FileHandler.loadAll();
        } catch (IOException e) {
            System.out.println("Error loading records: " + e.getMessage());
        }
    }

    public void addRecord(Record record) {
        records.add(record);
        save();
    }

    public boolean deleteRecord(int id) {
        Iterator<Record> iterator = records.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getId() == id) {
                iterator.remove();
                save();
                return true;
            }
        }
        return false;
    }

    public boolean updateRecord(int id, String name, String email) {
        for (Record record : records) {
            if (record.getId() == id) {
                record.setName(name);
                record.setEmail(email);
                save();
                return true;
            }
        }
        return false;
    }

    public List<Record> getAllRecords() {
        return records;
    }

    private void save() {
        try {
            FileHandler.saveAll(records);
        } catch (IOException e) {
            System.out.println("Error saving records: " + e.getMessage());
        }
    }
}
