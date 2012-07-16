package tuner.gui.event

import scala.swing.Component
import scala.swing.event.ComponentEvent

import tuner.Table

/**
 * Event that gets generated when new samples are imported
 */
case class NewDesignSelected(source:Component) extends ComponentEvent

/**
 * Event that gets generated when a new response value is selected
 */
case class NewResponseSelected(source:Component, response:String) 
    extends ComponentEvent

