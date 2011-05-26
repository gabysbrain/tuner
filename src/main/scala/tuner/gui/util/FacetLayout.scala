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
        if(xFld < yFld) {
          /*
          if(xFld == "minCorrelation" && yFld == "similThreshold")
            println("y " + yPos + " " + bounds.minY + " " + constrainingSize + " " + bounds.height)
          */
          val blBounds = 
            Rectangle((xPos, yPos), facetSize, facetSize)
          val trBounds = {
            val startX = bounds.minX+constrainingSize-xPos-Config.plotSpacing*2
            val endY = 2*bounds.minY+constrainingSize-yPos
            val startY = endY - facetSize
            val endX = startX + facetSize
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

