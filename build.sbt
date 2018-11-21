import Dependencies._

showCurrentGitBranch

git.useGitDescribe := true

lazy val commonSettings = Seq(
  organization := "tdm",
  organizationName := "Text and Data Mining (TDM) initiative involving HathiTrust/HTRC, JSTOR, and Portico",
  scalaVersion := "2.12.7",
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-language:postfixOps",
    "-language:implicitConversions"
  ),
  resolvers ++= Seq(
    "I3 Repository" at "http://nexus.htrc.illinois.edu/content/groups/public",
    Resolver.mavenLocal
  ),
  packageOptions in (Compile, packageBin) += Package.ManifestAttributes(
    ("Git-Sha", git.gitHeadCommit.value.getOrElse("N/A")),
    ("Git-Branch", git.gitCurrentBranch.value),
    ("Git-Version", git.gitDescribedVersion.value.getOrElse("N/A")),
    ("Git-Dirty", git.gitUncommittedChanges.value.toString),
    ("Build-Date", new java.util.Date().toString)
  )
)

lazy val ammoniteSettings = Seq(
  libraryDependencies += {
    val version = scalaBinaryVersion.value match {
      case "2.10" => "1.0.3"
      case _ ⇒ "1.4.4"
    }
    "com.lihaoyi" % "ammonite" % version % "test" cross CrossVersion.full
  },
  sourceGenerators in Test += Def.task {
    val file = (sourceManaged in Test).value / "amm.scala"
    IO.write(file, """object amm extends App { ammonite.Main.main(args) }""")
    Seq(file)
  }.taskValue,
  (fullClasspath in Test) ++= {
    (updateClassifiers in Test).value
      .configurations
      .find(_.configuration == Test.name)
      .get
      .modules
      .flatMap(_.artifacts)
      .collect { case (a, f) if a.classifier.contains("sources") => f }
  }
)

lazy val `tdm-token-count-tag-cloud` = (project in file(".")).
  enablePlugins(SbtTwirl, GitVersioning, GitBranchPrompt, JavaAppPackaging).
  settings(commonSettings).
  settings(ammoniteSettings).
  //settings(spark("2.4.0")).
  settings(spark_dev("2.4.0")).
  settings(
    name := "tdm-token-count-tag-cloud",
    libraryDependencies ++= Seq(
      "tdm"                           %% "feature-extractor"    % "2.1" 
        excludeAll ExclusionRule(organization = "edu.stanford.nlp"),
      "org.hathitrust.htrc"           %% "scala-utils"          % "2.6",
      "org.hathitrust.htrc"           %% "spark-utils"          % "1.2.0",
      "com.nrinaudo"                  %% "kantan.csv"           % "0.5.0",
      "com.typesafe.play"             %% "play-json"            % "2.6.10"
        exclude("com.fasterxml.jackson.core", "jackson-databind")
        exclude("ch.qos.logback", "logback-classic"),
      "com.typesafe"                  %  "config"               % "1.3.3",
      "org.rogach"                    %% "scallop"              % "3.1.5",
      "com.gilt"                      %% "gfc-time"             % "0.0.7",
      "ch.qos.logback"                %  "logback-classic"      % "1.2.3",
      "org.codehaus.janino"           %  "janino"               % "3.0.11",
      "org.scalacheck"                %% "scalacheck"           % "1.14.0"      % Test,
      "org.scalatest"                 %% "scalatest"            % "3.0.5"       % Test
    ),
    dependencyOverrides += "com.google.guava" % "guava" % "16.0.1"
  )
