package com.blitzoffline.stringexpansion;

import org.bukkit.ChatColor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.Configurable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class StringExpansion extends PlaceholderExpansion implements Configurable {

    private final Map<String, ReplacementConfiguration> replacementConfigurations = new HashMap<>();
    private final String separator;

    public StringExpansion() {
        this.separator = Pattern.quote(getString("separator", "_"));

        final ConfigurationSection replacementSection = getConfigSection("replacements");
        if (replacementSection == null) {
            return;
        }

        for (final String configurationName : replacementSection.getKeys(false)) {
            final ConfigurationSection configuration = replacementSection.getConfigurationSection(configurationName);

            if (configuration == null) {
                continue;
            }

            final List<String> searchList = new ArrayList<>(configuration.getKeys(false));
            final String[] replacementList = searchList
                    .stream()
                    .map(it -> configuration.getString(it, ""))
                    .toArray(String[]::new);

            this.replacementConfigurations.put(configurationName, new ReplacementConfiguration(searchList.toArray(new String[0]), replacementList));
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "string";
    }

    @Override
    public @NotNull String getAuthor() {
        return "BlitzOffline";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.7";
    }

    @Override
    public Map<String, Object> getDefaults() {
        return ImmutableMap.<String, Object>builder()
                .put("separator", "_")
                .put("replacements.small-numbers.0", "₀")
                .put("replacements.small-numbers.1", "₁")
                .put("replacements.small-numbers.2", "₂")
                .put("replacements.small-numbers.3", "₃")
                .put("replacements.small-numbers.4", "₄")
                .put("replacements.small-numbers.5", "₅")
                .put("replacements.small-numbers.6", "₆")
                .put("replacements.small-numbers.7", "₇")
                .put("replacements.small-numbers.8", "₈")
                .put("replacements.small-numbers.9", "₉")
                .build();
    }

    private String getBoolean(boolean b) {
        return b ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String args) {
        final String[] parts = args.split(separator, 2);

        if (parts.length <= 1) {
            return null;
        }

        String action = parts[0].toLowerCase(Locale.ENGLISH);
        String arguments = PlaceholderAPI.setBracketPlaceholders(player, parts[1]);

        String[] split;

        switch (action) {
            case "equals":
                split = arguments.split(separator, 2);
                if (split.length < 2) {
                    return null;
                }
                return getBoolean(split[0].equals(split[1]));
            case "equalsignorecase":
                split = arguments.split(separator, 2);
                if (split.length < 2) {
                    return null;
                }
                return getBoolean(split[0].equalsIgnoreCase(split[1]));
            case "contains":
                split = arguments.split(separator, 2);
                if (split.length < 2) {
                    return null;
                }
                return getBoolean(split[0].contains(split[1]));
            case "containsignorecase":
                split = arguments.split(separator, 2);
                if (split.length < 2) {
                    return null;
                }
                return getBoolean(StringUtils.containsIgnoreCase(split[0], split[1]));
            case "charat":
                split = arguments.split(separator, 2);
                if (split.length < 2) {
                    return null;
                }
                if (Integer.parseInt(split[0]) >= split[1].length()) {
                    return "";
                }
                return String.valueOf(split[1].charAt(Integer.parseInt(split[0])));
            case "indexof":
                split = arguments.split(separator, 2);
                if (split.length < 2) {
                    return null;
                }
                return String.valueOf(split[0].indexOf(split[1]));
            case "lastindexof":
                split = arguments.split(separator, 2);
                if (split.length < 2) {
                    return null;
                }
                return String.valueOf(split[0].lastIndexOf(split[1]));
            case "substring":
                split = arguments.split(separator, 2);
                if (split.length < 2) {
                    return null;
                }
                if (!split[0].contains(",")) {
                    int index = Integer.parseInt(split[0]);
                    int absIndex = Math.abs(index);
                    int textLength = split[1].length();

                    if (absIndex > textLength) {
                        return "";
                    }

                    if (index < 0) {
                        return split[1].substring(0, textLength - absIndex);
                    }

                    return split[1].substring(index);
                }

                String[] indexes = split[0].split(",", 2);

                int firstIndex = Integer.parseInt(indexes[0]);
                int secondIndex = Integer.parseInt(indexes[1]);

                int textLength = split[1].length();

                if (firstIndex < 0) {
                    firstIndex = 0;
                }

                if (firstIndex >= textLength) {
                    return "";
                }

                if (secondIndex > textLength) {
                    secondIndex = textLength;
                }

                if (secondIndex < 0) {
                    secondIndex = textLength + secondIndex;
                }

                if (firstIndex >= secondIndex) {
                    return "";
                }

                return split[1].substring(firstIndex, secondIndex);
            case "random":
                split = arguments.split(",");
                int random = (int) Math.floor(Math.random()*(split.length));
                return split[random];
            case "replacecharacters":
                split = arguments.split(separator, 2);
                final ReplacementConfiguration configuration = replacementConfigurations.get(split[0]);
                return configuration == null ? split[1] : configuration.replace(split[1]);
            case "shuffle":
                List<String> letters = Arrays.asList(arguments.split(""));
                Collections.shuffle(letters);
                return String.join("", letters);
            case "uppercase":
                return arguments.toUpperCase(Locale.ENGLISH);
            case "lowercase":
                return arguments.toLowerCase(Locale.ENGLISH);
            case "capitalize":
                return arguments.substring(0, 1).toUpperCase() + arguments.substring(1);
            case "sentencecase":
                return arguments.substring(0, 1).toUpperCase() + arguments.substring(1).toLowerCase(Locale.ENGLISH);
            case "length":
                return String.valueOf(arguments.length());
            case "alternateuppercase":
                char[] tempString = arguments.toLowerCase(Locale.ENGLISH).toCharArray();
                for(int index = 0; index < tempString.length; index+=2)
                    tempString[index] = Character.toUpperCase(tempString[index]);
                return String.valueOf(tempString);
            case "startswith":
                split = arguments.split(separator, 2);
                return String.valueOf(split[0].startsWith(split[1]));
            case "endswith":
                split = arguments.split(separator, 2);
                return String.valueOf(split[0].endsWith(split[1]));
            case "trim":
                return arguments.trim();
            case "occurences":
            case "occurrences":
                split = arguments.split(separator, 3);
                if (split.length < 3) {
                    return null;
                }

                if (!split[0].toLowerCase(Locale.ENGLISH).equals("count")) {
                    return null;
                }

                return String.valueOf(StringUtils.countOccurrences(split[1], split[2]));
            case "stripcolor":
            return ChatColor.stripColor(arguments);
            case "ignorecolor":
                split = arguments.split(separator, 2);
                if (split.length < 2) {
                    return null;
                };
                
                int limit;
                try {
                    limit = Integer.parseInt(split[0]);
                } catch (NumberFormatException e) {
                    return null;
                };
                
                String textToLimit = split[1];
                String cleanedText = ChatColor.stripColor(textToLimit);
    
                if (cleanedText.length() <= limit) {
                    return textToLimit; 
                } else {
                    StringBuilder result = new StringBuilder();
                    int visibleCharsCount = 0;
                    boolean inColorCode = false;
    
                    for (int i = 0; i < textToLimit.length(); i++) {
                        char c = textToLimit.charAt(i);
    
                        if (c == ChatColor.COLOR_CHAR) {
                            result.append(c);
                            inColorCode = true;
                            if (i + 1 < textToLimit.length()) {
                                result.append(textToLimit.charAt(i + 1));
                                i++;
                            }
                        } else if (inColorCode) {
                            inColorCode = false;
                            result.append(c);
                            visibleCharsCount++;
                        } else {
                            result.append(c);
                            visibleCharsCount++;
                        }
    
                        if (visibleCharsCount >= limit) {
                            break;
                        }
                    }
                    return result.toString();
                }
        }

        return null;
    }
}
