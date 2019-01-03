// Borrowed from my private project.
package pl.themolka.janusz.util;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class PrettyDurationFormatter implements Formatter {
    @Override
    public String format(Object object) {
        if (object instanceof Duration) {
            return this.format((Duration) object);
        }

        return null;
    }

    public String format(Duration duration) {
        return this.format(duration, null);
    }

    public String format(Duration duration, String and) {
        if (duration.isNegative() || duration.isZero()) {
            return null;
        } else if (and == null) {
            and = "i";
        }
        and += " ";

        long days = duration.toDays();
        long months = days / 31;
        long years = months / 12;

        long hours = duration.toHours() - TimeUnit.DAYS.toHours(days);
        long minutes = duration.toMinutes() - TimeUnit.HOURS.toMinutes(hours);
        long seconds = duration.getSeconds() - TimeUnit.MINUTES.toSeconds(minutes);

        if (years == 1) {
            return "rok";
        } else if (years > 1) {
            return "bardzo dawno";
        } else if (months == 1) {
            return "miesiąc";
        } else if (months > 1) {
            return months + " miesięcy";
        } else if (days == 1) {
            return "dzień";
        } else if (days > 1) {
            return days + " dni";
        }

        boolean printHours = hours > 0;
        boolean printMinutes = printHours || minutes > 0;
        boolean printSeconds = seconds > 0 && hours <= 0;

        StringBuilder builder = new StringBuilder();
        if (printHours) {
            builder.append(hours).append(" godzin ");
        }
        if (printMinutes) {
            if (printHours) {
                builder.append(and);
            }

            builder.append(minutes).append(" minut ");
        }
        if (printSeconds) {
            if (printMinutes) {
                builder.append(and);
            }

            builder.append(seconds).append(" sekund ");
        }

        return builder.toString().trim();
    }
}
