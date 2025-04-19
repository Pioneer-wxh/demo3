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


}
