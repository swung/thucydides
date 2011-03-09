package net.thucydides.core.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.util.StringUtils;

/**
 * Utility class to convert test case and method names into human-readable form.
 * 
 * @author johnsmart
 *
 */
public final class NameConverter {

    private static final Pattern CAMEL_CASE_MATCHER_PATTERN = Pattern.compile("\\p{Lu}");

    private NameConverter() {}
    
    /**
     * Converts a class or method name into a human-readable sentence.
     */
    public static String humanize(final String name) {
        if (name.contains(" ") && !name.contains(":")) {
            return name;
        } else {
            String noUnderscores = name.replaceAll("_", " ");
            String splitCamelCase = splitCamelCase(noUnderscores);
            return StringUtils.capitalizeFirstLetter(splitCamelCase.toLowerCase());
        }
    }

    /**
     * Inserts spaces between words in a CamelCase name.
     */
    public static String splitCamelCase(final String name) {
        Matcher m = CAMEL_CASE_MATCHER_PATTERN.matcher(name);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, " " + m.group());
        }
        m.appendTail(sb);
        return sb.toString().trim();
    }

    /**
     * Transform a camel-case word to underscored-version.
     */
    public static String underscore(final String name) {
        return  name.replaceAll(" ", "_").toLowerCase().trim();
    }

}