package com.example.whatsappclone.SocialGraph.api;

import com.example.whatsappclone.SocialGraph.application.BlockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @PostMapping("/block/{userId}")
    public void block(@PathVariable String userId) {
        log.info("blocking user "+userId);
        blockService.block(userId);
    }

    @DeleteMapping("/unblock/{userId}")
    public void unblock(@PathVariable String userId) {
        blockService.unblock(userId);
    }
}
