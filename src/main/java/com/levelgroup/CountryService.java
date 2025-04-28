package com.levelgroup;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Component
public class CountryService {

    private final Set<String> allowedCountries;

    public CountryService() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("allowed-countries.json");
        Map<String, String> countriesMap = mapper.readValue(inputStream, new TypeReference<>() {});
        this.allowedCountries = new HashSet<>(countriesMap.values());
    }

    public boolean isAllowed(String countryName) {
        return allowedCountries.contains(countryName);
    }
}
