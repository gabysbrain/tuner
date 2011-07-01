package tuner.project

trait Saved {
  val path:String

  val modificationDate = new java.util.Date

  def save() : Unit = this.asInstanceOf[Project].save(path)

  override def hashCode : Int = path.hashCode

  override def equals(other:Any) : Boolean = other match {
    case that:Saved => this.path == that.path
    case _            => false
  }
}

