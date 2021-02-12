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

package org.anvilpowered.anvil.sponge.command

import com.google.inject.Inject
import java.util.Optional
import org.anvilpowered.anvil.api.registry.Keys
import org.anvilpowered.anvil.api.registry.Registry
import org.anvilpowered.anvil.common.command.CommonAnvilDumpCommand
import org.anvilpowered.anvil.common.command.regedit.alias
import org.anvilpowered.anvil.common.command.regedit.whitespace
import org.spongepowered.api.command.CommandCallable
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.text.Text
import org.spongepowered.api.world.World

class SpongeAnvilDumpCommand
  : CommonAnvilDumpCommand<Text, CommandSource>(), CommandCallable {

  @Inject
  private lateinit var registry: Registry

  companion object {
    val DESCRIPTION: Optional<Text> = Optional.of(Text.of("Anvil dump command"))
    val USAGE: Text get() = Text.of("/$alias dump  [--all|-a] [<plugin>]")
  }

  override fun process(source: CommandSource, context: String): CommandResult {
    execute(source, context.split(whitespace).toTypedArray())
    return CommandResult.success()
  }

  override fun getSuggestions(
    source: CommandSource,
    context: String,
    targetPosition: org.spongepowered.api.world.Location<World>?
  ): MutableList<String> = suggest(source, context.split(whitespace).toTypedArray()) as MutableList<String>

  override fun testPermission(source: CommandSource): Boolean {
    return source.hasPermission(registry.getOrDefault(Keys.DUMP_PERMISSION))
  }

  override fun getShortDescription(source: CommandSource): Optional<Text> = DESCRIPTION
  override fun getHelp(source: CommandSource): Optional<Text> = DESCRIPTION
  override fun getUsage(source: CommandSource): Text = USAGE
}
