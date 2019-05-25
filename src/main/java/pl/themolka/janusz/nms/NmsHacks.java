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
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

public class NmsHacks implements INmsHacks {
    private UnsupportedOperationException no() {
        return new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BaseComponent[] getSignContent(Sign sign) {
        throw this.no();
    }

    @Override
    public void updateBlockState(BlockState blockState) {
        throw this.no();
    }
}
