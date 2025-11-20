package com.irctc.localdb;


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class LocalDB<T> {
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final File file;
    private final Class<T> clazz;


    public LocalDB(String path, Class<T> clazz) {
        this.file = new File(path);
        this.clazz = clazz;
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
    }


    public List<T> readList() {
        try {
            if (!file.exists()) return new ArrayList<>();
            JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return mapper.readValue(file, type);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    public void writeList(List<T> list) {
        try {
            mapper.writeValue(file, list);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}