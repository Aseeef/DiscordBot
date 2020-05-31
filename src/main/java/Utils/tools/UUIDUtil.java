package Utils.tools;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Stephen
 */
public class UUIDUtil {

    /**
     * A {@link Pattern} used to identify and/or split full UUIDs
     */
    private static final Pattern PATTERN_UUID = Pattern.compile("^[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}$", Pattern.CASE_INSENSITIVE);
    /**
     * A {@link Pattern} used to identify and/or split trimmed UUIDs
     */
    private static final Pattern PATTERN_TRIMMED_UUID = Pattern.compile("^([a-z0-9]{8})([a-z0-9]{4})([a-z0-9]{4})([a-z0-9]{4})([a-z0-9]{12})$", Pattern.CASE_INSENSITIVE);


    /**
     * Create a UUID safely from a {@link String}.
     *
     * @param string The {@link String} to deserialize into an {@link UUID} object.
     * @return {@link Optional#empty()} if the provided {@link String} is illegal, otherwise an {@link Optional}
     * containing the deserialized {@link UUID} object.
     */
    public static Optional<UUID> createUUID(String string) {
        if (string == null) {
            return Optional.empty();
        }

        UUID result = null;

        try {
            // Is it a valid UUID?
            if (!PATTERN_UUID.matcher(string).matches()) {
                // Un-trim UUID if it is trimmed
                Matcher matcher = PATTERN_TRIMMED_UUID.matcher(string);
                if (matcher.matches()) {
                    StringBuilder sb = new StringBuilder();

                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        if (i != 1) {
                            sb.append("-");
                        }

                        sb.append(matcher.group(i));
                    }

                    string = sb.toString();
                } else {
                    // Invalid UUID
                    string = null;
                }
            }

            if (string != null) {
                result = UUID.fromString(string);
            }
        } catch (IllegalArgumentException ignored) {
            // Useless data passed
        }

        return Optional.ofNullable(result);
    }
}
