package com.example.rareNote.repository;

import com.example.rareNote.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByTagsName(String tagName);

    @Query("SELECT DISTINCT n FROM Note n JOIN n.tags t WHERE t.name IN :tagNames")
    List<Note> findNotesRelatedToAnyTags(Set<String> tagNames);
}
