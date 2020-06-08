/*
 * GNU General Public License version 2
 *
 * Copyright (C) 2019-2020 JetBrains s.r.o.
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
package org.jetbrains.projector.server.idea

import org.jetbrains.projector.common.protocol.data.PaintValue
import org.jetbrains.projector.server.log.Logger
import java.awt.Color
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

/**
 * Class retrieves color info from IDE settings.
 * Subscribe on LafManager and update colors on LAF change.
 * Calls provided in constructor onColorsChanged action on LAF change.
 */
class IdeColors(private val onColorsChanged: (Map<String, PaintValue.Color>) -> Unit) {

  private val logger = Logger(IdeColors::class.simpleName!!)

  var colors = emptyMap<String, PaintValue.Color>()
    private set

  init {
    invokeWhenIdeaIsInitialized("Getting IDE colors") { ideaClassLoader ->
      colors = getColors(ideaClassLoader)
      onColorsChanged(colors)
      subscribeToIdeLafManager(ideaClassLoader)
    }
  }

  private fun subscribeToIdeLafManager(ideaClassLoader: ClassLoader) {

    try {
      val lafManagerClass = Class.forName("com.intellij.ide.ui.LafManager", false, ideaClassLoader)
      val lafManagerListenerClass = Class.forName("com.intellij.ide.ui.LafManagerListener", false, ideaClassLoader)

      val obj = InvocationHandler { _, method, _ ->
        if (method.declaringClass == lafManagerListenerClass && method.name == "lookAndFeelChanged") {
          colors = getColors(ideaClassLoader)
          onColorsChanged(colors)
        }
        null
      }

      val proxy = Proxy.newProxyInstance(ideaClassLoader, arrayOf(lafManagerListenerClass), obj) as Proxy

      val lafManagerInstance = lafManagerClass.getDeclaredMethod("getInstance").invoke(null)
      lafManagerClass.getDeclaredMethod("addLafManagerListener", lafManagerListenerClass).invoke(lafManagerInstance, proxy)
    }
    catch (e: Exception) {
      logger.error(e) { "Failed to subscribe to IDE LAF manager." }
    }
  }

  private fun getColors(ideaClassLoader: ClassLoader): Map<String, PaintValue.Color> {

    try {
      val result = mutableMapOf<String, PaintValue.Color>()

      val popupClass = Class.forName("com.intellij.util.ui.JBUI\$CurrentTheme\$Popup", false, ideaClassLoader)

      val headerBackgroundMethod = popupClass.getDeclaredMethod("headerBackground", Boolean::class.java)
      result["windowHeaderActiveBackground"] = PaintValue.Color((headerBackgroundMethod.invoke(null, true) as Color).rgb)
      result["windowHeaderInactiveBackground"] = PaintValue.Color((headerBackgroundMethod.invoke(null, false) as Color).rgb)

      val borderColorMethod = popupClass.getDeclaredMethod("borderColor", Boolean::class.java)
      result["windowActiveBorder"] = PaintValue.Color((borderColorMethod.invoke(null, true) as Color).rgb)
      result["windowInactiveBorder"] = PaintValue.Color((borderColorMethod.invoke(null, false) as Color).rgb)

      val labelClass = Class.forName("com.intellij.util.ui.JBUI\$CurrentTheme\$Label", false, ideaClassLoader)

      val labelForegroundMethod = labelClass.getDeclaredMethod("foreground")
      result["windowHeaderActiveText"] = PaintValue.Color((labelForegroundMethod.invoke(null) as Color).rgb)
      result["windowHeaderInactiveText"] = result["windowHeaderActiveText"]!!

      return result
    }
    catch (e: Exception) {
      logger.error(e) { "Failed to get IDE color scheme." }
      return emptyMap()
    }
  }
}
