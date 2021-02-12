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

import io.netty.resolver.InetNameResolver
import io.netty.util.concurrent.EventExecutor
import io.netty.util.concurrent.Promise
import io.netty.util.internal.SocketUtils
import java.net.InetAddress
import java.net.UnknownHostException

class AnvilInetNameResolver(executor: EventExecutor) : InetNameResolver(executor) {

  override fun doResolve(inetHost: String, promise: Promise<InetAddress>) {
    try {
      promise.setSuccess(SocketUtils.addressByName(inetHost))
    } catch(e: UnknownHostException) {
      e.printStackTrace()
    }
  }

  override fun doResolveAll(inetHost: String, promise: Promise<MutableList<InetAddress>>) {
    try {
      promise.setSuccess(listOf(*SocketUtils.allAddressesByName(inetHost)) as MutableList<InetAddress>?)
    } catch (e: UnknownHostException) {
      promise.setFailure(e)
    }
  }
}
