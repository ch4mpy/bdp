package com.c4soft.resthero.card.web;

import jakarta.validation.constraints.NotNull;

public record CardStatusRequest(@NotNull Boolean isActive) {

}
