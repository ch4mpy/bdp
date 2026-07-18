package nc.sgcb.labs.card.payment.web;

import jakarta.validation.constraints.NotNull;

public record CardStatusRequest(@NotNull Boolean isActive) {

}
