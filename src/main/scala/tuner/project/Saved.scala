package tuner.project

trait Saved {
  val path:String

  val modificationDate = new java.util.Date

  protected def save = this.asInstanceOf[Project].save(path)
}

