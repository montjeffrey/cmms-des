package com.grash.model.enums;

public enum PartRelationType {
    // A part that can be used in place of another (symmetric: stored once, shown on both parts)
    SUBSTITUTE,
    // A related / accessory part (directional: stored on the source part only)
    RELATED
}
