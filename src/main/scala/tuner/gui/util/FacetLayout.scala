package tuner.gui.util

import tuner.geom.Rectangle
import tuner.Config

object FacetLayout {
  
  def plotBounds(bounds:Rectangle, fields:List[String]) 
        : (Float, Map[(String,String),Rectangle]) = {
    
    val constrainingSize = math.min(bounds.width, bounds.height)
    val spacingSize = Config.plotSpacing * (fields.length - 1)
    val facetSize = (constrainingSize - spacingSize) / fields.length

    var plotDims = Map[(String,String),Rectangle]()
    fields.foldLeft(bounds.minX) {case (xPos, xFld) =>
      fields.foldLeft(bounds.minY) {case (yPos, yFld) =>
        if(xFld != yFld) {
          val bounds = Rectangle((xPos, yPos), facetSize, facetSize)
          plotDims += ((xFld, yFld) -> bounds)
        }
        yPos + facetSize + Config.plotSpacing
      }
      xPos + facetSize + Config.plotSpacing
    }
    (facetSize, plotDims)
  }
}

