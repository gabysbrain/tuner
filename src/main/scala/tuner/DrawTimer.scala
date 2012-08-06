package tuner

import collection.mutable.MutableList

/**
 * Records statistics on how long things take to draw. 
 * Then we can predict how long drawing will take.
 *
 * For predicting the timing we need to record the following items:
 * * number of sample points to process
 * * the radii of the gaussian kernels
 * * the extents of the various dimensions (to normalize the radii)
 * * dimensionality of the data
 * * the time it took to draw
 */
object DrawTimer {

  implicit def int2Timing(i:Int) : Timing = Nanos(i)
  sealed trait Timing {
    def +(t2:Timing) : Timing = {
      // Just convert everything to nanos
      val x1 = this match {
        case Millis(m) => m * 1000000L
        case Nanos(n)  => n
      }
      val x2 = t2 match {
        case Millis(m) => m * 1000000L
        case Nanos(n)  => n
      }
      Nanos(x1 + x2)
    }
    def -(t2:Timing) : Timing = {
      // Just convert everything to nanos
      val x1 = this match {
        case Millis(m) => m * 1000000L
        case Nanos(n)  => n
      }
      val x2 = t2 match {
        case Millis(m) => m * 1000000L
        case Nanos(n)  => n
      }
      Nanos(x1 - x2)
    }

    def toSeconds:Float

    override def toString = toSeconds.toString
  }
  case class Millis(m:Long) extends Timing {
    def toSeconds = m / 1e3f
  }
  case class Nanos(n:Long) extends Timing {
    def toSeconds = n / 1e9f
  }

  // A tuple of type (lower dim bound, upper dim bound, radius)
  type TimingRadii = (Float,Float,Float)

  // The time to draw an individual hyperslice plot matrix
  val drawTimes = new MutableList[(Int,Seq[TimingRadii],Timing)]

  // The time to draw all the static information that doesn't depend
  // on what hyperslice view we're drawing
  val staticTimes = new MutableList[Timing]

  /**
   * A utility function to return the time to run a block of code
   */
  def timed(log:Boolean)(block: => Unit) : Timing = {
    val startTime = System.nanoTime
    block
    val endTime = System.nanoTime
    if(log) println("t1: " + startTime + " t2: " + endTime)
    val time = Nanos(endTime-startTime)
    if(log) println("logged time: " + time)
    time
  }

  def timed(block: => Unit) : Timing = timed(false)(block)

  /**
   * add a drawing timing for spherical kernels in (0,1) dimension spaces
   */
  def addSphericalTiming(totalPoints:Int, radius:Float, dims:Int, time:Timing) =
    drawTimes +=((totalPoints, List.fill(dims)((0, 1, radius)), time))

  /**
   * add a drawing timing for eliptical kernels
   */
  def addElipticalTiming(totalPoints:Int, 
                         radii:Seq[(Float,Float,Float)], 
                         time:Timing) =
    drawTimes +=((totalPoints, radii, time))

}

