name := "videosystem_for_distributed_video_processing"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "net.vz.mongodb.jackson" %% "play-mongo-jackson-mapper" % "1.1.0"
)     

play.Project.playJavaSettings
