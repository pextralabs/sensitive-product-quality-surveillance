resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.3")
addSbtPlugin("com.typesafe.sbt" % "sbt-play-ebean" % "4.0.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.5")
addSbtPlugin("com.typesafe.sbt" % "sbt-play-enhancer" % "1.1.0")
