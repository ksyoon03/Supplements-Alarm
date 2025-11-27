package com.nutrient_reminder.service;

import com.nutrient_reminder.model.Nutrient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class NutrientRepository {
    private static final String FILE_PATH = "nutrient_data.json";
    private final Gson gson;

    public NutrientRepository() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void save(Nutrient newNutrient) {
        List<Nutrient> list = loadAll();
        list.add(newNutrient);
        saveToFile(list);
    }

    public List<Nutrient> loadAll() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new ArrayList<>();
        try (Reader reader = new FileReader(file, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<ArrayList<Nutrient>>(){}.getType();
            List<Nutrient> list = gson.fromJson(reader, listType);
            return list != null ? list : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void saveToFile(List<Nutrient> list) {
        try (Writer writer = new FileWriter(FILE_PATH, StandardCharsets.UTF_8)) {
            gson.toJson(list, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}