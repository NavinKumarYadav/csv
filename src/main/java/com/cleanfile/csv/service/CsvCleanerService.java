package com.cleanfile.csv.service;


import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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



    public void cleanCsv(){
        String inputFile = "src/main/resources/assignment_1.csv";
        String outputFile = "src/main/resources/cleaned_assignment_1.csv";

        try(
                Reader reader = Files.newBufferedReader(Paths.get(inputFile));
                CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
                Writer writer = Files.newBufferedWriter(Paths.get(outputFile));
                CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                        .withHeader("partner", "assigned_user", "productId", "name", "phone_number", "email", "stage",
                                "status", "created_date", "activity_json" ))
        ){
            Map<String, Integer> headerMap = parser.getHeaderMap();
            Set<String> seenProducts = new HashSet<>();

            for (CSVRecord record : parser){
                String partner = cleanPartner(record.get(headerKey(headerMap, "partner")));
                String assignedUser = cleanAssignedUser(record.get(headerKey(headerMap, "assigned_user")));
                String productId = mapProduct(record.get(headerKey(headerMap, "productId")));
                String name = cleanName(record.get(headerKey(headerMap, "name")));
                String phone = cleanPhone(record.get(headerKey(headerMap, "phone_number")));
                String email = record.get(headerKey(headerMap, "email"));
                String stage = mapStage(record.get(headerKey(headerMap, "stage")));
                String status = mapStatus(record.get(headerKey(headerMap, "status")));
                String createdDate = record.get(headerKey(headerMap, "created_date"));
                String activity = mapActivity(record.get(headerKey(headerMap, "activity_json")));

                if(phone.isBlank() || productId == null || productId.isBlank()) continue;

                String uniqueKey = productId + "-" + phone;
                if (seenProducts.contains(uniqueKey)) continue;
                seenProducts.add(uniqueKey);

                printer.printRecord(partner, assignedUser, productId, name, phone, email, stage,
                        status, createdDate, activity);
            }

            System.out.println("✅ Cleaned CSV created successfully at: " + outputFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String headerKey(Map<String, Integer> headerMap, String expected) {
        for (String key : headerMap.keySet()) {
            if (key.trim().equalsIgnoreCase(expected.trim())
                    || key.replaceAll("[^a-zA-Z]", "").equalsIgnoreCase(expected
                    .replaceAll("[^a-zA-Z]", ""))) {
                return key;
            }
        }
        System.out.println("Available headers: " + headerMap.keySet());
        throw new IllegalArgumentException("Header not found for: " + expected);
    }



    private String cleanPhone(String phone){
        if (phone == null || phone.isBlank()) return "";
        phone = phone.replaceAll("[^0-9]", "");
        if (phone.startsWith("91") && phone.length() > 10) {
            phone = phone.substring(phone.length() - 10);
        } else if (phone.startsWith("0") && phone.length() > 10) {
            phone = phone.substring(phone.length() - 10);
        }

        if (phone.length() < 10) return "";

        phone = phone.substring(phone.length() - 10);

        return "+91" + phone;
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

    private String cleanPartner(String partner){
        if(partner == null || partner.isBlank())  return "client_nocode";
        return partner;
    }

    private String cleanAssignedUser(String user){
        if (user == null || user.isBlank()) return "Unassigned";
        return user;
    }

    private String mapProduct(String product){
        if(product == null || product.isBlank()) return "";

        if(product.matches("\\d+")){
            product = "PR" + String.format("%08d", Integer.parseInt(product));
        }

        String projectName = newCsvProjects.getOrDefault(product.trim(), product.trim());

        for(Map.Entry<Integer, String> entry : refProjects.entrySet()){
            if(entry.getValue().equalsIgnoreCase(projectName)){
                return String.valueOf(entry.getKey());
            }
        }
        System.out.println("⚠ Unmapped product: " + projectName);
        return projectName;
    }

    private String mapStage(String stage){
        Map<String, String> stageMap = Map.of(
                "dcrm_stage1", "leadzump_stage1",
                "dcrm_stage2", "leadzump_stage2"
        );
        return stageMap.getOrDefault(stage, stage);
    }

    private String mapStatus(String status){
        Map<String, String> statusMap = Map.of(
                "dcrm_active", "leadzump_active",
                "dcrm_closed", "leadzump_closed"
        );
        return statusMap.getOrDefault(status, status);
    }

    private String mapActivity(String activity){
        Map<String, String> activityMap = Map.of(
                "dcrm_call", "leadzump_call",
                "dcrm_meeting", "leadzump_meeting"
        );
        return activityMap.getOrDefault(activity, activity);
    }
}
