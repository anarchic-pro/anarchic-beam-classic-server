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

package org.jetbrains.projector.awt.peer

import java.awt.Dimension
import java.awt.TextField
import java.awt.im.InputMethodRequests
import java.awt.peer.TextFieldPeer

class PTextFieldPeer(target: TextField) : PTextComponentPeer(target), TextFieldPeer {

  override fun getInputMethodRequests(): InputMethodRequests? {
    return null
  }

  override fun setEchoChar(echoChar: Char) {}

  override fun getPreferredSize(columns: Int): Dimension? {
    return null
  }

  override fun getMinimumSize(columns: Int): Dimension? {
    return null
  }
}
