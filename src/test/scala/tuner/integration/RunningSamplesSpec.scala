package tuner.test.integration

import scala.swing.Reactor
import scala.swing.event.Event

import tuner.project._
import tuner.Progress
import tuner.ProgressComplete
import tuner.Region
import tuner.Table
import tuner.ViewInfo

import scala.util.Try

import tuner.test.Util._

class ProxyReactor(project:InProgress) extends Reactor {
  val events = new scala.collection.mutable.ArrayBuffer[Event]

  reactions += {
    case x => events += x
  }

  listenTo(project)
}

class RunningSamplesSpec extends IntegrationTest {

  val normalConfig = createConfig(resource("/sims/run_sim_noisy.sh", true))

  "A RunningSamples project" should {
    "running a normal project" should {
      val designSites = new Table
      val normalProj = new RunningSamples(normalConfig, resource("/sims"),
                                          testSamples, designSites)
      val reactor = new ProxyReactor(normalProj)
      val logger = new java.io.ByteArrayOutputStream
      normalProj.logStream = Some(logger)
      normalProj.start

      "send a Progress event with no progress when started" in {
        reactor.events should not be empty
        val e1 = reactor.events.head.asInstanceOf[Progress]
        e1.currentTime must_== 0
        e1.totalTime must_== testSamples.numRows
        e1.ok must beTrue
      }
      "send a ProgressComplete event when finished" in {
        // One start, one finish, and one sampling finished message
        reactor.events.size must be_>=(3)
        // Make sure it's the right type of object
        val x = ProgressComplete
        ProgressComplete == ProgressComplete
        reactor.events.last must be_==(ProgressComplete)
      }
      "write all stdout and stderr output to a log" in {
        logger.size must be_>(0)
      }
      "send a Progress event with 100% progress when finished" in {
        val lastE = reactor.events(reactor.events.length-2).asInstanceOf[Progress]
        lastE.currentTime must_== lastE.totalTime
        lastE.ok must beTrue
      }
      "have all the new design sites written to the given table" in {
        designSites.numRows must_== testSamples.numRows
      }
    }
  }

  def testSamples : Table = {
    val tblData = List(
      List(("x1", 2.0f), ("x2", 0.2f), ("x3", 1.2f)),
      List(("x1", 2.1f), ("x2", 0.4f), ("x3", 0.3f)),
      List(("x1", 0.1f), ("x2", 0.9f), ("x3", 0.5f))
    )
    val tbl = new Table
    tblData.foreach {row => tbl.addRow(row)}
    tbl
  }
  
  def createConfig(script:String) : ProjConfig = ProjConfig(
    name="test",
    scriptPath=script,
    inputs=List("x1","x2","x3") map {InputSpecification(_, 0f, 1f)},
    outputs=List(),
    ignoreFields=List(),
    gpModels=List(),
    buildInBackground=false,
    currentVis=ViewInfo.Default,
    currentRegion=Region.Default,
    history=None
  )
}

