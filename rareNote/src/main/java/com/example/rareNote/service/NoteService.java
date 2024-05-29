package com.example.rareNote.service;

import com.example.rareNote.model.Note;
import com.example.rareNote.model.Tag;
import com.example.rareNote.repository.NoteRepository;
import com.example.rareNote.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TagRepository tagRepository;

    private static final String DIRECTORY_PATH = "notes/";

    @Transactional
    public Note save(Note note, String content) {
        Set<String> tagNames = extractTags(content);
        if (tagNames == null || tagNames.isEmpty()) {
            return null;
        }

        Set<Tag> tags = new HashSet<>();
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName);
            if (tag == null) {
                tag = new Tag();
                tag.setName(tagName);
                tag = tagRepository.save(tag);
            }
            tags.add(tag);
        }
        note.setTags(tags);

        String filePath = saveNoteToFile(content, tags);
        note.setFilePath(filePath);

        return noteRepository.save(note);
    }

    private String saveNoteToFile(String content, Set<Tag> tags) {
        File directory = new File(DIRECTORY_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String baseFileName = getFirstTagName(tags);
        String fileName = getUniqueFileName(baseFileName);
        String filePath = DIRECTORY_PATH + fileName;

        try (FileWriter writer = new FileWriter(filePath, true)) {
            writer.write(content + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filePath;
    }

    private String getFirstTagName(Set<Tag> tags) {
        String firstTagName = tags.iterator().next().getName();
        return sanitizeFileName(firstTagName);
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
    }

    private String getUniqueFileName(String baseFileName) {
        String fileName = baseFileName + ".txt";
        int counter = 1;
        File file = new File(DIRECTORY_PATH + fileName);
        while (file.exists()) {
            fileName = baseFileName + "_" + counter + ".txt";
            file = new File(DIRECTORY_PATH + fileName);
            counter++;
        }
        return fileName;
    }

    public Set<String> extractTags(String content) {
        Set<String> tags = new HashSet<>();
        try {
            String pythonPath = "E:/Study_Material/Code/rareNote/venv/Scripts/python.exe";
            ProcessBuilder processBuilder = new ProcessBuilder(pythonPath, "E:/Study_Material/Code/python_services/extract_tags.py", content);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Error:")) {
                    return null;
                } else {
                    String[] extractedTags = line.split(",");
                    tags.addAll(Arrays.asList(extractedTags));
                }
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("Error executing Python script: " + e.getMessage());
            return null;
        }
        return tags;
    }

    public Note updateNoteContent(Long noteId, String newContent) {
        Optional<Note> optionalNote = noteRepository.findById(noteId);
        if (optionalNote.isPresent()) {
            Note note = optionalNote.get();
            note.setFilePath(saveNoteToFile(newContent, note.getTags()));
            return noteRepository.save(note);
        } else {
            return null;
        }
    }

    public List<Note> findNotesByTag(String tagName) {
        return noteRepository.findByTagsName(tagName);
    }

    public void deleteNoteById(Long noteId) {
        noteRepository.deleteById(noteId);
    }

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public Note getNoteById(Long noteId) {
        Optional<Note> optionalNote = noteRepository.findById(noteId);
        return optionalNote.orElse(null);
    }

    public String getNoteContent(Note note) throws IOException {
        return new String(Files.readAllBytes(Paths.get(note.getFilePath())));
    }

    public List<Note> findNotesByQuery(String query) {
        return noteRepository.findAll().stream()
                .filter(note -> {
                    try {
                        String content = getNoteContent(note);
                        return content.contains(query);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    public List<Note> findNotesRelatedToAnyTags(Set<String> tags) {
        Set<Note> relatedNotes = new HashSet<>();

        for (String tag : tags) {
           // System.out.println("Processing tag: " + tag);
            List<Note> notesWithTag = findNotesByQuery(tag);
           // System.out.println("Notes with tag '" + tag + "': " + notesWithTag);
            relatedNotes.addAll(notesWithTag);
            ///System.out.println("Current related notes after processing tag '" + tag + "': " + relatedNotes);
        }

        return new ArrayList<>(relatedNotes);
    }
}
