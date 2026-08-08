@echo off
set "MAVEN_PROJECT_BASEDIR=%~dp0"
if "%MAVEN_PROJECT_BASEDIR:~-1%"=="\" set "MAVEN_PROJECT_BASEDIR=%MAVEN_PROJECT_BASEDIR:~0,-1%"
java "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECT_BASEDIR%" -classpath "%MAVEN_PROJECT_BASEDIR%\.mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %*
