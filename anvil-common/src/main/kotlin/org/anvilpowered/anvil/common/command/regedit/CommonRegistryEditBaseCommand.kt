/*
 *   Anvil - AnvilPowered
 *   Copyright (C) 2020-2021
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

package org.anvilpowered.anvil.common.command.regedit

import com.google.inject.Inject
import org.anvilpowered.anvil.api.command.SimpleCommand
import org.anvilpowered.anvil.api.plugin.PluginInfo
import org.anvilpowered.anvil.api.plugin.PluginMessages
import org.anvilpowered.anvil.api.registry.Keys
import org.anvilpowered.anvil.api.registry.Registry
import org.anvilpowered.anvil.api.util.PermissionService
import org.anvilpowered.anvil.api.util.TextService
import org.anvilpowered.anvil.api.util.UserService

abstract class CommonRegistryEditBaseCommand<TUser, TPlayer, TString, TCommandSource> :
  SimpleCommand<TString, TCommandSource> {

    @Inject
    protected lateinit var registry: Registry

    @Inject
    protected lateinit var permissionService: PermissionService

    @Inject
    protected lateinit var pluginInfo: PluginInfo<TString>

    @Inject
    protected lateinit var pluginMessages: PluginMessages<TString>

    @Inject
    protected lateinit var textService: TextService<TString, TCommandSource>

    @Inject
    protected lateinit var userService: UserService<TUser, TPlayer>

  override fun canExecute(source: TCommandSource): Boolean {
    return permissionService.hasPermission(source, registry.getOrDefault(Keys.REGEDIT_PERMISSION))
  }
}
