package com.github.avenderov;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "dynamic")
@Data
public class DynamicProperties {

    @NotBlank
    @Length(min = 5)
    private String message;

}
