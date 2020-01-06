//
// Copyright (C) Lightbend Inc. <https://www.lightbend.com>
//

lazy val root = (project in file(".")).enablePlugins(PlayScala).enablePlugins(MediatorWorkaroundPlugin)

name := "system-property"

scalaVersion := sys.props.get("scala.version").getOrElse("2.12.9")

// because the "test" directory clashes with the scripted test file
scalaSource in Test := (baseDirectory.value / "tests")

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-akka-http-server" % sys.props("project.version"),
  "com.typesafe.play" %% "play-netty-server"     % sys.props("project.version"),
  guice,
  ws,
  specs2 % Test
)

fork in Test := true

javaOptions in Test += "-Dplay.server.provider=play.core.server.NettyServerProvider"

PlayKeys.playInteractionMode := play.sbt.StaticPlayNonBlockingInteractionMode

InputKey[Unit]("verifyResourceContains") := {
  val args       = Def.spaceDelimited("<path> <status> <words> ...").parsed
  val path       = args.head
  val status     = args.tail.head.toInt
  val assertions = args.tail.tail
  DevModeBuild.verifyResourceContains(path, status, assertions, 0)
}

// This is copy/pasted from AkkaSnapshotRepositories since scripted tests also need
// the snapshot resolvers in `cron` builds.
// If this is a cron job in Travis:
// https://docs.travis-ci.com/user/cron-jobs/#detecting-builds-triggered-by-cron
resolvers in ThisBuild ++= (sys.env.get("TRAVIS_EVENT_TYPE").filter(_.equalsIgnoreCase("cron")) match {
  case Some(_) =>
    Seq(
      "akka-snapshot-repository".at("https://repo.akka.io/snapshots"),
      "akka-http-snapshot-repository".at("https://dl.bintray.com/akka/snapshots/")
    )
  case None => Seq.empty
})