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

package pl.themolka.janusz.nms;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import pl.themolka.janusz.JanuszPlugin;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

public class NmsHacksHandler extends JanuszPlugin.Handler implements INmsHacks {
    public static final String UNSUPPORTED = ChatColor.RED + "This server implementation (or this feature) is not supported!";

    private final JanuszPlugin plugin;

    private NmsHacks backend;

    public NmsHacksHandler(JanuszPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @Override
    public void enable(JanuszPlugin plugin) {
        String serverVersion = this.getServerVersion().orElse(null);
        if (serverVersion == null) {
            this.plugin.getLogger().log(Level.SEVERE, "This server implementation is not supported!");
            return;
        }

        this.backend = this.resolveHackImpl(serverVersion).orElse(INmsHacks.NULL);

        super.enable(plugin);
    }

    @Override
    public void disable(JanuszPlugin plugin) {
        super.disable(plugin);
        this.backend = null;
    }

    private Optional<String> getServerVersion() {
        String[] path = this.plugin.getServer().getClass().getPackage().getName().split("\\.");
        if (path.length != 4) {
            return Optional.empty();
        }

        return Optional.of(path[3]);
    }

    private Optional<NmsHacks> resolveHackImpl(String serverVersion) {
        switch (Objects.requireNonNull(serverVersion, "serverVersion")) {
            case "v1_14_R1":
                return Optional.of(new V1_14_R1(serverVersion));
        }

        return Optional.empty();
    }

    private <T> T test(T t) {
        return Objects.requireNonNull(t, "Returned value cannot be null!");
    }

    //
    // NMS Hacks
    //

    @Override
    public BaseComponent[] getSignContent(Sign sign) {
        Objects.requireNonNull(sign, "sign");
        return this.test(this.backend.getSignContent(sign));
    }

    @Override
    public void updateBlockState(BlockState blockState) {
        Objects.requireNonNull(blockState, "blockState");
        this.backend.updateBlockState(blockState);
    }
}
