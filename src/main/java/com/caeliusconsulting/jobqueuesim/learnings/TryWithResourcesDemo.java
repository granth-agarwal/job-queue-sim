package com.caeliusconsulting.jobqueuesim.learnings;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TryWithResourcesDemo {

    public static void main(String[] args) {

        try (BufferedReader reader = new BufferedReader(
                new FileReader("data.txt"))) {

            String line = reader.readLine();
            System.out.println("Read: " + line);

        } catch (IOException e) {
            System.out.println("File error: " + e.getMessage());
        }
    }
}