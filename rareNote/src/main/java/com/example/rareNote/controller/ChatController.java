package com.example.rareNote.controller;

import com.example.rareNote.model.Note;
import com.example.rareNote.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private NoteService noteService;

    public ChatController() {
        // Constructor can remain empty or be used for other initializations if necessary
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startPythonServices() {
        try {
            new ProcessBuilder("python", "E:/Study_Material/Code/python_services/gpt2_service.py").start();
            new ProcessBuilder("python", "E:/Study_Material/Code/python_services/query_gemini.py").start();
            System.out.println("Python services started successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error starting Python services: " + e.getMessage());
        }
    }

    @PostMapping
    public Map<String, String> handleChat(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        Map<String, String> responseMap = new HashMap<>();

        try {
            if (message.endsWith("!")) {
                String response = handleTagQuery(message);
                responseMap.put("response", response);
            } else if (message.startsWith("edit:")) {
                String response = handleEditCommand(message);
                responseMap.put("response", response);
            } else if (message.endsWith("?")) {
                String response = handleGeminiQuery(message);
                responseMap.put("response", response);
            } else {
                String response = handleRegularMessage(message);
                responseMap.put("response", response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            responseMap.put("response", "An error occurred: " + e.getMessage());
        }

        return responseMap;
    }

    private String handleTagQuery(String message) {
        String cleanedMessage = message.replace("!", "").trim();
        Set<String> tags = noteService.extractTags(cleanedMessage);

        if (tags == null || tags.isEmpty()) {
            return "No tags extracted from the query.";
        }

        List<Note> relatedNotes = noteService.findNotesRelatedToAnyTags(tags);
        if (relatedNotes.isEmpty()) {
            return "No related notes found for the provided tags.";
        }
        StringBuilder responseBuilder = new StringBuilder();
        for (Note note : relatedNotes) {
            try {
                responseBuilder.append(noteService.getNoteContent(note)).append(" ");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String combinedContent = responseBuilder.toString().trim();
        combinedContent=handleGeminiQuery(combinedContent+" don't say anything else just answer in brief whatever the *query is, if provided text is insufficient say 'Data is insufficient but here's response enhanced by AI ' and then provide gemini response. *query='"+message+"'");
        return combinedContent.isEmpty() ? "No related notes found." : combinedContent;
    }

    private String handleEditCommand(String message) {
        String[] parts = message.split(":", 3);
        if (parts.length == 3) {
            try {
                Long noteId = Long.parseLong(parts[1].trim());
                String newContent = parts[2].trim();
                Note updatedNote = noteService.updateNoteContent(noteId, newContent);
                return updatedNote != null ? "Note updated successfully." : "Note not found.";
            } catch (NumberFormatException e) {
                return "Invalid note ID.";
            }
        } else {
            return "Invalid edit command format.";
        }
    }

    private String handleGeminiQuery(String message) {
        Map<String, String> request = new HashMap<>();
        request.put("input", message);
        try {
            RestTemplate restTemplate = new RestTemplate();
            Map<String, String> response = restTemplate.postForObject("http://127.0.0.1:5011/query_gemini", request, Map.class);
            return response != null ? response.get("response") : "Error processing query.";
        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            return "Error processing query: " + e.getResponseBodyAsString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing query: " + e.getMessage();
        }
    }

    private String handleRegularMessage(String message) {
        Note note = new Note();
        Note savedNote = noteService.save(note, message);
        return savedNote != null ? "Note saved successfully." : "Error saving note.";
    }
}
