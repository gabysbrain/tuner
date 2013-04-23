package tuner.gui

import com.jogamp.common.nio.Buffers
import com.jogamp.opengl.util.awt.TextRenderer
import javax.media.opengl.{GL,GL2,DebugGL2,GL2GL3,GL2ES1}
import java.awt.Graphics2D

import scala.swing.GL2Panel
import scala.swing.event.{MouseClicked, MouseDragged, MouseMoved}

import tuner.BoxRegion
import tuner.Color
import tuner.Config
import tuner.EllipseRegion
import tuner.SpecifiedColorMap
import tuner.Table
import tuner.ViewInfo
import tuner.geom.Rectangle
import tuner.gui.opengl.ValueShader
import tuner.gui.opengl.Glsl
import tuner.gui.opengl.Prosection
import tuner.gui.util.Matrix4
import tuner.gui.util.FacetLayout
import tuner.gui.widgets.Axis
import tuner.gui.widgets.Colorbar
import tuner.gui.widgets.Widgets
import tuner.project.Viewable
import tuner.util.AxisTicks
import tuner.util.ColorLib

class JoglMainPlotPanel(val project:Viewable) extends GL2Panel 
                                              with MainPlotPanel {

  preferredSize = new java.awt.Dimension(800, 600)

  val backgroundColor = Color(Config.backgroundColor)

  // This makes sure everything is in the (0,1) coordinate system
  val projectionMatrix = Matrix4.translate(-1, -1, 0) * Matrix4.scale(2, 2, 1)

  // These need to wait for the GL context to be set up
  // We use a separate shader program for each response
  var valueShaders:Map[String,ValueShader] = Map() // just for estimate
  var prosectionShaders:Map[String,Prosection] = Map()
  var colormapShader:Option[Glsl] = None

  // The buffers we're using
  var textureFbo:Option[Int] = None
  var fboTexture:Option[Int] = None

  // This is used for font rendering
  var textRenderer:TextRenderer = null

  // All the plot transforms
  var plotTransforms = Map[(String,String),(Matrix4,Matrix4)]()

  // Save the highlighted plot
  var mousedPlot:Option[(String,String)] = None

  // Everything response 1 needs 
  val resp1Colorbar:Colorbar = new Colorbar(Colorbar.Left)
  val resp1XAxes = createAxes(Axis.HorizontalBottom)
  val resp1YAxes = createAxes(Axis.VerticalLeft)

  // Everything response 2 needs
  val resp2Colorbar:Colorbar = new Colorbar(Colorbar.Right)
  val resp2XAxes = createAxes(Axis.HorizontalTop)
  val resp2YAxes = createAxes(Axis.VerticalRight)

  // These will get reset later
  var screenWidth = 0
  var screenHeight = 0

  listenTo(mouse.clicks, mouse.moves)

  reactions += {
    case MouseClicked(_, pt, _, _, _) => 
      handleBarMouse(pt.x, pt.y)
      handlePlotMouse(pt.x, pt.y)
    case MouseDragged(_, pt, _) =>
      handleBarMouse(pt.x, pt.y)
      handlePlotMouse(pt.x, pt.y)
    case MouseMoved(_, pt, _) => 
      val pb = sliceBounds.find {case (_,b) => b.isInside(pt.x, pt.y)}
      val newPos = pb.map {tmp => tmp._1}
      if(newPos != mousedPlot) {
        mousedPlot = newPos
        redraw
      }
  }

  override def init(gl2:GL2) = {
    // Create the shader programs
    if(valueShaders.isEmpty) {
      valueShaders = project.responseFields.map {resFld =>
        val model = project.gpModels(resFld)
        val estShader = ValueShader.fromResource(
            gl2, project.inputFields.size, 
            "/shaders/est.plot.frag.glsl",
            model.mean, model.sig2,
            model.thetas.toArray,
            model.design, model.corrResponses)
        //println(estShader.attribIds)
        (resFld -> estShader)
      } toMap
    }
    if(prosectionShaders.isEmpty) {
      prosectionShaders = project.responseFields.map {resFld =>
        val model = project.gpModels(resFld)
        val ptShader = Prosection.fromResource(
            gl2, project.inputFields.size, model.design, model.responses)
        //println(ptShader.attribIds)
        (resFld -> ptShader)
      } toMap
    }
    if(!colormapShader.isDefined) {
      colormapShader = Some(Glsl.fromResource(
        gl2, "/shaders/cmap.vert.glsl", 
             "/shaders/cmap.frag.glsl"))
      //println(colormapShader.get.attribIds)
    }

    textRenderer = new TextRenderer(
      new java.awt.Font("SansSerif", java.awt.Font.PLAIN, Config.smallFontSize))
  }

  override def dispose(gl2:GL2) = {
    // Make sure there are no remaining errors
    var errCode = gl2.glGetError
    while(errCode != GL.GL_NONE) {
      println("err: " + errCode)
      errCode = gl2.glGetError
    }

    // No more shaders
    gl2.getGL2ES2.glUseProgram(0)

    // No more texture
    gl2.glBindTexture(GL.GL_TEXTURE_2D, 0)
  }

  override def reshape(gl2:GL2, x:Int, y:Int, width:Int, height:Int) = {
    screenWidth = width
    screenHeight = height

    // Update all the bounding boxes
    updateBounds(width, height)
    val (ss, sb) = FacetLayout.plotBounds(plotBounds, project.inputFields)
    sliceSize = ss
    sliceBounds = sb

    plotTransforms = computePlotTransforms(sliceBounds, width, height)
    // All plots are the same size
    val plotRect = sliceBounds.head._2
    setupTextureTarget(gl2, plotRect.width.toInt, plotRect.height.toInt)
  }

  def redraw = peer.display

  def display(gl2:GL2) = {
    // The clear color gets reset by the plot drawings
    gl2.glClearColor(backgroundColor.r, 
                     backgroundColor.g, 
                     backgroundColor.b, 
                     1f)
    gl2.glClear(GL.GL_COLOR_BUFFER_BIT)

    // The 2D graphics we need
    val t1 = System.currentTimeMillis
    val j2d = overlay.createGraphics
    //println("t1: " + (System.currentTimeMillis-t1))
    j2d.setBackground(new java.awt.Color(0, 0, 0, 0))
    val t2 = System.currentTimeMillis
    j2d.clearRect(0, 0, 1000, 1000)
    //println("t2: " + (System.currentTimeMillis-t2))

    // See if we should highlight the 2 plots
    mousedPlot.foreach {case (fld1, fld2) => drawPlotHighlight(gl2, fld1, fld2)}

    // Draw the colorbars
    //val t4 = System.currentTimeMillis
    project.viewInfo.response1View.foreach {r =>
      resp1Colorbar.draw(j2d, leftColorbarBounds.minX, 
                              leftColorbarBounds.minY,
                              leftColorbarBounds.width, 
                              leftColorbarBounds.height,
                              r, colormap(r, resp1Colormaps))
    }
    project.viewInfo.response2View.foreach {r =>
      resp2Colorbar.draw(j2d, rightColorbarBounds.minX, 
                              rightColorbarBounds.minY,
                              rightColorbarBounds.width, 
                              rightColorbarBounds.height,
                              r, colormap(r, resp2Colormaps))
    }
    //println("colorbar draw: " + (System.currentTimeMillis-t4))

    // Draw the axes
    //val t5 = System.currentTimeMillis
    project.inputFields.foreach {fld =>
      val rng = (fld, project.viewInfo.currentZoom.range(fld))
      drawAxes(gl2, rng)
    }
    //println("axis draw: " + (System.currentTimeMillis - t5))

    // Draw the responses
    drawResponses(gl2, j2d)

    overlay.markDirty(0, 0, 1000, 1000)
    overlay.drawAll

    // Also get rid of the Java2D graphics
    j2d.dispose
  }

  /**
   * Create the texture and framebuffer objects
   */
  def setupTextureTarget(gl:GL2, texWidth:Int, texHeight:Int) = {
    // First setup the overall framebuffer
    if(!textureFbo.isDefined) {
      val fbo = Array(0)
      gl.glGenFramebuffers(1, fbo, 0)
      textureFbo = Some(fbo(0))
    }

    // Create a texture in which to render
    if(!fboTexture.isDefined) {
      val tex = Array(0)
      gl.glGenTextures(1, tex, 0)
      fboTexture = Some(tex(0))

      gl.glBindTexture(GL.GL_TEXTURE_2D, fboTexture.get)
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE)
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE)
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST)
      gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST)
      val fakeBuffer = Buffers.newDirectFloatBuffer(Array.fill(4*texWidth*texHeight)(0f))
      fakeBuffer.rewind
      gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA32F, 
                      texWidth, texHeight, 0, 
                      GL.GL_BGRA, GL.GL_FLOAT, fakeBuffer)
      //gl.glGenerateMipmap(GL.GL_TEXTURE_2D)
    }
  }

  /**
   * The plots themselves will be drawn in data space so these 
   * move everything into the proper coordinate system
   */
  def computePlotTransforms(
        sb:Map[(String,String),Rectangle], 
        width:Float, height:Float) = sb.map {case ((xFld,yFld),bounds) =>
    val (minX,maxX) = project.viewInfo.currentZoom.range(xFld)
    val (minY,maxY) = project.viewInfo.currentZoom.range(yFld)

    // transforms to move from data space to 0,1 space
    // response 2 plots are flipped 90 degrees
    val (dataTrans, dataScale) = if(xFld < yFld) {
      (Matrix4.translate(-minX, -minY, 0),
       Matrix4.scale(1/(maxX-minX), 1/(maxY-minY), 1))
    } else {
      (Matrix4.translate(-minY, -minX, 0),
       Matrix4.scale(1/(maxY-minY), 1/(maxX-minX), 1))
    }

    // Put the bounds in 0,1 terms
    // bounds are defined upside down since that's how processing likes it
    val tmp = Rectangle((bounds.minX, height-bounds.minY),
                        (bounds.maxX, height-bounds.maxY))
    val pctBounds = tmp.flipVertical / (width, height)

    // moves the plots into place
    val plotTrans = Matrix4.translate(pctBounds.minX, pctBounds.minY, 0)
    val plotScale = Matrix4.scale(pctBounds.width, pctBounds.height, 1)

    // The final transformations
    val ttlProj = projectionMatrix * dataTrans * dataScale
    val ttlPlot = projectionMatrix * plotTrans * plotScale
    (xFld,yFld) -> (ttlProj, ttlPlot)
  }

  private def drawPlotHighlight(gl2:GL2, field1:String, field2:String) = {
    val bounds1 = sliceBounds((field1, field2))
    val bounds2 = sliceBounds((field2, field1))

    gl2.glColor3f(1f, 1f, 1f)
    List(bounds1, bounds2).foreach {bounds =>
      val xx1 = P5Panel.map(bounds.minX-1, 0, screenWidth, -1, 1)
      val yy1 = P5Panel.map(bounds.minY-1, screenHeight, 0, -1, 1)
      val xx2 = P5Panel.map(bounds.maxX+1, 0, screenWidth, -1, 1)
      val yy2 = P5Panel.map(bounds.maxY+1, screenHeight, 0, -1, 1)
      gl2.glBegin(GL.GL_LINE_LOOP)
        gl2.glVertex2f(xx1, yy1)
        gl2.glVertex2f(xx2, yy1)
        gl2.glVertex2f(xx2, yy2)
        gl2.glVertex2f(xx1, yy2)
      gl2.glEnd
    }
  }

  private def drawAxes(gl2:GL2, range:(String,(Float,Float))) = {
    val (fld, (low, high)) = range
    val firstField = project.inputFields.head
    val lastField = project.inputFields.last

    //val t8 = System.currentTimeMillis
    project.viewInfo.response1View.foreach {r1 =>
      // See if we draw the x-axis
      if(fld != lastField) {
        val sliceDim = sliceBounds((fld, lastField))
        val axis = resp1XAxes(fld)
        //val t10 = System.currentTimeMillis
        val ticks = AxisTicks.ticks(low, high, 
                                    sliceDim.height, 
                                    Config.smallFontSize)
        //println("tick compute: " + (System.currentTimeMillis-t10))
        //val t11 = System.currentTimeMillis
        axis.draw(gl2, textRenderer, 
                       sliceDim.minX, bottomAxisBounds.minY,
                       sliceDim.width, bottomAxisBounds.height,
                       screenWidth, screenHeight,
                       fld, ticks)
        //println("axis draw: " + (System.currentTimeMillis-t11))
      }
      // See if we draw the y-axis
      if(fld != firstField) {
        val sliceDim = sliceBounds((firstField, fld))
        val axis = resp1YAxes(fld)
        val ticks = AxisTicks.ticks(low, high, 
                                    sliceDim.height, 
                                    Config.smallFontSize)
        axis.draw(gl2, textRenderer, 
                       leftAxisBounds.minX, sliceDim.minY,
                       leftAxisBounds.width, sliceDim.height,
                       screenWidth, screenHeight,
                       fld, ticks)
      }
    }
    //println("r1 axis draw: " + (System.currentTimeMillis-t8))

    //val t9 = System.currentTimeMillis
    project.viewInfo.response2View.foreach {r2 =>
      // See if we draw the x-axis
      if(fld != lastField) {
        val sliceDim = sliceBounds((lastField, fld))
        val axis = resp2XAxes(fld)
        val ticks = AxisTicks.ticks(low, high, 
                                    sliceDim.height, 
                                    Config.smallFontSize)
        axis.draw(gl2, textRenderer, sliceDim.minX, topAxisBounds.minY,
                       sliceDim.width, topAxisBounds.height,
                       screenWidth, screenHeight,
                       fld, ticks)
      }
      // See if we draw the y-axis
      if(fld != firstField) {
        val sliceDim = sliceBounds((fld, firstField))
        val axis = resp2YAxes(fld)
        val ticks = AxisTicks.ticks(low, high, 
                                    sliceDim.height, 
                                    Config.smallFontSize)
        axis.draw(gl2, textRenderer, rightAxisBounds.minX, sliceDim.minY,
                       rightAxisBounds.width, sliceDim.height,
                       screenWidth, screenHeight,
                       fld, ticks)
      }
    }
    //println("r2 axis draw: " + (System.currentTimeMillis-t9))
  }

  /**
   * Does opengl setup and takedown 
   */
  protected def drawResponses(gl2:GL2, j2d:Graphics2D) = {

    // Find the closest sample
    val closestSample = project.closestSample(
      project.viewInfo.currentSlice.toList).toMap

    // Loop through all field combinations to see what we need to draw
    project.inputFields.foreach {xFld =>
      project.inputFields.foreach {yFld =>
        val xRange = (xFld, project.viewInfo.currentZoom.range(xFld))
        val yRange = (yFld, project.viewInfo.currentZoom.range(yFld))

        if(xFld < yFld) {
          project.viewInfo.response1View.foreach {r1 => 
            drawResponse(gl2, xRange, yRange, r1)
            drawResponseWidgets(gl2, j2d, xRange, yRange, closestSample)
          }
        } else if(xFld > yFld) {
          project.viewInfo.response2View.foreach {r2 =>
            drawResponse(gl2, xRange, yRange, r2)
            drawResponseWidgets(gl2, j2d, xRange, yRange, closestSample)
          }
        }
      }
    }
  }

  /**
   * Draw a single continuous plot
   */
  protected def drawResponse(gl2:GL2, 
                             xRange:(String,(Float,Float)),
                             yRange:(String,(Float,Float)),
                             response:String) = {

    // only use the opengl renderer if we're looking at the values
    if(project.viewInfo.currentMetric == ViewInfo.ValueMetric) {

      val (texTrans, plotTrans) = plotTransforms((xRange._1, yRange._1))

      // First draw to the texture
      project.viewInfo.currentVis match {
        case ViewInfo.Hyperslice =>
          drawEstimationToTexture(gl2, xRange, yRange, response, texTrans)
        case ViewInfo.Splom =>
          drawProsectionToTexture(gl2, xRange, yRange, response, texTrans)
      }

      // Now put the texture on a quad
      val (xFld, yFld) = (xRange._1, yRange._1)
      val cm = if(xFld < yFld) resp1Colormaps else resp2Colormaps
      drawResponseTexturedQuad(gl2, colormap(response, cm), plotTrans)
    } else {
      //super.drawResponse(xRange, yRange, response)
    }
  }

  /**
   * This draws the prosection matrix
   */
  def drawProsectionToTexture(gl:GL2, xRange:(String,(Float,Float)),
                                      yRange:(String,(Float,Float)),
                                      response:String,
                                      trans:Matrix4) = {

    val shader = prosectionShaders(response)
    val model = project.gpModels(response)
    val fields = model.dims
    val plotRect = sliceBounds((xRange._1, yRange._1))
    val (xi,yi) = if(xRange._1 < yRange._1) {
      (fields.indexOf(xRange._1), fields.indexOf(yRange._1))
    } else {
      (fields.indexOf(yRange._1), fields.indexOf(xRange._1))
    }
    val slice = fields.map(project.viewInfo.currentSlice(_)).toArray
    val minVals = fields.map(project.viewInfo.currentZoom.min(_)).toArray
    val maxVals = fields.map(project.viewInfo.currentZoom.max(_)).toArray
    shader.draw(gl, fboTexture.get, 
                    plotRect.width.toInt, plotRect.height.toInt,
                    trans,
                    xRange, yRange, 
                    xi, yi, 
                    minVals, maxVals)
  }

  /**
   * This puts the estimated value in a texture
   */
  def drawEstimationToTexture(gl:GL2, xRange:(String,(Float,Float)),
                                      yRange:(String,(Float,Float)),
                                      response:String,
                                      trans:Matrix4) = {

    val shader = valueShaders(response)
    val model = project.gpModels(response)
    val fields = model.dims
    val plotRect = sliceBounds((xRange._1, yRange._1))
    val corrResponses = model.corrResponses
    val (xi,yi) = if(xRange._1 < yRange._1) {
      (fields.indexOf(xRange._1), fields.indexOf(yRange._1))
    } else {
      (fields.indexOf(yRange._1), fields.indexOf(xRange._1))
    }
    val slice = fields.map(project.viewInfo.currentSlice(_)).toArray

    shader.draw(gl, fboTexture.get, 
                    plotRect.width.toInt, plotRect.height.toInt,
                    trans,
                    xRange, yRange, 
                    xi, yi, 
                    slice)
  }

  /**
   * Processes the texture through the filtered colormap 
   * and draws everything on screen
   */
  def drawResponseTexturedQuad(gl:GL2, 
                               colormap:SpecifiedColorMap, 
                               trans:Matrix4) = {
    val es2 = gl.getGL2ES2

    gl.glDisable(GL.GL_BLEND)
    gl.glEnable(GL.GL_TEXTURE_2D)

    // Activate the texture program
    gl.glUseProgram(colormapShader.get.programId)

    // Bind the texture uniform
    gl.glActiveTexture(GL.GL_TEXTURE0)
    es2.glUniform1i(colormapShader.get.uniformId("values"), 0)

    // Set the colormap properties
    gl.glUniform1f(colormapShader.get.uniformId("filterLevel"), 
                   colormap.filterVal)
    gl.glUniform1i(colormapShader.get.uniformId("invert"), 
                   if(colormap.isInverted) 1 else 0)
    gl.glUniform1f(colormapShader.get.uniformId("minVal"), colormap.minVal)
    gl.glUniform1f(colormapShader.get.uniformId("maxVal"), colormap.maxVal)
    gl.glUniform4f(colormapShader.get.uniformId("minColor"),
                   colormap.minColor.r, 
                   colormap.minColor.g, 
                   colormap.minColor.b, 
                   1f)
    gl.glUniform4f(colormapShader.get.uniformId("maxColor"),
                   colormap.maxColor.r, 
                   colormap.maxColor.g, 
                   colormap.maxColor.b, 
                   1f)
    gl.glUniform4f(colormapShader.get.uniformId("filterColor"),
                   colormap.filterColor.r, 
                   colormap.filterColor.g,
                   colormap.filterColor.b,
                   1f)

    // Enable the texture
    gl.glBindTexture(GL.GL_TEXTURE_2D, fboTexture.get)

    gl.glUniformMatrix4fv(colormapShader.get.uniformId("trans"), 
                          1, false, trans.toArray, 0)

    gl.glBegin(GL2.GL_QUADS)
    gl.glVertex3f(0f, 0f, 0f)
    gl.glVertex3f(1f, 0f, 0f)
    gl.glVertex3f(1f, 1f, 0f)
    gl.glVertex3f(0f, 1f, 0f)
    gl.glEnd

    //gl.glPopMatrix
    gl.glBindTexture(GL.GL_TEXTURE_2D, 0)
    gl.glDisable(GL.GL_TEXTURE_2D)
    gl.glUseProgram(0)
  }

  protected def drawResponseWidgets(gl2:GL2,
                                    j2d:Graphics2D,
                                    xRange:(String,(Float,Float)),
                                    yRange:(String,(Float,Float)),
                                    closestSample:Table.Tuple) = {
    val (xFld, yFld) = (xRange._1, yRange._1)
    val bounds = sliceBounds((xFld, yFld))
    val (xf, yf, xr, yr) = if(xFld < yFld) {
      (xFld, yFld, xRange, yRange)
    } else {
      (yFld, xFld, yRange, xRange)
    }

    val (xSlice, ySlice) = (project.viewInfo.currentSlice(xf), 
                            project.viewInfo.currentSlice(yf))

    // Crosshair showing current location
    val t21 = System.currentTimeMillis
    Widgets.crosshair(gl2, bounds.minX, bounds.minY, 
                           bounds.width, bounds.height,
                           800, 600,
                           xSlice, ySlice, xr._2, yr._2)
    //println("crosshair draw: " + (System.currentTimeMillis-t21))

    // Line to the nearest sample
    if(project.viewInfo.showSampleLine) {
      Widgets.sampleLine(j2d, bounds.minX, bounds.minY,
                              bounds.width, bounds.height,
                              xSlice, ySlice, 
                              closestSample(xf), closestSample(yf),
                              xr._2, yr._2)
    }

    // The region mask
    if(project.viewInfo.showRegion)
      drawMask(j2d:Graphics2D, xFld, yFld)
  }

  private def drawMask(j2d:Graphics2D, xFld:String, yFld:String) = {

    // The bounding box of the slice on screen
    val slice = sliceBounds((xFld, yFld))

    val (xf, yf) = if(xFld < yFld) (xFld, yFld)
                   else            (yFld, xFld)
    val (xZoom, yZoom) = (project.viewInfo.currentZoom.range(xf),
                          project.viewInfo.currentZoom.range(yf))

    val (xMinRng, xMaxRng) = project.region.range(xf)
    val (yMinRng, yMaxRng) = project.region.range(yf)

    // Convert the x and y ranges into screen space
    val xxMin = P5Panel.constrain(
      P5Panel.map(xMinRng, xZoom._1, xZoom._2, 0, slice.width),
      0, slice.width) toInt
    val xxMax = P5Panel.constrain(
      P5Panel.map(xMaxRng, xZoom._1, xZoom._2, 0, slice.width),
      0, slice.width) toInt
    val yyMin = P5Panel.constrain(
      P5Panel.map(yMinRng, yZoom._2, yZoom._1, 0, slice.height),
      0, slice.height) toInt
    val yyMax = P5Panel.constrain(
      P5Panel.map(yMaxRng, yZoom._2, yZoom._1, 0, slice.height),
      0, slice.height) toInt

    j2d.translate(slice.minX.toInt, slice.minY.toInt)
    project.region match {
      case _:BoxRegion =>
        j2d.setPaint(Config.regionColor)
        j2d.fillRect(xxMin, yyMax, xxMax-xxMin, yyMin-yyMax)
        j2d.setPaint(ColorLib.darker(Config.regionColor))
        j2d.drawRect(xxMin, yyMax, xxMax-xxMin, yyMin-yyMax)
        //println(xSlice + " " + ySlice + " " + xRad + " " + yRad)
      case _:EllipseRegion =>
        j2d.setPaint(Config.regionColor)
        j2d.fillOval(xxMin, yyMax, xxMax-xxMin, yyMin-yyMax)
        j2d.setPaint(ColorLib.darker(Config.regionColor))
        j2d.drawOval(xxMin, yyMax, xxMax-xxMin, yyMin-yyMax)
    }
    j2d.translate(-slice.minX.toInt, -slice.minY.toInt)

  }

  /**
   * What to do when the mouse is clicked inside the bounds 
   * of the hyperslice matrix
   */
  def handlePlotMouse(mouseX:Int, mouseY:Int) = {

    // Do a rough check to see if we're near any of the slices
    if(plotBounds.isInside(mouseX, mouseY)) {
      sliceBounds.foreach {case ((xfld,yfld), sb) =>
        if(sb.isInside(mouseX, mouseY)) {
          // Make sure we're inside a bounds that's active
          if(xfld < yfld && project.viewInfo.response1View.isDefined) {
            val (lowZoomX, highZoomX) = project.viewInfo.currentZoom.range(xfld)
            val (lowZoomY, highZoomY) = project.viewInfo.currentZoom.range(yfld)
            val newX = P5Panel.map(mouseX, sb.minX, sb.maxX,
                                           lowZoomX, highZoomX)
            val newY = P5Panel.map(mouseY, sb.minY, sb.maxY,
                                           highZoomY, lowZoomY)
            publishSliceChanged(this, (xfld, newX), (yfld, newY))
          } else if(xfld > yfld && project.viewInfo.response2View.isDefined) {
            // x and y fields are reversed here!!!
            val (lowZoomX, highZoomX) = project.viewInfo.currentZoom.range(yfld)
            val (lowZoomY, highZoomY) = project.viewInfo.currentZoom.range(xfld)
            val newX = P5Panel.map(mouseX, sb.minX, sb.maxX,
                                           lowZoomX, highZoomX)
            val newY = P5Panel.map(mouseY, sb.minY, sb.maxY,
                                           highZoomY, lowZoomY)
            publishSliceChanged(this, (yfld, newX), (xfld, newY))
          }
        }
      }
    }
  }

  def handleBarMouse(mouseX:Int, mouseY:Int) = {
    if(leftColorbarBounds.isInside(mouseX, mouseY)) {
      project.viewInfo.response1View.foreach {r1 =>
        val cb = resp1Colorbar
        val cm = colormap(r1, resp1Colormaps)
        val filterVal = P5Panel.map(mouseY, cb.barBounds.maxY, 
                                            cb.barBounds.minY, 
                                            cm.minVal, 
                                            cm.maxVal)
        cm.filterVal = filterVal
      }
    }
    if(rightColorbarBounds.isInside(mouseX, mouseY)) {
      project.viewInfo.response2View.foreach {r2 =>
        val cb = resp2Colorbar
        val cm = colormap(r2, resp2Colormaps)
        val filterVal = P5Panel.map(mouseY, cb.barBounds.maxY, 
                                            cb.barBounds.minY, 
                                            cm.minVal, 
                                            cm.maxVal)
        cm.filterVal = filterVal
      }
    }

    redraw
  }

  private def createAxes(position:Axis.Placement) = {
    val fields = position match {
      case Axis.HorizontalTop | Axis.HorizontalBottom => 
        project.inputFields.init
      case Axis.VerticalLeft | Axis.VerticalRight => 
        project.inputFields.tail
    }
    fields.map {fld => (fld, new Axis(position))} toMap
  }
  
}


