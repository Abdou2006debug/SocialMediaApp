package com.example.whatsappclone.Controllers;

import com.example.whatsappclone.Services.RelationShipsServices.BlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/block")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @PostMapping("/me/{useruuid}")
    public void block(@PathVariable String useruuid) {
        blockService.block(useruuid);
    }

    @DeleteMapping("/me/{useruuid}")
    public void unblock(@PathVariable String useruuid) {
        blockService.unblock(useruuid);
    }
}
