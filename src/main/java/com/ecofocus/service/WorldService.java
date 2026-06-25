package com.ecofocus.service;

import com.ecofocus.dto.*;
import com.ecofocus.entity.World;
import com.ecofocus.repository.WorldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorldService {
    private final WorldRepository worldRepository;

    public WorldDto getWorld(Long userId) {
        World w = worldRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Dünya bulunamadı"));
        return toDto(w);
    }

    public WorldDto toDto(World w) {
        var objects = w.getObjects().stream()
                .map(o -> new WorldObjectDto(o.getId(), w.getId(), o.getType(), o.getTileX(), o.getTileY()))
                .collect(Collectors.toList());
        int earned = w.getTotalEarned() != null ? w.getTotalEarned() : w.getTotalPoints();
        return new WorldDto(w.getId(), w.getUser().getId(),
                w.getMapWidth(), w.getMapHeight(), w.getTotalPoints(), earned, objects);
    }
}
