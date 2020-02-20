lazy val commonSettings = Seq(
  version := "0.1-SNAPSHOT",
  organization := "edu.upenn",
  scalaVersion := "2.11.8",
  scalaVersion in ThisBuild := "2.11.8",
  //test in assembly := {},
  name := "neosemantics_loader",
  //logLevel in assembly := Level.Debug
)

val ScalatraVersion = "2.6.3"

lazy val app = (project in file("app")).
  settings(commonSettings: _*).
  settings(
    mainClass in assembly := Some("edu.upenn.turbo.Neo4jConnector"),
  )

lazy val utils = (project in file("utils")).
  settings(commonSettings: _*).
  settings(
    assemblyJarName in assembly := "neosemanticsLoader.jar",
  )

assemblyMergeStrategy in assembly := {
    case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
    case PathList("META-INF", "LICENSES.txt") => MergeStrategy.discard
    case PathList("META-INF", "LICENSE.txt") => MergeStrategy.discard
    case PathList("META-INF", "NOTICE.txt") => MergeStrategy.discard
    case PathList("META-INF", "README.txt") => MergeStrategy.discard
    case PathList("META-INF", "README.md") => MergeStrategy.discard
    case PathList("META-INF", "services", "org.apache.lucene.codecs.Codec") => MergeStrategy.concat
    case PathList("META-INF", "services", "org.apache.lucene.codecs.DocValuesFormat") => MergeStrategy.concat
    case PathList("META-INF", "services", "org.apache.lucene.codecs.PostingsFormat") => MergeStrategy.concat
    case PathList("META-INF", "services", "org.neo4j.commandline.admin.AdminCommand$Provider") => MergeStrategy.concat
    case PathList("META-INF", "services", "org.neo4j.configuration.LoadableConfig") => MergeStrategy.concat
    case PathList("META-INF", "services", "org.neo4j.kernel.extension.KernelExtensionFactory") => MergeStrategy.concat
    case PathList("META-INF", "services", "org.neo4j.diagnostics.DiagnosticsOfflineReportProvider") => MergeStrategy.concat
    case PathList("META-INF", "services", "org.neo4j.jmx.impl.ManagementBeanProvider") => MergeStrategy.concat
    case PathList("module-info.class") => MergeStrategy.concat
    case PathList("META-INF", xs @ _*) =>
      (xs map {_.toLowerCase}) match {
    case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") || ps.last.endsWith(".rsa") =>
          MergeStrategy.discard
    case x => MergeStrategy.deduplicate
    }
  case x => MergeStrategy.deduplicate
}

resolvers += Classpaths.typesafeReleases

libraryDependencies ++= Seq(
  "org.neo4j" % "neo4j" % "3.5.1"
)

