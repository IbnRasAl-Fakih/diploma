package com.diploma.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public class CsvReadService {
    public static List<List<String>> readCsv(MultipartFile file, String split) throws IOException {
        List<List<String>> records = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(split);
                records.add(List.of(values)); 
            }
        }
        return records;
    }
}
