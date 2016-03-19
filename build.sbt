val ext_libgdxVersion = "1.9.2"

val leavu3 = Project(id = "leavu3", base = file("."))
  .settings(
    organization := "se.gigurra",
    version := "SNAPSHOT",

    scalaVersion := "2.11.8",
    scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation"),

    libraryDependencies ++= Seq(
      "com.badlogicgames.gdx" %   "gdx"                   % ext_libgdxVersion,
      "com.badlogicgames.gdx" %   "gdx-freetype"          % ext_libgdxVersion,
      "com.badlogicgames.gdx" %   "gdx-backend-lwjgl"     % ext_libgdxVersion,
      "com.badlogicgames.gdx" %   "gdx-platform"          % ext_libgdxVersion classifier "natives-desktop",
      "com.badlogicgames.gdx" %   "gdx-freetype-platform" % ext_libgdxVersion classifier "natives-desktop"
    )
    
  )
  .dependsOn(uri("git://github.com/GiGurra/service-utils.git#0.1.7"))
