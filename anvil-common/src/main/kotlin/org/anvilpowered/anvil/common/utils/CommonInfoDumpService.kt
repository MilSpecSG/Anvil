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

package org.anvilpowered.anvil.common.utils

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.inject.Inject
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.util.concurrent.GlobalEventExecutor
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import org.anvilpowered.anvil.api.Anvil
import org.anvilpowered.anvil.api.Environment
import org.anvilpowered.anvil.api.plugin.BasicPluginInfo
import org.anvilpowered.anvil.api.registry.Key
import org.anvilpowered.anvil.api.registry.Keys
import org.anvilpowered.anvil.api.registry.Registry
import org.anvilpowered.anvil.api.util.InfoDumpService
import org.anvilpowered.anvil.api.util.TextService
import org.asynchttpclient.Dsl.asyncHttpClient
import org.asynchttpclient.Dsl.config
import org.asynchttpclient.filter.FilterContext
import org.asynchttpclient.filter.RequestFilter
import org.slf4j.Logger

class CommonInfoDumpService<TString, TCommandSource> : InfoDumpService<TCommandSource> {

  @Inject
  private lateinit var logger: Logger

  @Inject
  private lateinit var textService: TextService<TString, TCommandSource>

  private val gson = GsonBuilder()
    .setPrettyPrinting()
    .excludeFieldsWithoutExposeAnnotation()
    .setLenient()
    .create()

  private fun collectInformation(environment: Environment): JsonElement {
    val result = JsonObject()
    val pluginInfo = JsonObject()
    val info = environment.injector.getInstance(BasicPluginInfo::class.java)
    pluginInfo.addProperty("version", info.version)
    pluginInfo.addProperty("description", info.description)
    pluginInfo.addProperty("url", info.url)
    pluginInfo.addProperty("authors", info.authors.joinToString(", "))
    pluginInfo.addProperty("buildDate", info.buildDate)
    result.add("pluginInfo", pluginInfo)
    val keys = JsonObject()
    for (it in Keys.getAll(environment.name)) {
      if (it.value.isSensitive) {
        keys.addProperty(it.key, "***")
      } else {
        keys.addProperty(it.key, toString(it.value, environment.registry))
      }
    }
    result.add("keys", keys)
    return result
  }

  private fun collectSystemInfo(): JsonElement {
    val sysInfo = JsonObject()
    sysInfo.addProperty("os", System.getProperty("os.name"))
    sysInfo.addProperty("osVersion", System.getProperty("os.version"))
    sysInfo.addProperty("architecture", System.getProperty("os.arch"))
    sysInfo.addProperty("javaVersion", System.getProperty("java.version"))
    sysInfo.addProperty("javaVendor", System.getProperty("java.vendor"))
    sysInfo.addProperty("platform", Anvil.getPlatform().name)
    return sysInfo
  }

  override fun publishInfo(source: TCommandSource) {
    val data = JsonObject()
    val plugins = JsonObject()
    data.add("system", collectSystemInfo())
    plugins.add("anvil", collectInformation(Anvil.getEnvironment()))
    for (env in Anvil.getEnvironmentManager().environments.values) {
      if (env.name == "anvil") {
        continue
      }
      plugins.add(env.name, collectInformation(env))
    }
    data.add("plugins", plugins)
    publish(source, data.asJsonObject)
  }

  override fun publishInfo(source: TCommandSource, vararg environments: Environment) {
    val data = JsonObject()
    val plugins = JsonObject()
    data.add("system", collectSystemInfo())
    // Always add the Anvil environment, regardless of whether it is in the provided environment vararg
    plugins.add("anvil", collectInformation(Anvil.getEnvironment()))
    for (env in environments) {
      if (env.name == "anvil") {
        continue
      }
      plugins.add(env.name, collectInformation(env))
    }
    data.add("plugins", plugins)
    publish(source, data.asJsonObject)
  }

  private fun publish(source: TCommandSource, data: JsonObject) {
    val readable = GsonBuilder()
      .setPrettyPrinting()
      .create().toJson(data)
    val client = asyncHttpClient(
      config()
        .setEventLoopGroup(NioEventLoopGroup(1))
        .setUserAgent("AnvilPowered")
        .addRequestFilter(object : RequestFilter {
          override fun <T> filter(ctx: FilterContext<T>): FilterContext<T> {
            return FilterContext.FilterContextBuilder(ctx).request(
              ctx.request.toBuilder()
                .setNameResolver(AnvilInetNameResolver(GlobalEventExecutor.INSTANCE))
                .build()
            ).build()
          }
        })
        .setFollowRedirect(true)
        .build()
    )
    client.preparePost("http://dump.anvilpowered.org/dump")
      .setHeader("Content-Type", "text/plain")
      .addHeader("AnvilPowered", "Dump")
      .setBody(readable.toByteArray(StandardCharsets.UTF_8))
      .execute()
      .toCompletableFuture()
      .exceptionally {
        logger.error("An error occurred posting dump", it)
        null
      }
      .thenApplyAsync { response ->
        try {
          if (response.statusCode != 200) {
            logger.error(
              """
              An error occurred while attempting to post your dump.
              The server may be down at this time or there is an issue with your internet connection.
              If you believe this may be a bug, please contact the Anvil discord server.
              """.trimIndent()
            )
            return@thenApplyAsync
          }

          val key = parse(response.getResponseBody(StandardCharsets.UTF_8))
          check(key.has("key")) { "URL response missing!" }
          val url = "http://dump.anvilpowered.org/${key.get("key").asString}.json"
          textService.builder()
            .appendPrefix()
            .green()
            .append("If a developer has requested you run this command, please provide them with the following link:\n")
            .gold().append(url).onClickOpenUrl(url)
            .sendTo(source)
        } catch (e: UnknownHostException) {
          logger.error("A connection error occurred while posting your dump", e)
        } catch (e: JsonParseException) {
          logger.error("An error occurred causing the dump to not be completed.", e)
        }
      }
  }

  private fun parse(string: String): JsonObject {
    check(string.isNotEmpty()) { "An error occurred while posting the data" }
    return gson.fromJson(string, JsonObject::class.java)
  }

  fun <T> toString(key: Key<T>, registry: Registry): String = key.toString(registry.getOrDefault(key))
}
