/*
 *   Anvil - AnvilPowered
 *   Copyright (C) 2020
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.anvilpowered.anvil.nukkit.module;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import com.google.inject.TypeLiteral;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.anvilpowered.anvil.api.command.CommandNode;
import org.anvilpowered.anvil.common.command.regedit.CommonRegistryEditRootCommand;
import org.anvilpowered.anvil.common.module.CommonModule;
import org.anvilpowered.anvil.common.plugin.AnvilPluginInfo;
import org.anvilpowered.anvil.nukkit.command.NukkitAnvilCommandNode;
import org.anvilpowered.anvil.nukkit.command.regedit.NukkitRegistryEditRootCommand;

import java.io.File;
import java.nio.file.Paths;

public class NukkitModule extends CommonModule<String, CommandSender> {
    @Override
    protected void configure() {
        super.configure();
        File configFilesLocation = Paths.get("plugins/" + AnvilPluginInfo.id).toFile();
        if (!configFilesLocation.exists()) {
            if (!configFilesLocation.mkdirs()) {
                throw new IllegalStateException("Unable to create config directory");
            }
        }
        bind(new TypeLiteral<ConfigurationLoader<CommentedConfigurationNode>>() {
        }).toInstance(HoconConfigurationLoader.builder().setPath(Paths.get(configFilesLocation + "/anvil.conf")).build());
        bind(new TypeLiteral<CommandNode<CommandSender>>() {
        }).to(NukkitAnvilCommandNode.class);
        bind(new TypeLiteral<CommonRegistryEditRootCommand<Player, Player, String, CommandSender>>() {
        }).to(NukkitRegistryEditRootCommand.class);
    }
}
