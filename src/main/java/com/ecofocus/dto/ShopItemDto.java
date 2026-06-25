package com.ecofocus.dto;

public record ShopItemDto(
    String type,
    String emoji,
    String name,
    String unlockType,
    int price,
    int challengeThreshold,
    boolean owned,
    int quantity,
    boolean canUnlock,
    int remainingChallenges
) {}
