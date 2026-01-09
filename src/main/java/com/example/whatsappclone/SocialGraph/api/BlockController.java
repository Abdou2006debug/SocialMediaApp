package com.example.whatsappclone.SocialGraph.api;

import com.example.whatsappclone.SocialGraph.application.BlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @PostMapping("/block/{userId}")
    public void block(@PathVariable String userId) {
        blockService.block(userId);
    }

    @DeleteMapping("/unblock/{userId}")
    public void unblock(@PathVariable String userId) {
        blockService.unblock(userId);
    }
}
