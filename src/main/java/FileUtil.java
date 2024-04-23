import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtil { // kasutada kui üldise failitöötluseks, seda võiks üldiselt kasutada kliendi poolel, serveril oma eraldi

    // seda kasutada, et failikirjutada. Kasutakse sõnumite salvestamiseks
    public static void writeToFileSave(String filename, String message) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, true))) {
            writer.write(message);
            writer.newLine(); // Lisa uus rida
        }
    }
    // Lugemise meetod
    public static List<String> readFromFile(String filename) throws IOException {
        List<String> messages = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                messages.add(line); // Lisab iga rea sõnumite järjendisse
            }
        }
        return messages;
    }
}
