/*
 * GNU General Public License version 2
 *
 * Copyright (C) 2019-2021 JetBrains s.r.o.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package org.jetbrains.projector.server.util

import org.jetbrains.projector.awt.PWindow
import org.jetbrains.projector.awt.data.AwtImageInfo
import org.jetbrains.projector.awt.data.AwtPaintType
import org.jetbrains.projector.awt.data.Direction
import org.jetbrains.projector.common.protocol.data.ImageEventInfo
import org.jetbrains.projector.common.protocol.data.PaintType
import org.jetbrains.projector.common.protocol.toClient.WindowType
import org.jetbrains.projector.common.protocol.toServer.ResizeDirection
import javax.swing.Popup

fun AwtPaintType.toPaintType() = when (this) {
  AwtPaintType.DRAW -> PaintType.DRAW
  AwtPaintType.FILL -> PaintType.FILL
}

fun AwtImageInfo.toImageEventInfo() = when (this) {
  is AwtImageInfo.Point -> ImageEventInfo.Xy(x = x, y = y)
  is AwtImageInfo.Rectangle -> ImageEventInfo.XyWh(x = x, y = y, width = width, height = height, argbBackgroundColor = argbBackgroundColor)
  is AwtImageInfo.Area -> ImageEventInfo.Ds(
    dx1 = dx1, dy1 = dy1, dx2 = dx2, dy2 = dy2,
    sx1 = sx1, sy1 = sy1, sx2 = sx2, sy2 = sy2,
    argbBackgroundColor = argbBackgroundColor
  )
  is AwtImageInfo.Transformation -> ImageEventInfo.Transformed(tx)
}

val PWindow.windowType: WindowType
  get() = when {
    "IdeFrameImpl" in target::class.java.simpleName -> WindowType.IDEA_WINDOW
    target is Popup -> WindowType.POPUP
    else -> WindowType.WINDOW
  }

fun ResizeDirection.toDirection() = when (this) {
  ResizeDirection.NW -> Direction.NW
  ResizeDirection.SW -> Direction.SW
  ResizeDirection.NE -> Direction.NE
  ResizeDirection.SE -> Direction.SE
  ResizeDirection.N -> Direction.N
  ResizeDirection.W -> Direction.W
  ResizeDirection.S -> Direction.S
  ResizeDirection.E -> Direction.E
}
