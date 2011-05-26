package tuner.gui.util

import tuner.geom.Rectangle
import tuner.Config

object FacetLayout {
  
  def plotBounds(bounds:Rectangle, fields:List[String]) 
        : (Float, Map[(String,String),Rectangle]) = {
    
    val constrainingSize = math.min(bounds.width, bounds.height)
    val spacingSize = Config.plotSpacing * (fields.length - 1)
    val facetSize = (constrainingSize - spacingSize) / fields.length
    val plotBounds = Rectangle((bounds.minX, bounds.minY),
                               constrainingSize, constrainingSize)

    var plotDims = Map[(String,String),Rectangle]()
    fields.foldLeft(plotBounds.minX) {case (xPos, xFld) =>
      fields.foldLeft(plotBounds.minY) {case (yPos, yFld) =>
        if(xFld < yFld) {
          val blBounds = 
            Rectangle((xPos, yPos), facetSize, facetSize)
          val trBounds = {
            val startY = plotBounds.minY+plotBounds.maxY-(yPos+facetSize)
            val endX = plotBounds.minX + plotBounds.maxX-xPos
            val startX = endX - facetSize
            val endY = startY + facetSize
            Rectangle((startX, startY), (endX, endY))
          }
          plotDims += ((xFld, yFld) -> blBounds)
          plotDims += ((yFld, xFld) -> trBounds)
        }
        yPos + facetSize + Config.plotSpacing
      }
      xPos + facetSize + Config.plotSpacing
    }
    (facetSize, plotDims)
  }
}

