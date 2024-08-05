import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
public class Main {

    private static LocalDateTime getLocalDateTimeInput(Scanner scanner) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        while (true) {
            try {
                String input = scanner.nextLine();
                return LocalDateTime.parse(input, formatter);
            }
            catch (DateTimeParseException e) {
                System.out.println("Invalid date/time format. Please use YYYYMMDDHHmm:");
            }
        }
    }

}
