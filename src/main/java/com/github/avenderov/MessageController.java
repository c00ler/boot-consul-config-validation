package com.github.avenderov;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/message")
@AllArgsConstructor
public class MessageController {

    private final DynamicProperties dynamicProperties;

    @GetMapping
    public MessageDto getMessage() {
        return new MessageDto(dynamicProperties.getMessage());
    }

}
