package net.grandtheftmc.discordbot.utils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtils {

    public static final Map<Character, String> SIMILAR_CHARACTER_MAP;

    public static final Pattern PATTERN_NON_ALPHANUMERICS = Pattern.compile("[^a-zA-Z0-9]+", Pattern.CASE_INSENSITIVE);

    public static final Pattern PATTERN_FORMAT_CODE = Pattern.compile("[&§]([0-9A-Fa-fK-Ok-oRr])", Pattern.CASE_INSENSITIVE);

    /**
     * Get the singular or plural of a String depending on the amount of the item
     *
     * @param str The singular
     * @param amount The amount of the object
     *
     * @return The singular or plural of the specified string, depending on the amount specified
     *
     * @see StringUtils#getPlural(String, String, int)
     */
    public static String getPlural(String str, int amount) {
        return getPlural(str, str + (str.endsWith("s") ? "'" : "s"), amount);
    }

    /**
     * Get the singular or plural of a String depending on the amount of the item
     *
     * @param singular The singular
     * @param plural The plural
     * @param amount The amount of the object
     *
     * @return The singular or plural of the specified string, depending on the amount specified
     */
    public static String getPlural(String singular, String plural, int amount) {
        return amount == 1 ? singular : plural;
    }

    /**
     * Get the article for a noun
     *
     * @param str The noun
     *
     * @return The article
     */
    public static String getArticle(String str) {
        return getArticle(str.charAt(0));
    }

    public static String replaceLast(String str, String toReplace, String replaceWith) {
        int start = str.lastIndexOf(toReplace);
        return str.substring(0, start) +
                replaceWith +
                str.substring(start + toReplace.length());
    }

    /**
     * Get the article for a noun
     *
     * @param c The first letter of the noun
     *
     * @return The article
     */
    public static String getArticle(char c) {
        switch (Character.toLowerCase(c)) {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
                return "An";
            default:
                return "A";
        }
    }

    /**
     * Get whether a string is empty (whitespace and null is classed as empty)
     *
     * @param str The string
     *
     * @return {@code true} if the string is null or contains nothing but whitespace, {@code false otherwise}
     */
    public static boolean isEmpty(String str) {
        return str == null || str.replaceAll("\\s+", "").equalsIgnoreCase("");
    }

    /**
     * Strip all text formatting that minecraft uses (colors and format such as bold etc.)
     *
     * @param str The string to strip
     *
     * @return The string, without any formatting codes
     */
    public static String stripFormatting(String str) {
        if (str == null) {
            return str;
        }

        return PATTERN_FORMAT_CODE.matcher(str).replaceAll("");
    }

    /**
     * Strip all non-alphanumerics from a string
     *
     * @param str The string
     *
     * @return The string, with all non-alphanumerics stripped out
     */
    public static String stripNonAlphanumerics(String str) {
        return StringUtils.PATTERN_NON_ALPHANUMERICS.matcher(str).replaceAll("");
    }

    /**
     * Get a characters similar characters (case in-senstive)
     *
     * @param c The character to get associations
     *
     * @return The character passed to it and all similar characters as a String
     */
    public static String getSimilarCharacters(char c) {
        return SIMILAR_CHARACTER_MAP.getOrDefault(Character.toLowerCase(c), Character.toString(c));
    }

    /**
     * Joins elements of a String array with the glue between them into a String
     *
     * @param array of elements to join together
     * @param glue what to put between each element
     *
     * @return Concacted Array combined with glue
     */
    public static String join(String[] array, String glue) {
        return join(Arrays.asList(array), glue);
    }

    /**
     * Joins elements of a String array with the glue between them into a String
     *
     * @param array of elements to join together
     * @param glue what to put between each element
     * @param startIndex what index to start combining from
     * @param startIndex what index to end combining from
     *
     * @return Concacted Array combined with glue
     */
    public static String join(String[] array, String glue, int startIndex, int endIndex) {
        return join(Arrays.asList(array), glue, startIndex, endIndex);
    }

    /**
     * Joins elements of a String array with the glue between them into a String
     * @param list of enchants to join together
     * @param glue what to put between each element
     *
     * @return Concacted Array combined with glue
     */
    public static String join(Collection<String> list, String glue) {
        return join(list, glue, 0, list.size() - 1);
    }

    public static String join (Enum[] enums, String glue) {
        return join(Arrays.stream(enums).map(Enum::name).collect(Collectors.toList()), glue);
    }

    /**
     * Joins elements of a String array with the glue between them into a String
     * @param list of enchants to join together
     * @param glue what to put between each element
     * @param startIndex what index to start combining from
     * @param startIndex what index to end combining from
     *
     * @return Concacted Array combined with glue
     */
    public static String join(Collection<String> list, String glue, int startIndex, int endIndex) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> iter = list.iterator();
        int i = 0;
        while (iter.hasNext()) {
            String s = iter.next();
            if (i < startIndex) {
                i++;
                continue;
            }
            sb.append(s);
            if (i != endIndex)
                sb.append(glue);
            i++;
        }
        return sb.toString();
    }

    /**
     * Add similar characters to the similarity map
     *
     * @param map The character map
     * @param chars The characters that are similar
     */
    private static void addSimilarCharacters(Map<Character, String> map, char... chars) {
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            sb.append(c);
        }

        String combined = sb.toString();

        for (char c : chars) {
            map.put(c, combined);
        }
    }

    /**
     * Generate a random alphanumerical String of specified length
     * Code adapted from: https://www.baeldung.com/java-random-string
     */

    public static String getRandomString(int length) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString().toLowerCase();
    }

    static {
        // Populate character similarity map
        Map<Character, String> map = new HashMap<>();

        addSimilarCharacters(map, 'a', '@');
        addSimilarCharacters(map, 'e', '£', '3');
        addSimilarCharacters(map, 'i', 'l', '1', '!', '|');
        addSimilarCharacters(map, 'o', '0');
        addSimilarCharacters(map, 's', '$');

        SIMILAR_CHARACTER_MAP = Collections.unmodifiableMap(map);
    }

}
