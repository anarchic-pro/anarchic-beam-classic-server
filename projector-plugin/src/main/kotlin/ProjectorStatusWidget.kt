/*
 * Copyright (c) 2019-2021, JetBrains s.r.o. and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation. JetBrains designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact JetBrains, Na Hrebenech II 1718/10, Prague, 14000, Czech Republic
 * if you need additional information or have any questions.
 */

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidget.WidgetPresentation
import com.intellij.openapi.wm.impl.status.EditorBasedWidget
import com.intellij.util.Consumer
import java.awt.Component
import java.awt.event.MouseEvent
import javax.swing.Icon

class ProjectorStatusWidget(project: Project)
  : EditorBasedWidget(project),
    StatusBarWidget.MultipleTextValuesPresentation,
    StatusBarWidget.Multiframe {
  private val logger = Logger.getInstance(ProjectorStatusWidget::class.java.name)

  override fun ID(): String = ProjectorStatusWidget::class.java.name

  override fun copy(): StatusBarWidget = ProjectorStatusWidget(project)

  override fun getPopupStep(): ListPopup {
    val context = DataManager.getInstance().getDataContext(myStatusBar as Component)
    return JBPopupFactory.getInstance().createActionGroupPopup("TITLE Projector",
                                                               actions,
                                                               context,
                                                               false,
                                                               null,
                                                               5)
  }

  override fun getTooltipText(): String = updateTooltip()

  override fun getClickConsumer(): Consumer<MouseEvent>? = null

  override fun getSelectedValue(): String = updateText()

  override fun getPresentation(): WidgetPresentation = this

  override fun getIcon() = updateIcon()

  override fun dispose() {
    super.dispose()
  }

  fun update() {
    myStatusBar.updateWidget(ID())
  }

  private fun updateIcon(): Icon {
    return when {
      isActivationNeeded() -> RED_DOT
      isProjectorRunning() -> GREEN_DOT
      isProjectorAutoStarting() -> YELLOW_DOT
      isProjectorDisabled() -> BLACK_DOT
      else  -> BLACK_DOT
    }
  }

  private fun updateText(): String {
    return "Projector"
  }

  private fun updateTooltip(): String {
    return when {
      isActivationNeeded() -> "Activation is needed"
      isProjectorRunning() -> "Projector is running"
      isProjectorAutoStarting() -> "Projector is starting"
      isProjectorDisabled() -> "Projector is disabled"
      else  -> "Impossible state"
    }
  }

  companion object {
    private val actions = object : ActionGroup() {
      override fun getChildren(e: AnActionEvent?): Array<AnAction> = arrayOf(ActivateAction(),
                                                                             EnableAction(),
                                                                             DisableAction(),
                                                                             HeadlessProjectorAction(),
                                                                             SessionAction())
    }

    @JvmField
    val RED_DOT = IconLoader.getIcon("/META-INF/redSign.svg", ProjectorStatusWidget::class.java)

    @JvmField
    val GREEN_DOT = IconLoader.getIcon("/META-INF/greenSign.svg", ProjectorStatusWidget::class.java)

    @JvmField
    val YELLOW_DOT = IconLoader.getIcon("/META-INF/yellowSign.svg", ProjectorStatusWidget::class.java)

    @JvmField
    val BLACK_DOT = IconLoader.getIcon("/META-INF/blackSign.svg", ProjectorStatusWidget::class.java)
  }


}
