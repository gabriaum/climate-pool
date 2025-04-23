package com.gabriaum.change.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.World;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum ChangeType {

    WEATHER(worlds -> {

        for (World world : worlds) {

            world.setStorm(true);
        }
    }),

    THUNDER(worlds -> {

        for (World world : worlds) {

            world.setStorm(true);
            world.setThundering(true);
        }
    }),

    CLEAN(worlds -> {

        for (World world : worlds) {

            world.setStorm(false);
            world.setThundering(false);
        }
    }),
    ;

    private final Executor executor;

    public interface Executor {
        void execute(List<World> worlds);
    }

    public static ChangeType getByName(String name) {
        for (ChangeType type : ChangeType.values()) {

            if (type.name().equalsIgnoreCase(name)) {

                return type;
            }
        }

        return null;
    }

    public static ChangeType getNext(ChangeType type) {

        ChangeType[] values = ChangeType.values();

        for (int i = 0; i < values.length; i++) {

            if (values[i] == type) {

                return values[(i + 1) % values.length];
            }
        }

        return null;
    }
}
