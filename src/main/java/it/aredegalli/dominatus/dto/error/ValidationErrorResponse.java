package it.aredegalli.dominatus.dto.error;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ValidationErrorResponse {
    private String code;
    private Map<String, String> errors;
}
