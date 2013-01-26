import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "play21-origami-simple-app"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "com.wingnest.play2" % "play2-origami-plugin_2.10" % "1.0-SNAPSHOT",
	"org.apache.httpcomponents" % "httpclient" % "4.2.2",
	"commons-lang" % "commons-lang" % "2.6",
    javaCore
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
  )

}
