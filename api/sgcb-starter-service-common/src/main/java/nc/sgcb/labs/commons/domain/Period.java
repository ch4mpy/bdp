package nc.sgcb.labs.commons.domain;

import java.time.Instant;
import org.jspecify.annotations.Nullable;

public record Period(@Nullable Instant from, @Nullable Instant to) {

}
