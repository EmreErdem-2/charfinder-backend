package com.example.charfinder.characters;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Document(collection = "characters")
public class Character {
    @Id
    private String id;

    private String name;

    private String ancestryKey;
    private String heritageKey;
    private String backgroundKey;
    private String classKey;

    private List<String> featKeys;

    @Indexed
    private String userId;
    @Indexed
    private String userEmail;
    private LocalDateTime creationDate;

}
