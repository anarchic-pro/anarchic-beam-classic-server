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
@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package org.jetbrains.projector.awt.image

import sun.awt.image.SurfaceManager
import sun.java2d.NullSurfaceData
import sun.java2d.SurfaceData
import java.awt.*
import java.awt.image.BufferedImage
import java.awt.image.ImageObserver
import java.awt.image.VolatileImage
import java.lang.ref.WeakReference

class PVolatileImage(
  private val width: Int,
  private val height: Int,
  transparency: Int,
  private val caps: ImageCapabilities?,
) : VolatileImage() {

  init {
    Image::class.java.getDeclaredField("surfaceManager").apply {
      isAccessible = true

      set(this@PVolatileImage, NULL_SURFACE_MANAGER)
    }
  }

  val id = ++NEXT_ID

  private val graphics = PGraphics2D(
    Descriptor(
      pVolatileImageId = id,
      width = width,
      height = height
    )
  )

  private var valid = true

  init {
    this.transparency = transparency

    synchronized(weakImages) {
      weakImages.add(WeakReference(this))
    }
  }

  constructor(width: Int, height: Int) : this(
    width = width,
    height = height,
    transparency = Transparency.TRANSLUCENT,
    caps = null
  )

  fun invalidate() {
    valid = false
  }

  override fun getSnapshot(): BufferedImage {
    return BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)  // todo
  }

  override fun getWidth(): Int {
    return width
  }

  override fun getHeight(): Int {
    return height
  }

  override fun createGraphics(): Graphics2D {
    return graphics.create() as Graphics2D
  }

  override fun validate(gc: GraphicsConfiguration): Int {
    if (valid) {
      return IMAGE_OK
    }
    else {
      valid = true

      return IMAGE_RESTORED
    }
  }

  override fun contentsLost(): Boolean {
    return !valid
  }

  override fun getCapabilities(): ImageCapabilities? {
    return caps
  }

  override fun getWidth(observer: ImageObserver?): Int {
    return width
  }

  override fun getHeight(observer: ImageObserver?): Int {
    return height
  }

  override fun getProperty(name: String, observer: ImageObserver?): Any? {
    return null
  }

  companion object {

    private var NEXT_ID: Long = 0

    private val NULL_SURFACE_MANAGER = object : SurfaceManager() {
      override fun getPrimarySurfaceData(): SurfaceData = NullSurfaceData.theInstance

      override fun restoreContents(): SurfaceData = NullSurfaceData.theInstance
    }

    private var weakImages = mutableSetOf<WeakReference<PVolatileImage>>()

    val images: List<PVolatileImage>
      get() = synchronized(weakImages) {
        val result = mutableListOf<PVolatileImage>()

        weakImages.removeAll {
          val image = it.get()

          if (image != null) {
            result.add(image)
          }

          image == null
        }

        result
      }
  }

  class Descriptor(
    val pVolatileImageId: Long,
    val width: Int,
    val height: Int,
  )
}
