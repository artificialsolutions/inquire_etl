package inquirereport;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LocalFile {
    static void output(String separator, Map.Entry<String, Iterable<Map<String, Object>>> results, String path) {

        //replace bad chars in filename
        String fileName = sanitizeFilename(results.getKey());

        // create the output folder if it does not exist
        File directory = new File(String.valueOf(path));
        if (!directory.exists()) {
            boolean mkdirsSuccess = directory.mkdirs();
            System.out.println((mkdirsSuccess ? "Success making " : "Failed to make ") + " directory: " + directory.getPath());

        }

        String outputFileName = fileName;
        if (separator.equalsIgnoreCase("json")) {
            outputFileName = path.concat(outputFileName).concat(".json");

        } else if (separator.equalsIgnoreCase(",")) {
            outputFileName = path.concat(outputFileName).concat(".csv");
        } else {
            outputFileName = path.concat(outputFileName).concat(".txt");
        }

        System.out.println("Writing to " + outputFileName + " ...");

        if (separator.equalsIgnoreCase("json")) {
            // Write out to JSON
            writeResultsToJson(results.getValue(), outputFileName);
        } else {
            // Write out to text files
            writeResultstoText(results.getValue(), outputFileName, separator);
        }
    }

    private static void writeResultsToJson(Iterable<Map<String, Object>> results, String fullFileName) {

        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(fullFileName), results);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void writeResultstoText(Iterable<Map<String, Object>> results, String fullFileName, String separator) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fullFileName), StandardCharsets.UTF_8))) {

            boolean headerDisplayed = false;
            for (Map<String, Object> map : results) {
                List<String> header = new ArrayList<>();
                List<String> row = new ArrayList<>();
                map.entrySet().stream().forEach((entry) -> {
                    header.add("\"" + entry.getKey() + "\"");
                    row.add("\"" + entry.getValue().toString().replace("\"", "\"\"") + "\"");
                });
                if (!headerDisplayed) {
                    writer.write(String.join(separator, header) + System.getProperty("line.separator"));
                    headerDisplayed = true;
                }
                writer.write(String.join(separator, row) + System.getProperty("line.separator"));
            }

            System.out.println("Writing to " + fullFileName + " complete.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static String sanitizeFilename(String name) {
        return name.replaceAll("[:\\\\/*?|<>]", "_");
    }
}
