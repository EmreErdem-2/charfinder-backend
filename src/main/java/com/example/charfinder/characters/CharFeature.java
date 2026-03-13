package com.example.charfinder.characters;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Getter
@Setter
public class CharFeature {
    @Id
    private String id;

    @Indexed
    private String key;

    private String name;

    @Field(targetType = FieldType.STRING)
    private String description;
}

