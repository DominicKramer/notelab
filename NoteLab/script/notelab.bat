@echo off

REM This is the NoteLab startup script for Windows based systems.  It is used to start NoteLab with specific
REM virtual machine arguments as well as NoteLab specific arguments.  These arguments are those that are
REM specified by the user using NoteLab's settings control.

REM Please DO NOT EDIT this file as it is responsible for starting NoteLab.  If this file is corrupted NoteLab
REM may not start.  If you want to edit this file, it is advised that you first make a backup.

REM Determine the batch file that serves to initialize the environment for the Java virtual machine and NoteLab.
SET INIT_FILE=%HOMEDRIVE%%HOMEPATH%\.NoteLab\initenv.bat

REM If the file above exists invoke it so that the environment is initialized.
IF EXIST "%INIT_FILE%" CALL "%INIT_FILE%"

REM Get the installation directory, i.e. the current working directory
SET INSTALL_DIR=%CD%

REM Move to the installation directory
cd "%INSTALL_DIR%"

REM Start the Java virtual machine with the given VM arguments and instruct it to load NoteLab with NoteLab's arguments
java %NOTELAB_VM_ARGS% -DNOTELAB_SETTINGS_FILENAME="%INIT_FILE%" -cp .;./info;"%CLASSPATH%" noteLab.util.StartupUtilities %NOTELAB_ARGS% %*

REM Reset all of the constructed variables
set INIT_FILE=
set NOTELAB_VM_ARGS=
set NOTELAB_ARGS=
set INSTALL_DIR=
