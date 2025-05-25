
package ui;

import model.Record;
import service.RecordService;

import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        RecordService service = new RecordService();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- CRUD Console App ---");
            System.out.println("1. Add Record");
            System.out.println("2. View Records");
            System.out.println("3. Update Record");
            System.out.println("4. Delete Record");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    try {
                        System.out.print("Enter ID: ");
                        int id = Integer.parseInt(scanner.nextLine());
                        System.out.print("Enter Name: ");
                        String name = scanner.nextLine();
                        System.out.print("Enter Email: ");
                        String email = scanner.nextLine();
                        service.addRecord(new Record(id, name, email));
                        System.out.println("Record added.");
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID format.");
                    }
                    break;
                case 2:
                    List<Record> records = service.getAllRecords();
                    for (Record record : records) {
                        System.out.println(record);
                    }
                    break;
                case 3:
                    try {
                        System.out.print("Enter ID to update: ");
                        int id = Integer.parseInt(scanner.nextLine());
                        System.out.print("Enter new Name: ");
                        String name = scanner.nextLine();
                        System.out.print("Enter new Email: ");
                        String email = scanner.nextLine();
                        if (service.updateRecord(id, name, email)) {
                            System.out.println("Record updated.");
                        } else {
                            System.out.println("Record not found.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input.");
                    }
                    break;
                case 4:
                    try {
                        System.out.print("Enter ID to delete: ");
                        int id = Integer.parseInt(scanner.nextLine());
                        if (service.deleteRecord(id)) {
                            System.out.println("Record deleted.");
                        } else {
                            System.out.println("Record not found.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input.");
                    }
                    break;
                case 5:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }
}
