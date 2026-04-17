@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script
@REM ----------------------------------------------------------------------------
@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET %%ENV_VAR%%=
@SETLOCAL
@SET MAVEN_PROJECTBASEDIR=%~dp0
@IF NOT "%MAVEN_BASEDIR%"=="" SET MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%

@SET WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
@SET WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

@SET JAVA_HOME_CANDIDATE=
FOR /F "usebackq tokens=2,*" %%A IN (`REG QUERY HKLM\Software\JavaSoft\JDK /v CurrentVersion 2^>nul`) DO SET JAVA_VERSION=%%B
FOR /F "usebackq tokens=2,*" %%A IN (`REG QUERY "HKLM\Software\JavaSoft\JDK\%JAVA_VERSION%" /v JavaHome 2^>nul`) DO SET JAVA_HOME_CANDIDATE=%%B

IF NOT "%JAVA_HOME_CANDIDATE%"=="" IF NOT DEFINED JAVA_HOME SET JAVA_HOME=%JAVA_HOME_CANDIDATE%

IF NOT EXIST %WRAPPER_JAR% (
    IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" mkdir "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper"
    powershell -Command "& {$webclient = new-object System.Net.WebClient; $webclient.DownloadFile(%WRAPPER_URL%, %WRAPPER_JAR%)}"
)

%JAVA_HOME%\bin\java.exe ^
  -classpath %WRAPPER_JAR% ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*
@ENDLOCAL
