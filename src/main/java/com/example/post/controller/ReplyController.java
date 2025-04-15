package com.example.post.controller;

import com.example.post.dto.ReplyReqDto;
import com.example.post.service.ReplyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community/reply")
public class ReplyController {
    private final ReplyService replyService;

    @PostMapping
    public ResponseEntity<?> addReply(@RequestHeader("Authorization") String token,
                                      @RequestBody ReplyReqDto replyReqDto) throws JsonProcessingException {
        return replyService.addReply(token, replyReqDto);
    }

    @GetMapping
    public ResponseEntity<?> getReply(@RequestHeader("Authorization") String token,
                                      long commentId){
        return replyService.getReply(token, commentId);
    }

    @PatchMapping("/{replyId}")
    public ResponseEntity<?> updateReply(@RequestHeader("Authorization") String token,
                                         @PathVariable long replyId,
                                         @RequestBody ReplyReqDto replyReqDto){
        return replyService.updateReply(token, replyId, replyReqDto);
    }

    @DeleteMapping("/{replyId}")
    public ResponseEntity<?> deleteReply(@RequestHeader("Authorization") String token,
                                         @PathVariable long replyId){
        return replyService.deleteReply(token, replyId);
    }

    @PostMapping("/{replyId}")
    public ResponseEntity<?> reactToReply(@RequestHeader("Authorization") String token,
                                          @PathVariable long replyId,
                                          @RequestBody ReplyReqDto replyReqDto){
        return replyService.reactToReply(token, replyId, replyReqDto);
    }
}
