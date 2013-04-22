package scala.swing

import javax.media.opengl.GL2
import javax.media.opengl.GLAutoDrawable
import javax.media.opengl.GLEventListener
import javax.media.opengl.awt.GLJPanel
import com.jogamp.opengl.util.awt.Overlay

abstract class GL2Panel extends Component with Publisher {

  override lazy val peer:GLJPanel = new GLJPanel with SuperMixin
  lazy val overlay = new Overlay(peer)

  peer.addGLEventListener(new GLEventListener {
    def reshape(drawable:GLAutoDrawable, x:Int, y:Int, width:Int, height:Int) =
      GL2Panel.this.reshape(drawable.getGL.getGL2, x, y, width, height)
    def init(drawable:GLAutoDrawable) = 
      GL2Panel.this.init(drawable.getGL.getGL2)
    def dispose(drawable:GLAutoDrawable) = 
      GL2Panel.this.dispose(drawable.getGL.getGL2)
    def display(drawable:GLAutoDrawable) = 
      GL2Panel.this.display(drawable.getGL.getGL2)
  })

  def init(gl2:GL2) : Unit = {}
  def dispose(gl2:GL2) : Unit = {}

  def display(gl2:GL2) : Unit

  def reshape(gl2:GL2, x:Int, y:Int, width:Int, height:Int) : Unit = {}
}

