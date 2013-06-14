import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "play21-origami-plugin"
  val appVersion      = "1.1.2"
  val orientDBVersion = "1.4.1-SNAPSHOT"

  val appDependencies = Seq(  
    "com.orientechnologies" % "orientdb-core" % {orientDBVersion},
 	"com.orientechnologies" % "orient-commons" % {orientDBVersion},
    "com.orientechnologies" % "orientdb-client" % {orientDBVersion},
    "com.orientechnologies" % "orientdb-nativeos" % {orientDBVersion},
    "com.orientechnologies" % "orientdb-server" % {orientDBVersion},
    "com.orientechnologies" % "orientdb-object" % {orientDBVersion},
    "com.orientechnologies" % "orientdb-enterprise" % {orientDBVersion},
    "com.orientechnologies" % "orientdb-distributed" % {orientDBVersion},
    "com.hazelcast" % "hazelcast" % "2.1.2",
    "org.javassist" % "javassist" % "3.17.1-GA",
	"commons-collections" % "commons-collections" % "3.2.1",
    javaCore
  )
    
  val main = play.Project(appName, appVersion, appDependencies).settings(
    publishArtifact in(Compile, packageDoc) := false,
    organization := "com.wingnest.play2",
    resolvers += "Sonatype OSS Snapshot" at "https://oss.sonatype.org/content/repositories/snapshots",    
    resolvers += "Sonatype" at "https://oss.sonatype.org/content/repositories/public/"      
  )

}
