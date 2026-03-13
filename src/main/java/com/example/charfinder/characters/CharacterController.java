package com.example.charfinder.characters;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/characters")
public class CharacterController {

    private final CharacterService characterService;

    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @PostMapping
    public Character create(@RequestBody Character character) {
        return characterService.createCharacter(character);
    }

    @PutMapping("/{id}")
    public Character update(@PathVariable String id, @RequestBody Character character) {
        return characterService.updateCharacter(id, character);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        characterService.deleteCharacter(id);
    }

    @GetMapping("/{id}/sheet")
    public CharacterSheetDTO getSheet(@PathVariable String id) {
        return characterService.getCharacterSheet(id);
    }
}