package com.c4soft.resthero.commons.domain;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record Period(@Nullable Instant from, @Nullable Instant to) {

}
