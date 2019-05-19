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

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Sign;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.themolka.janusz.JanuszPlugin;
import pl.themolka.janusz.profile.LocalSession;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class ReplaceSignCommand extends JanuszPlugin.CommandHandler {
    private static final int MAX_DISTANCE = 5;

    public ReplaceSignCommand() {
        super("replacesign");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(LocalSession.ERROR + "Nie jesteś graczem!");
            return true;
        } else if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Nie podano nowego materiału!");
            sender.sendMessage(ChatColor.RED + "Użycie: /" + label + " <material>");
            return true;
        }

        Player player = (Player) sender;
        String input = args[0];

        // resolve input
        Material material = Material.matchMaterial(input, false);
        if (material == null) {
            player.sendMessage(ChatColor.RED + "'" + input + "' nie jest znanym mi materiałem.");
            return true;
        } else if (!Tag.SIGNS.isTagged(material)) {
            player.sendMessage(ChatColor.RED + "'" + this.prettyMaterial(material) + "' nie jest tabliczką.");
            return true;
        }

        // resolve target block
        Block target = player.getTargetBlockExact(MAX_DISTANCE);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Brak bloku w zasięgu!");
            return true;
        }

        Material oldMaterial = target.getType();
        if (!Tag.SIGNS.isTagged(oldMaterial)) {
            player.sendMessage(ChatColor.RED + "Blok w zasięgu nie jest tabliczką.");
            return true;
        } else if (Objects.equals(oldMaterial, material)) {
            player.sendMessage(ChatColor.RED + "Tabliczka jest już '" + this.prettyMaterial(material) + "'.");
            return true;
        }

        // replace block data
        BlockData oldBlockData = target.getBlockData().clone();
        BlockData newBlockData = this.createNewBlockData(oldBlockData, material);

        BlockState oldBlockState = target.getState();

        target.setBlockData(newBlockData, false);
        boolean ok = this.mergeState(oldBlockState, target.getState());

        if (ok) {
            player.sendMessage(ChatColor.GREEN + "Tabliczka '" + this.prettyMaterial(oldMaterial) +
                    "' jest teraz '" + this.prettyMaterial(material) + "'.");
        } else {
            player.sendMessage(ChatColor.RED + "Nie udało się skopiować zawartości tabliczki. :(");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.US);
            return Tag.SIGNS.getValues().stream().map(material -> material.getKey().toString())
                    .filter(key -> key.toLowerCase(Locale.US).startsWith(input))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private String prettyMaterial(Material material) {
        return Objects.requireNonNull(material, "material").getKey().toString();
    }

    private BlockData createNewBlockData(BlockData oldData, Material material) {
        Objects.requireNonNull(oldData, "oldData");
        Objects.requireNonNull(material, "material");
        this.validateBlockData(oldData);

        BlockData newData = material.createBlockData();
        this.validateBlockData(newData);

        // common
        ((Waterlogged) newData).setWaterlogged(((Waterlogged) oldData).isWaterlogged());

        // for Sign only
        if (newData instanceof Sign && oldData instanceof Sign) {
            ((Sign) newData).setRotation(((Sign) oldData).getRotation());
        }

        // for WallSign only
        if (newData instanceof WallSign && oldData instanceof WallSign) {
            ((WallSign) newData).setFacing(((WallSign) oldData).getFacing());
        }

        return newData;
    }

    private void validateBlockData(BlockData blockData) {
        Objects.requireNonNull(blockData, "blockData");
        if (!(blockData instanceof Sign) && !(blockData instanceof WallSign)) {
            throw new IllegalArgumentException("blockData must be an instance of Sign or WallSign.");
        }
    }

    private boolean mergeState(BlockState from, BlockState to) {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");

        if (!(from instanceof org.bukkit.block.Sign) || !(to instanceof org.bukkit.block.Sign)) {
            throw new IllegalArgumentException("from and to must be an instance of Sign");
        }

        org.bukkit.block.Sign fromSign = (org.bukkit.block.Sign) from;
        org.bukkit.block.Sign toSign = (org.bukkit.block.Sign) to;

        String[] lines = fromSign.getLines();
        for (int i = 0; i < lines.length; i++) {
            toSign.setLine(i, lines[i]);
        }

        return toSign.update(false, true);
    }
}
