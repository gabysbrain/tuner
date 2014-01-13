resolvers += Resolver.url("artifactory", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.1")

addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.4.0")

