package scala.swing

import org.japura.gui.CollapsibleRootPanel

class CollapsiblePanel extends Panel with SequentialContainer.Wrapper {
  override lazy val peer = new CollapsibleRootPanel with SuperMixin
}

