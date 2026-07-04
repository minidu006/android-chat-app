@ECHO OFF
IF EXIST gradle\wrapper\gradle-wrapper.jar (
  java -classpath gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain %*
) ELSE (
  ECHO Gradle wrapper jar is missing. Open this project in Android Studio and let it sync using the IDE.
  EXIT /B 1
)
