package com.NotePilot;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/research")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class NotePilotController {
    private final NotePilotService notePilotService;

    @PostMapping("/process")
    public ResponseEntity<String> processContent(@RequestBody NotePilotRequest request){
        String result = notePilotService.processContent(request);
        return ResponseEntity.ok(result);
    }
}
