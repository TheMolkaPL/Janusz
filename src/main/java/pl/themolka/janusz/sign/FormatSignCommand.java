/*
 * Copyright 2019 Aleksander Jagiełło <themolkapl@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.themolka.janusz.sign;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.ChatColor;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.nms.NmsHacksHandler;
import pl.themolka.janusz.profile.LocalSession;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FormatSignCommand extends JanuszPlugin.CommandHandler {
    private static final int MAX_DISTANCE = 5;

    private static final Map<String, Format> FORMAT_MAP;

    static {
        ImmutableMap.Builder<String, Format> builder = ImmutableMap.builder();

        Stream.of(
                new BoldFormat(),
                new Color(),
                new Italic(),
                new Obfuscated(),
                new Strikethrough(),
                new Underlined()
        ).sorted().forEach(format -> builder.put(format.name().toLowerCase(Locale.US), format));

        FORMAT_MAP = builder.build();
    }

    private final JanuszPlugin plugin;

    public FormatSignCommand(JanuszPlugin plugin) {
        super("formatsign");

        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(LocalSession.ERROR + "Nie jesteś graczem!");
            return true;
        } else if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Nie podano formatu!");
            sender.sendMessage(ChatColor.RED + "Użycie: /" + label + " <format> <value>");
            return true;
        } else if (args.length == 1) {
            sender.sendMessage(ChatColor.RED + "Nie podano wartości formatu!");
            sender.sendMessage(ChatColor.RED + "Użycie: /" + label + " <format> <value>");
            return true;
        }

        Player player = (Player) sender;
        String formatInput = args[0].toLowerCase(Locale.US);
        String valueInput = args[1];

        // resolve format
        Format format = FORMAT_MAP.get(formatInput);
        if (format == null) {
            player.sendMessage(ChatColor.RED + "'" + formatInput + "' nie jest znanym mi formatem.");
            return true;
        }

        // resolve format value
        String value = null;
        for (String entry : format.values()) {
            if (entry.equalsIgnoreCase(valueInput)) {
                value = entry;
            }
        }

        if (value == null) {
            player.sendMessage(ChatColor.RED + "'" + valueInput + "' nie jest znaną mi wartością dla '" + format.name() + "'.");
            return true;
        }

        // resolve target block
        Block target = player.getTargetBlockExact(MAX_DISTANCE);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Brak bloku w zasięgu!");
            return true;
        } else if (!Tag.SIGNS.isTagged(target.getType())) {
            player.sendMessage(ChatColor.RED + "Blok w zasięgu nie jest tabliczką.");
            return true;
        }

        BlockState blockState = target.getState();
        if (!(blockState instanceof Sign)) {
            throw new IllegalArgumentException(); // should never happen
        }

        // format sign components
        if (this.format(player, (Sign) blockState, format, value)) {
//            blockState.update(false, false);
            player.sendMessage(ChatColor.GREEN + "'" + format.name() + "' jest teraz '" + value + "'.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String formatInput = args[0].toLowerCase(Locale.US);
            return FORMAT_MAP.values().stream()
                    .map(Format::name)
                    .filter(name -> name.toLowerCase(Locale.US).startsWith(formatInput))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            Format format = FORMAT_MAP.get(args[0].toLowerCase(Locale.US));
            if (format != null) {
                String valueInput = args[1].toLowerCase(Locale.US);
                return format.values().stream()
                        .filter(suggestion -> suggestion.toLowerCase(Locale.US).startsWith(valueInput))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    private boolean format(Player sender, Sign sign, Format format, String value) {
        Objects.requireNonNull(sender, "sender");
        Objects.requireNonNull(sign, "sign");
        Objects.requireNonNull(format, "format");
        Objects.requireNonNull(value, "value");

        NmsHacksHandler nmsHacksHandler = this.plugin.getHandler(NmsHacksHandler.class).orElse(null);
        if (nmsHacksHandler == null) {
            sender.sendMessage(ChatColor.RED + "Handler " + NmsHacksHandler.class.getSimpleName() + " nie jest dostępny.");
            return false;
        }

        BaseComponent[] content;
        try {
            content = nmsHacksHandler.getSignContent(sign);
        } catch (UnsupportedOperationException e) {
            sender.sendMessage(NmsHacksHandler.UNSUPPORTED);
            return false;
        }

        for (BaseComponent component : content) {
            format.format(component, value);
        }

        nmsHacksHandler.updateBlockState(sign);
        return true;
    }

    /**
     * Base interface for all component formats.
     */
    interface Format extends Comparable<Format> {
        Set<String> BOOLEANS = ImmutableSet.of(
                Boolean.toString(true),
                Boolean.toString(false));

        String name();

        Set<String> values();

        void format(BaseComponent component, String value);

        @Override
        default int compareTo(Format o) {
            return this.name().compareToIgnoreCase(o.name());
        }
    }

    static class BoldFormat implements Format {
        @Override
        public String name() {
            return "bold";
        }

        @Override
        public Set<String> values() {
            return BOOLEANS;
        }

        @Override
        public void format(BaseComponent component, String value) {
            component.setBold(Boolean.parseBoolean(value));
        }
    }

    static class Color implements Format {
        @Override
        public String name() {
            return "color";
        }

        @Override
        public Set<String> values() {
            return Stream.of(ChatColor.values())
                    .filter(color -> !color.isFormat())
                    .map(color -> color.asBungee().getName())
                    .collect(Collectors.toSet());
        }

        @Override
        public void format(BaseComponent component, String value) {
            net.md_5.bungee.api.ChatColor color = null;

            for (net.md_5.bungee.api.ChatColor entry : net.md_5.bungee.api.ChatColor.values()) {
                if (Objects.equals(entry.getName(), value)) {
                    color = entry;
                    break;
                }
            }

            if (color == null) {
                throw new NullPointerException(); // should never happen
            }

            component.setColor(color);
        }
    }

    static class Italic implements Format {
        @Override
        public String name() {
            return "italic";
        }

        @Override
        public Set<String> values() {
            return BOOLEANS;
        }

        @Override
        public void format(BaseComponent component, String value) {
            component.setItalic(Boolean.parseBoolean(value));
        }
    }

    static class Obfuscated implements Format {
        @Override
        public String name() {
            return "obfuscated";
        }

        @Override
        public Set<String> values() {
            return BOOLEANS;
        }

        @Override
        public void format(BaseComponent component, String value) {
            component.setObfuscated(Boolean.parseBoolean(value));
        }
    }

    static class Strikethrough implements Format {
        @Override
        public String name() {
            return "strikethrough";
        }

        @Override
        public Set<String> values() {
            return BOOLEANS;
        }

        @Override
        public void format(BaseComponent component, String value) {
            component.setStrikethrough(Boolean.parseBoolean(value));
        }
    }

    static class Underlined implements Format {
        @Override
        public String name() {
            return "underlined";
        }

        @Override
        public Set<String> values() {
            return BOOLEANS;
        }

        @Override
        public void format(BaseComponent component, String value) {
            component.setUnderlined(Boolean.parseBoolean(value));
        }
    }
}
