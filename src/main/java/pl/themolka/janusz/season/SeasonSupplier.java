package pl.themolka.janusz.season;

import java.util.function.Function;

public interface SeasonSupplier extends Function<Long, Season> {
    Season current();
}
