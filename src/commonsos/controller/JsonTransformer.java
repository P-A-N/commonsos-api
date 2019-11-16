package commonsos.controller;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {
  private Gson gson;
  
  @Inject
  private void createGson() {
    GsonBuilder gb = new GsonBuilder();
    gb.registerTypeAdapter(LocalDate.class, new JsonSerializer<LocalDate>() {

      @Override
      public JsonElement serialize(LocalDate date, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
      }
      
    });
    gb.registerTypeAdapter(Instant.class, new JsonSerializer<Instant>() {

      @Override
      public JsonElement serialize(Instant instant, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(instant.toString());
      }
      
    });
    this.gson = gb.create();
  }

  @Override 
  public String render(Object model) {
    return gson.toJson(model);
  }
}
