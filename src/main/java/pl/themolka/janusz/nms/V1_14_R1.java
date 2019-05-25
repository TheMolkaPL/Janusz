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
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class V1_14_R1 extends DedicatedNmsHacks {
    public V1_14_R1(String serverVersion) {
        super(serverVersion);

        this.init_getSignContent();
        this.init_updateBlockState();
    }

    //
    //

    private Field craftBlockEntityState_tileEntity;
    private Field tileEntitySign_lines;
    private Method iChatBaseComponent_chatSerializer_a;

    void init_getSignContent() {
        this.craftBlockEntityState_tileEntity = this.getField(this.getOcbClass("block.CraftBlockEntityState"), "tileEntity");
        this.tileEntitySign_lines = this.getField(this.getNmsClass("TileEntitySign"), "lines");
        this.iChatBaseComponent_chatSerializer_a = this.getMethod(this.getNmsClass("IChatBaseComponent$ChatSerializer"), "a",
                this.getNmsClass("IChatBaseComponent"));
    }

    @Override
    public BaseComponent[] getSignContent(Sign sign) {
        try {
            Object tileEntity = this.craftBlockEntityState_tileEntity.get(sign);
            Object lines = this.tileEntitySign_lines.get(tileEntity);

            BaseComponent[] components = new BaseComponent[Array.getLength(lines)];

            for (int i = 0; i < components.length; i++) {
                Object iChatBaseComponent = Array.get(lines, i);
                Object asJsonString = this.iChatBaseComponent_chatSerializer_a.invoke(null, iChatBaseComponent);
                components[i] = ComponentSerializer.parse((String) asJsonString)[0];
            }

            return components;
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

    //
    //

    // TODO rework
    private Method tileEntity_update;

    void init_updateBlockState() {
        this.tileEntity_update = this.getMethod(this.getNmsClass("TileEntity"), "update");
    }

    @Override
    public void updateBlockState(BlockState blockState) {
        try {
            Object tileEntity = this.craftBlockEntityState_tileEntity.get(blockState);
            this.tileEntity_update.invoke(tileEntity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
