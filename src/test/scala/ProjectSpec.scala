package tuner.test

import org.scalatest.BeforeAndAfterEach
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers._

import java.io.File

import scala.actors.Actor

import tuner.Config
import tuner.DimRanges
import tuner.project._
import tuner.Region
import tuner.Sampler

class InProgressProjectSpec extends FunSuite with BeforeAndAfterEach {

  // temp directory where the project config stuff is stored
  var projectPath:String = _
  val newProjSavePath:String = getClass.getResource("/new_proj_path").getPath

  override def beforeEach = {
    val resourcePath = getClass.getResource("/in_progress_orig").getPath
    val resDir = new File(resourcePath)

    val testDir = {
      val tmptmp = File.createTempFile("in_progress", System.nanoTime.toString)
      tmptmp.delete
      val tmp = new File(tmptmp.getAbsolutePath + ".d")
      tmp.mkdir
      tmp
    }

    // Copy all the files in the directory
    for(fileName <- resDir.list) {
      val f1 = new File(resDir.getAbsolutePath + "/" + fileName)
      val f2 = new File(testDir.getAbsolutePath + "/" + fileName)
      val in = new java.io.FileInputStream(f1)
      val out = new java.io.FileOutputStream(f2)
      val buf = Array.fill(1024)(0.toByte)
      var len = in.read(buf)
      while(len > 0) {
        out.write(buf, 0, len)
        len = in.read(buf)
      }
      in.close
      out.close
      if(f1.getName.endsWith(".sh")) {
        f2.setExecutable(true)
      }
    }
    projectPath = testDir.getAbsolutePath
  }

  override def afterEach = {
    val curDir = new java.io.File(projectPath)
    curDir.delete
  }

  test("test in progress running of samples") {
    val tmpProj = Project.fromFile(projectPath)

    tmpProj.isInstanceOf[RunningSamples] should be (true)

    val proj = tmpProj.asInstanceOf[RunningSamples]
    val Some(sr) = proj.sampleRunner

    Thread.sleep(10)

    // Make sure the inititial status is ok
    proj should have ('runStatus ((0,100)))
    proj should not be ('finished)
    sr should have ('getState (Actor.State.Runnable))
    
    // Sleep for 5 seconds and then more samples should have finished
    Thread.sleep(5000)

    val (numDone, 100) = proj.runStatus
    numDone should not be (0)
    (numDone % Config.samplingRowsPerReq) should be (0)
  }

  test("test in progress progress query") {
    /*
    val proj = Project.fromFile(projectPath)
    val Some(sr) = proj.sampleRunner
    val random = new scala.util.Random

    Thread.sleep(10)

    // Make sure the inititial status is ok
    proj.status should be (Project.RunningSamples(0, 100))
    sr.getState should be (Actor.State.Runnable)
    
    // Sleep for 5 seconds and then more samples should have finished
    Thread.sleep(5000)

    val done = sr.completedSamples
    proj.status should be (Project.RunningSamples(done, 100))

    // Sleep some more
    Thread.sleep(5000 + random.nextInt(5000))

    val done2 = sr.completedSamples
    proj.status should be (Project.RunningSamples(done2, 100))
    */
    (pending)
  }


  test("new project status") {
    val numSamples = 5
    val projName = "testing_project"
    val savePath = newProjSavePath + "/" + projName
    val scriptPath = "/"
    val inputs = List(("d1", 0f, 1f), 
                      ("d2", 0f, 1f), 
                      ("d3", 0f, 1f), 
                      ("d4", 0f, 1f))
    val np = new NewProject(projName, savePath, scriptPath, inputs)
    np.newSamples(numSamples, Sampler.random)

    np.newSamples should have ('numRows (5))
    np.newSamples should have ('fieldNames (inputs.map(_._1)))

    np.save(savePath)

    val np2 = Project.fromFile(savePath)

    np2.isInstanceOf[RunningSamples] should be (true)

    val sp = np2.asInstanceOf[RunningSamples]
    sp.newSamples.numRows should be (numSamples)
    sp.runStatus should be ((0, 5))
    sp.finished should not be ('finished)
  }

  test("new project range should be full input range") {
    val dimRanges = List(("d1", 0f, 1f), 
                         ("d2", 0f, 1f), 
                         ("d3", 0f, 1f), 
                         ("d4", 0f, 1f))
    val numSamples = 5
    val projName = "testing_project"
    val savePath = newProjSavePath + "/" + projName
    val scriptPath = "/"
    val np = new NewProject(projName, savePath, scriptPath, dimRanges)

    // Make sure all the ranges equal each other
    dimRanges.foreach {case (fld, min, max) =>
      np.sampleRanges.range(fld) should be ((min, max))
    }
  }

  test("make sure projects in different stages are the right types") {
    val p1 = Project.fromFile(getClass.getResource("/build_gp_proj").getPath)
    p1.asInstanceOf[BuildingGp]

    val p2 = Project.fromFile(getClass.getResource("/in_progress_orig").getPath)
    p2.asInstanceOf[RunningSamples]

    val p3 = Project.fromFile(getClass.getResource("/has_images_proj").getPath)
    p3.asInstanceOf[Viewable]

    val p4 = Project.fromFile(getClass.getResource("/no_images_proj").getPath)
    p4.asInstanceOf[Viewable]
  }

  test("make sure a project without images loads up correctly") {
    val projPath = getClass.getResource("/no_images_proj").getPath
    val proj = Project.fromFile(projPath).asInstanceOf[Viewable]
    proj should have ('previewImages (None))
  }

  test("make sure a project with images loads up correctly") {
    val projPath = getClass.getResource("/has_images_proj").getPath
    val proj = Project.fromFile(projPath).asInstanceOf[Viewable]
    proj.previewImages should be ('defined)
  }

}

