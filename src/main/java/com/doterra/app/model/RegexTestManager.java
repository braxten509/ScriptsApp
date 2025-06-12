package com.doterra.app.model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages regex test cases with persistence
 */
public class RegexTestManager {
    private static final String TESTS_FILE = "data/regex_tests.dat";
    private List<RegexTest> tests;
    
    public RegexTestManager() {
        this.tests = new ArrayList<>();
        loadTests();
    }
    
    public List<RegexTest> getTests() {
        return new ArrayList<>(tests);
    }
    
    public void addTest(RegexTest test) {
        tests.add(test);
        saveTests();
    }
    
    public void updateTest(String testId, RegexTest updatedTest) {
        for (int i = 0; i < tests.size(); i++) {
            if (tests.get(i).getId().equals(testId)) {
                updatedTest.setId(testId); // Keep the same ID
                tests.set(i, updatedTest);
                saveTests();
                break;
            }
        }
    }
    
    public void deleteTest(String testId) {
        tests.removeIf(test -> test.getId().equals(testId));
        saveTests();
    }
    
    public void renameTest(String testId, String newName) {
        for (RegexTest test : tests) {
            if (test.getId().equals(testId)) {
                test.setName(newName);
                saveTests();
                break;
            }
        }
    }
    
    public RegexTest getTestById(String testId) {
        return tests.stream()
                .filter(test -> test.getId().equals(testId))
                .findFirst()
                .orElse(null);
    }
    
    @SuppressWarnings("unchecked")
    private void loadTests() {
        File file = new File(TESTS_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                tests = (List<RegexTest>) ois.readObject();
            } catch (Exception e) {
                System.err.println("Error loading regex tests: " + e.getMessage());
                tests = new ArrayList<>();
            }
        }
    }
    
    private void saveTests() {
        try {
            File file = new File(TESTS_FILE);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                oos.writeObject(tests);
            }
        } catch (Exception e) {
            System.err.println("Error saving regex tests: " + e.getMessage());
        }
    }
    
    public void moveTest(int fromIndex, int toIndex) {
        if (fromIndex >= 0 && fromIndex < tests.size() && 
            toIndex >= 0 && toIndex < tests.size() && 
            fromIndex != toIndex) {
            RegexTest test = tests.remove(fromIndex);
            tests.add(toIndex, test);
            saveTests();
        }
    }
}