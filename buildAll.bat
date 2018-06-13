@echo off
set path=%SystemRoot%\system32;%SystemRoot%;%SystemRoot%\System32\Wbem;%JAVA_HOME%\bin;%ANT_HOME%\bin;
set ANT_OPTS=-Xmx512m
set CLASSPATH=%JAVA_HOME%\lib\tools.jar;%JAVA_HOME%\lib\dt.jar

set ProjectRootPath=%cd% 
echo "current project root path :%ProjectRootPath%"

set Project_Juice=JuiceBottom
set Project_Common=Common
set Project_DBCommon=DBCommon
set Project_DataSystem=DBSystem
set Project_GameServer=GameTaurus
set Project_TaurusClub=TaurusClub
set Project_Pay=Pay

set Project_Login=Login

echo --------------------Start build all project--------------------

@rem JuiceBottom
call :antBuildJar %Project_Juice%
@rem copy Juice to All dependence project
xcopy bin\%Project_Juice%.jar %Project_DBCommon%\lib\%Project_Juice%.jar /y
xcopy bin\%Project_Juice%.jar %Project_Common%\lib\%Project_Juice%.jar /y
xcopy bin\%Project_Juice%.jar %Project_DataSystem%\lib\%Project_Juice%.jar /y
xcopy bin\%Project_Juice%.jar %Project_GameServer%\lib\%Project_Juice%.jar /y
xcopy bin\%Project_Juice%.jar %Project_Login%\lib\%Project_Juice%.jar /y
xcopy bin\%Project_Juice%.jar %Project_Pay%\lib\%Project_Juice%.jar /y
xcopy bin\%Project_Juice%.jar %Project_TaurusClub%\lib\%Project_Juice%.jar /y

@rem DBCommon
call :antBuildJar %Project_DBCommon%
@rem copy DBCommon to All dependence project
xcopy bin\%Project_DBCommon%.jar %Project_Common%\lib\%Project_DBCommon%.jar /y
xcopy bin\%Project_DBCommon%.jar %Project_DataSystem%\lib\%Project_DBCommon%.jar /y
xcopy bin\%Project_DBCommon%.jar %Project_GameServer%\lib\%Project_DBCommon%.jar /y
xcopy bin\%Project_DBCommon%.jar %Project_Login%\lib\%Project_DBCommon%.jar /y
xcopy bin\%Project_DBCommon%.jar %Project_Pay%\lib\%Project_DBCommon%.jar /y
xcopy bin\%Project_DBCommon%.jar %Project_TaurusClub%\lib\%Project_DBCommon%.jar /y

@rem Common
call :antBuildJar %Project_Common%
@rem copy Juice to All dependence project
xcopy bin\%Project_Common%.jar %Project_GameServer%\lib\%Project_Common%.jar /y
xcopy bin\%Project_Common%.jar %Project_TaurusClub%\lib\%Project_Common%.jar /y
xcopy bin\%Project_Common%.jar %Project_Login%\lib\%Project_Common%.jar /y
xcopy bin\%Project_Common%.jar %Project_Pay%\lib\%Project_Common%.jar /y

@rem DBSystem
call :antBuildJar %Project_DataSystem%
@rem copy DataSystem to All dependence project --- no dependence

@rem GameTaurus
call :antBuildJar %Project_GameServer%
@rem copy GameTaurus to All dependence project --- no dependence

@rem TaurusClub
call :antBuildJar %Project_TaurusClub%
@rem copy LoginServer to All dependence project --- no dependence

@rem Login
call :antBuildJar %Project_Login%
@rem copy LoginServer to All dependence project --- no dependence

@rem Pay
call :antBuildJar %Project_Pay%
@rem copy PAY to All dependence project --- no dependence


echo 
echo -----------------------------------------------------------------
echo --------------------build all project success--------------------
echo -----------------------------------------------------------------
echo 
pause
exit

:antBuildJar
echo === build project %1 start ===
call ant.bat -f "%1\build.xml"
if "%errorlevel%"=="0" (
echo === build project %1 success ===
)else (
echo === build project %1 failed,please check... ===
pause
exit
)