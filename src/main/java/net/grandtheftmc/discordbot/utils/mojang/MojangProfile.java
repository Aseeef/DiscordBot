package net.grandtheftmc.discordbot.utils.mojang;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.UUID;

@AllArgsConstructor @Getter @ToString
public class MojangProfile {

    private UUID uuid;
    private String username;
    private SkinModel skinModel;
    private String skinUrl;
    private @Nullable String capeUrl;

    @Getter(value = AccessLevel.NONE) @ToString.Exclude
    protected long created;

    public enum SkinModel {
        ALEX,
        STEVE,
        UNKNOWN,
        ;
        public static SkinModel fromString(String model) {
            if (model == null) {
                return STEVE;
            }
            switch (model) {
                case "slim": {
                    return ALEX;
                }
                default: {
                    return UNKNOWN;
                }
            }
        }
    }

}
