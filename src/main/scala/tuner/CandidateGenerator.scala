package tuner

class CandidateGenerator(project:Project) {
  
  val candidates:NamedPointManager = new NamedPointManager("Candidate")
  var currentFilter:List[(String,Float)] = Nil

  def update(newValues:List[(String,Float)]) = {
    currentFilter = newValues
    candidates.clear

    var data = project.designSites.get
    for(r <- 0 until data.numRows) {
      val tpl = data.tuple(r)
      if(newValues.forall {case (fld, v) => v == tpl(fld)}) {
        candidates.add(tpl.toList)
      }
    }
  }
}

