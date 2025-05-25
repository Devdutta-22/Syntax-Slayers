
package model;

public class Record {
    private int id;
    private String name;
    private String email;

    public Record(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return id + "," + name + "," + email;
    }

    public static Record fromString(String line) {
        String[] parts = line.split(",");
        return new Record(Integer.parseInt(parts[0]), parts[1], parts[2]);
    }
}
