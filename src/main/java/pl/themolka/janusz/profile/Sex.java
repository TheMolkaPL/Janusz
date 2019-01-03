package pl.themolka.janusz.profile;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableBiMap;
import pl.themolka.janusz.Message;

import java.util.Objects;

public enum Sex {
    FEMALE {
        @Override
        protected String format0(Message message) {
            return message.feminine();
        }
    },

    MALE {
        @Override
        protected String format0(Message message) {
            return message.masculine();
        }
    },

    UNISEX {
        @Override
        protected String format0(Message message) {
            return message.unisex();
        }
    };

    private static final BiMap<Sex, String> SERIALIZED;

    static {
        Sex[] values = values();
        BiMap<Sex, String> serialized = HashBiMap.create(values.length);
        for (Sex sex : values) {
            serialized.put(sex, sex.name().toLowerCase());
        }

        SERIALIZED = ImmutableBiMap.copyOf(serialized);
    }



    public String format(Message message) {
        return this.format0(Objects.requireNonNull(message, "message"));
    }

    protected abstract String format0(Message message);

    public String serialize() {
        return Objects.requireNonNull(SERIALIZED.get(this), "SERIALIZED is corrupted");
    }

    public static Sex deserialize(String input) {
        return SERIALIZED.inverse().get(Objects.requireNonNull(input, "input"));
    }
}
