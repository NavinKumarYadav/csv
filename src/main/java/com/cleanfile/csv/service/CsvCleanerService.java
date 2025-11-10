package com.cleanfile.csv.service;

import org.apache.commons.csv.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class CsvCleanerService {

    public static final Map<Integer, String> refProjects = Map.of(
            10, "Primus",
            11, "DB Lakven Visishta",
            12, "Cityville",
            13, "Purva Sparkling Springs",
            14, "Codename Lake & Bloom",
            15, "Countryside Raindance",
            16, "White Waters"
    );

    public static final Map<String, String> newCsvProjects = Map.ofEntries(
            Map.entry("PR00000001", "Tejas"),
            Map.entry("PR00000002", "Keerthi"),
            Map.entry("PR00000003", "Keerthi Commercial"),
            Map.entry("PR00000004", "Pravanika"),
            Map.entry("PR00000005", "Cityville"),
            Map.entry("PR00000006", "Gokulam"),
            Map.entry("PR00000007", "Sterling Villa Grand"),
            Map.entry("PR00000008", "Raja Woods Parkk"),
            Map.entry("PR00000009", "Habitat Aura"),
            Map.entry("PR00000010", "Purva Smiling Willows"),
            Map.entry("PR00000011", "Pratham Indraprastha"),
            Map.entry("PR00000012", "Sipani Royal Heritage"),
            Map.entry("PR00000013", "Keerthi Prime"),
            Map.entry("PR00000014", "SMR Gateways"),
            Map.entry("PR00000015", "SRK Gardens"),
            Map.entry("PR00000016", "JP Tulips"),
            Map.entry("PR00000017", "Birla Apple Spire"),
            Map.entry("PR00000018", "Whitefield Manors"),
            Map.entry("PR00000019", "SBR Minara"),
            Map.entry("PR00000020", "SBR Earth & Sky"),
            Map.entry("PR00000021", "Cedar String"),
            Map.entry("PR00000022", "Hebbal One"),
            Map.entry("PR00000023", "Life By the Lake"),
            Map.entry("PR00000024", "Tropical Garden"),
            Map.entry("PR00000025", "Olde Town"),
            Map.entry("PR00000026", "abc"),
            Map.entry("PR00000027", "Earthen Ambience"),
            Map.entry("PR00000028", "LITTLE EARTH"),
            Map.entry("PR00000029", "SDMV Elite"),
            Map.entry("PR00000030", "Little Earth"),
            Map.entry("PR00000031", "Midas Exotica"),
            Map.entry("PR00000032", "Pragathi Amber"),
            Map.entry("PR00000033", "Primus"),
            Map.entry("PR00000034", "SLN Nidhi Palms"),
            Map.entry("PR00000035", "Purva Sparkling Springs"),
            Map.entry("PR00000036", "Codename 10 Lines"),
            Map.entry("PR00000037", "DB Lakven Visishta"),
            Map.entry("PR00000038", "White Waters"),
            Map.entry("PR00000039", "Countryside Raindance"),
            Map.entry("PR00000040", "Codename Lake & Bloom")
    );

    private final Map<String, Integer> phoneLookup = loadPhoneLookup();

    private Map<String, Integer> loadPhoneLookup() {
        Map<String, Integer> lookup = new HashMap<>();
        try {
            String jsonText = Files.readString(Paths.get("src/main/resources/phone_data.json"));
            JSONArray arr = new JSONArray(jsonText);

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                int id = obj.getInt("ID");
                String phone = obj.getString("PHONE_NUMBER").replaceAll("[^0-9]", "");
                if (phone.length() > 10) phone = phone.substring(phone.length() - 10);
                lookup.put("+91" + phone, id);
            }
        } catch (Exception ignored) {}
        return lookup;
    }

    public void cleanCsv() {
        String inputFile = "src/main/resources/assignment_1.csv";
        String outputFile = "src/main/resources/cleaned_assignment_1.csv";

        try (
                Reader reader = Files.newBufferedReader(Paths.get(inputFile));
                CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
                Writer writer = Files.newBufferedWriter(Paths.get(outputFile));
                CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                        .withQuoteMode(QuoteMode.ALL)
                        .withHeader("partner","assigned_user","productId","name","phone_number","email","stage","status","created_date","activity_json"))
        ) {
            Set<String> seen = new HashSet<>();
            Map<String,Integer> headerMap = parser.getHeaderMap();

            for (CSVRecord record : parser) {

                String partner = mapUser(record.get(headerKey(headerMap,"partner")), true);
                String assigned = mapUser(record.get(headerKey(headerMap,"assigned_user")), false);

                String productId = mapProduct(record.get(headerKey(headerMap, "productId")));
                String name = cleanName(record.get(headerKey(headerMap,"name")));
                String phoneRaw = record.get(headerKey(headerMap,"phone_number"));
                String phone = formatPhone(phoneRaw);

                if (phone.isEmpty() || productId.isBlank()) continue;

                String unique = productId + "-" + phone;
                if (!seen.add(unique)) continue;

                printer.printRecord(
                        partner,
                        assigned,
                        productId,
                        name,
                        phone,
                        record.get(headerKey(headerMap, "email")),
                        record.get(headerKey(headerMap, "stage")),
                        record.get(headerKey(headerMap, "status")),
                        record.get(headerKey(headerMap, "created_date")),
                        record.get(headerKey(headerMap, "activity_json"))
                );
            }

            System.out.println("Cleaned CSV generated → " + outputFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String headerKey(Map<String, Integer> map, String expected) {

        String normalizedExpected = expected.replaceAll("[^a-zA-Z_]", "").trim();

        return map.keySet().stream()
                .filter(original -> {
                    String cleaned = original
                            .replace("\uFEFF", "")
                            .replace("\"", "")
                            .replaceAll("[^a-zA-Z_]", "")
                            .trim();
                    return cleaned.equalsIgnoreCase(normalizedExpected);
                })
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mapping for " + normalizedExpected + " not found, expected one of " + map.keySet()
                ));
    }
    
    private String mapUser(String value, boolean isPartner) {
        if (value == null || value.isBlank()) {
            return isPartner ? "client_nocode" : "Unassigned";
        }

        value = value.replaceAll("[^0-9]", "");

        if (value.length() > 10) value = value.substring(value.length() - 10);
        if (value.length() != 10) return isPartner ? "client_nocode" : "Unassigned";

        String formatted = "+91" + value;
        Integer id = phoneLookup.get(formatted);

        return id != null ? id.toString() : formatted;
    }

    private String formatPhone(String phone) {
        if (phone == null) return "";
        phone = phone.replaceAll("[^0-9]", "");
        if (phone.length() > 10) phone = phone.substring(phone.length() - 10);
        return phone.length() == 10 ? "+91" + phone : "";
    }

    private String cleanName(String name){
        if(name == null || name.isBlank()) return "New Customer";

        name = name.trim();
        name = name.replaceAll("[^A-Za-z\\s.]", " ").replaceAll("\\s+", " ").trim();
        name = name.replaceAll("(?i)\\b(NA|N/A|NULL|NONE|NO NAME|--|NIL)\\b", "").trim();
        name = name.replaceAll("(?i)\\b(\\w+)\\b(?:\\s+\\1\\b)+", "$1");
        name = name.replaceAll("\\.+$", "").trim();
        name = name.replaceAll("\\s+", " ").trim();
        return name.isBlank() ? "New Customer" : name;
    }

    private String mapProduct(String product) {
        if (product == null || product.isBlank()) return "";
        if (product.matches("\\d+")) product = "PR" + String.format("%08d", Integer.parseInt(product));
        String project = newCsvProjects.getOrDefault(product, product);
        return refProjects.entrySet().stream()
                .filter(e -> e.getValue().equalsIgnoreCase(project))
                .map(e -> String.valueOf(e.getKey()))
                .findFirst()
                .orElse(project);
    }
}
