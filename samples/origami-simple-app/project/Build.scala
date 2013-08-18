import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "play21-origami-simple-app"
  val appVersion      = "1.1.5"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "com.wingnest.play2" % "play21-origami-plugin_2.10" % "1.1.5",
//	"org.apache.httpcomponents" % "httpclient" % "4.2.2",
	"commons-lang" % "commons-lang" % "2.6",
    javaCore
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here      
    resolvers += "Sonatype OSS Snapshot" at "https://oss.sonatype.org/content/repositories/snapshots",    
    resolvers += "Sonatype" at "https://oss.sonatype.org/content/repositories/public/"      
  )

}
