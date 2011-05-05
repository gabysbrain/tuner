package tuner

class Matrix2D(rids:List[Float], 
               cids:List[Float]) {
  
  val rowIds = rids
  val colIds = cids

  def rows : Int = rowIds.size
  def columns : Int = colIds.size

  // Cache these
  private var _min:Float = Float.MaxValue
  private var _max:Float = Float.MinValue

  def min : Float = _min
  def max : Float = _max

  def minX : Float = rowIds.min
  def maxX : Float = rowIds.max
  def minY : Float = colIds.min
  def maxY : Float = colIds.max

  // Storing stuff in column-major order
  protected val data:Array[Float] = new Array(colIds.length * rowIds.length)

  def set(row:Int, col:Int, value:Float) = {
    data(col * rows + row) = value
    _min = math.min(_min, value)
    _max = math.max(_max, value)
  }

  def get(row:Int, col:Int): Float = {
    data(col * rows + row)
  }

  def rowVal(row:Int) : Float = rowIds(row)
  def colVal(col:Int) : Float = colIds(col)
}

