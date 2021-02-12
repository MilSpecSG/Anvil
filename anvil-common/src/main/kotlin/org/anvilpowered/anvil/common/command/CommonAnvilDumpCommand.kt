/*
 * Anvil - AnvilPowered
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
 *     along with this program.  If not, see https://www.gnu.org/licenses/.
 */

package org.anvilpowered.anvil.common.command

import com.google.inject.Inject
import java.util.Optional
import org.anvilpowered.anvil.api.Anvil
import org.anvilpowered.anvil.api.Environment
import org.anvilpowered.anvil.api.misc.Named
import org.anvilpowered.anvil.api.registry.Keys
import org.anvilpowered.anvil.api.registry.Registry
import org.anvilpowered.anvil.api.util.InfoDumpService
import org.anvilpowered.anvil.api.util.PermissionService
import org.anvilpowered.anvil.api.util.TextService
import org.anvilpowered.anvil.common.plugin.AnvilPluginMessages
import java.util.stream.Collectors

open class CommonAnvilDumpCommand<TString, TCommandSource> {
  @Inject
  private lateinit var dumpService: InfoDumpService<TCommandSource>

  @Inject
  private lateinit var permissionService: PermissionService

  @Inject
  private lateinit var pluginMessages: AnvilPluginMessages<TString, TCommandSource>

  @Inject
  private lateinit var registry: Registry

  @Inject
  private lateinit var textService: TextService<TString, TCommandSource>

  open fun execute(source: TCommandSource, context: Array<String>) {
    if (!permissionService.hasPermission(source, registry.getOrDefault(Keys.DUMP_PERMISSION))) {
      textService.send(pluginMessages.noPermission, source)
      return
    }
    if (context.isEmpty()) {
      textService.builder()
        .appendPrefix()
        .red().append("Plugin is required if '--all' is not set")
        .sendTo(source)
      return
    }
    if ("-a" == context[0] || "--all" == context[0]) {
      dumpService.publishInfo(source)
      return
    } else {
      dumpDirect(source, context)
    }
  }

  open fun suggest(source: TCommandSource, context: Array<String>): List<String> {
    val suggestions = Anvil.getEnvironmentManager()
      .environments.values.stream()
      .map(Named::getName)
      .sorted().collect(Collectors.toList())
    suggestions.add("--all")
    return suggestions
  }

  private fun dumpDirect(source: TCommandSource, plugins: Array<String>): Boolean {
    val optionalEnvironments: MutableList<Optional<Environment>> = mutableListOf()
    for (environment in Anvil.getEnvironmentManager().environments.values) {
      if (plugins.contains(environment.name)) {
        optionalEnvironments.add(Optional.of(environment))
      }
    }
    if (optionalEnvironments.isEmpty()) {
      textService.builder()
        .appendPrefix()
        .red().append("Could not find plugin(s) ")
        .gold().append(plugins.joinToString(separator = " "))
        .sendTo(source)
      return false
    }
    val environments: MutableList<Environment> = mutableListOf()
    for (env in optionalEnvironments) {
      environments.add(env.get())
    }
    dumpService.publishInfo(source, *environments.toTypedArray())
    return true
  }
}
