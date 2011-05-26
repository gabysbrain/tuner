package tuner

class CandidateGenerator(project:Project) {
  
  var candidates:HistoryManager = new HistoryManager
  var currentFilter:List[(String,Float)] = Nil

  def update(newValues:List[(String,Float)]) = {
    currentFilter = newValues
    candidates = new HistoryManager

    var data = project.designSites.get
    for(r <- 0 until data.numRows) {
      val tpl = data.tuple(r)
      if(newValues.forall {case (fld, v) => v == tpl(fld)}) {
        candidates.add(tpl.toList)
      }
    }
  }
}

