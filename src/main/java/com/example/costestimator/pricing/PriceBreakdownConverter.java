package com.example.costestimator.pricing;

import com.example.costestimator.pricing.calc.PriceLine;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

@Converter
public class PriceBreakdownConverter implements AttributeConverter<List<PriceLine>, String> {

  private static final JsonMapper MAPPER = JsonMapper.builder().build();
  private static final TypeReference<List<PriceLine>> TYPE = new TypeReference<>() {
  };

  @Override
  public String convertToDatabaseColumn(List<PriceLine> lines) {
    return lines == null ? null : MAPPER.writeValueAsString(lines);
  }

  @Override
  public List<PriceLine> convertToEntityAttribute(String json) {
    return json == null || json.isBlank() ? null : MAPPER.readValue(json, TYPE);
  }
}
