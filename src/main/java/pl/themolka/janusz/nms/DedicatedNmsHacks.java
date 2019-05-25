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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

public class DedicatedNmsHacks extends NmsHacks {
    private final String serverVersion;

    public DedicatedNmsHacks(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public String getServerVersion() {
        return this.serverVersion;
    }

    public Class<?> getClass(String className) {
        Objects.requireNonNull(className, "className");
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Class<?> getBukkitClass(String className) {
        Objects.requireNonNull(className, "className");
        return this.getClass("org.bukkit." + className);
    }

    public Class<?> getNmsClass(String className) {
        Objects.requireNonNull(className, "className");
        return this.getClass("net.minecraft.server." + this.serverVersion + "." + className);
    }

    public Class<?> getOcbClass(String className) {
        Objects.requireNonNull(className, "className");
        return this.getClass("org.bukkit.craftbukkit." + this.serverVersion + "." + className);
    }

    public Field getField(Class<?> clazz, String name) {
        Objects.requireNonNull(clazz, "clazz");
        Objects.requireNonNull(name, "name");

        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        Objects.requireNonNull(clazz, "clazz");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(parameterTypes, "parameterTypes");

        try {
            return clazz.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }
}
