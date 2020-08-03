package com.github.taoroot.tao.security.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

public class RequestUtil {

    public static HashMap<String, String> getBodyJSON(HttpServletRequest request) {
        HashMap<String, String> hashMap = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        String line = null;

        try (BufferedReader reader = request.getReader()) {
            while (true) {
                try {
                    if ((line = reader.readLine()) == null) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sb.append(line);
            }
        } catch (IOException e) {
            return hashMap;
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            hashMap = mapper.readValue(sb.toString(), new TypeReference<HashMap<String, String>>() {
            });
        } catch (JsonProcessingException e) {
            return hashMap;
        }
        return hashMap;
    }
}
