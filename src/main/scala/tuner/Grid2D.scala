package tuner

class Grid2D(rids:List[Float], 
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
  //protected val data:Array[Float] = new Array(colIds.length * rowIds.length)
  protected val data:Array[Float] = 
    Array.fill(colIds.length * rowIds.length)(0f)

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

  def map(f:Float=>Float) = {
    val newData = data.map(f)
    new Grid2D(rowIds, colIds) {
      override val data = newData
      _min = newData.min
      _max = newData.max
    }
  }
}

