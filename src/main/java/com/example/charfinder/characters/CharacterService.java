package com.example.charfinder.characters;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final MongoTemplate mongoTemplate;

    public CharacterService(CharacterRepository characterRepository,
                            MongoTemplate mongoTemplate) {
        this.characterRepository = characterRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Character createCharacter(Character character) {
        return characterRepository.save(character);
    }

    public Character updateCharacter(String id, Character updated) {
        Character existing = characterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Character not found"));

        existing.setName(updated.getName());
        existing.setAncestryKey(updated.getAncestryKey());
        existing.setHeritageKey(updated.getHeritageKey());
        existing.setBackgroundKey(updated.getBackgroundKey());
        existing.setClassKey(updated.getClassKey());
        existing.setFeatKeys(updated.getFeatKeys());

        return characterRepository.save(existing);
    }

    public void deleteCharacter(String id) {
        characterRepository.deleteById(id);
    }

    public CharacterSheetDTO getCharacterSheet(String id) {

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("_id").is(id)),

                Aggregation.lookup("ancestries", "ancestryKey", "key", "ancestry"),
                Aggregation.lookup("heritages", "heritageKey", "key", "heritage"),
                Aggregation.lookup("backgrounds", "backgroundKey", "key", "background"),
                Aggregation.lookup("classes", "classKey", "key", "classInfo"),
                Aggregation.lookup("feats", "featKeys", "key", "feats"),

                Aggregation.unwind("ancestry", true),
                Aggregation.unwind("heritage", true),
                Aggregation.unwind("background", true),
                Aggregation.unwind("classInfo", true)
        );

        return mongoTemplate
                .aggregate(agg, "characters", CharacterSheetDTO.class)
                .getUniqueMappedResult();
    }
}
