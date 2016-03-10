val ext_libgdxVersion = "1.9.2"

val leavu3 = Project(id = "leavu3", base = file("."))
  .settings(
    organization := "se.gigurra",
    version := "SNAPSHOT",

    scalaVersion := "2.11.7",
    scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation"),

    libraryDependencies ++= Seq(
      "com.github.mpilquist"  %%  "simulacrum"            %   "0.7.0",
      "com.badlogicgames.gdx" %   "gdx"                   % ext_libgdxVersion,
      "com.badlogicgames.gdx" %   "gdx-freetype"          % ext_libgdxVersion,
      "com.badlogicgames.gdx" %   "gdx-backend-lwjgl"     % ext_libgdxVersion,
      "com.badlogicgames.gdx" %   "gdx-platform"          % ext_libgdxVersion classifier "natives-desktop",
      "com.badlogicgames.gdx" %   "gdx-freetype-platform" % ext_libgdxVersion classifier "natives-desktop",
      "org.scalatest"         %%  "scalatest"             %   "2.2.4"           %   "test",
      "org.mockito"           %   "mockito-core"          %   "1.10.19"         %   "test"
    )
    
  )
  .dependsOn(uri("git://github.com/GiGurra/service-utils.git#0.1.4"))

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)