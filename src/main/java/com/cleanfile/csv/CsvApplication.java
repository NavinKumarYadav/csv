package com.cleanfile.csv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import com.cleanfile.csv.service.CsvCleanerService;

@SpringBootApplication
public class CsvApplication implements CommandLineRunner {
    @Autowired
    private CsvCleanerService csvCleanerService;

    public static void main(String[] args) {
        SpringApplication.run(CsvApplication.class, args);
    }
    @Override
    public void run(String... args) {
        csvCleanerService.cleanCsv();
    }
}