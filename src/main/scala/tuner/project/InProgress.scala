package tuner.project

trait InProgress {
  
  var buildInBackground:Boolean

  protected def totalTime : Int
  protected def currentTime : Int

  def runStatus:(Int,Int) = (currentTime, totalTime)
  def finished:Boolean

  def start:Unit
  def stop:Unit
}

