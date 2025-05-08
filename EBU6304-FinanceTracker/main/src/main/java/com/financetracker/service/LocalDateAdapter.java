package com.financetracker.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * Gson TypeAdapter for converting LocalDate objects to and from JSON
 * using the ISO_LOCAL_DATE format (YYYY-MM-DD).
 */
public class LocalDateAdapter extends TypeAdapter<LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Writes a LocalDate object to JSON as a string.
     *
     * @param out   The JSON writer.
     * @param value The LocalDate object to write.
     * @throws IOException If an I/O error occurs.
     */
    @Override
    public void write(JsonWriter out, LocalDate value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.format(FORMATTER));
        }
    }

    /**
     * Reads a LocalDate object from JSON.
     *
     * @param in The JSON reader.
     * @return The LocalDate object read from JSON.
     * @throws IOException If an I/O error occurs or the date format is invalid.
     */
    @Override
    public LocalDate read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        } else {
            String dateString = in.nextString();
            try {
                return LocalDate.parse(dateString, FORMATTER);
            } catch (java.time.format.DateTimeParseException e) {
                // Handle potential parsing errors, e.g., log or throw a custom exception
                System.err.println("Error parsing date: " + dateString + ". " + e.getMessage());
                // Depending on requirements, you might return null, throw exception, or return a default date
                return null; // Or throw new IOException("Invalid date format: " + dateString, e);
            }
        }
    }
}
