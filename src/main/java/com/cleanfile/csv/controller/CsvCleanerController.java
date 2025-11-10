package com.cleanfile.csv.controller;


import com.cleanfile.csv.service.CsvCleanerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsvCleanerController {

    private final CsvCleanerService csvCleanerService;

    public CsvCleanerController(CsvCleanerService csvCleanerService){
        this.csvCleanerService = csvCleanerService;
    }

    @GetMapping("/clean")
    public String cleanCsv(){
        csvCleanerService.cleanCsv();

        return "CSV file cleaned successfully! Check the cleaned_assignment_1.csv file in resources.";
    }
}
