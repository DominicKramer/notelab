@echo off

REM This is the NoteLab uninstall script for Windows based systems.

REM Please DO NOT EDIT this file as it is responsible for uninstall NoteLab.  
REM If this file is corrupted you may not be able to uninstall NoteLab. 
REM If you want to edit this file, it is advised that you first make a backup.

REM First store the installation directory, i.e. the current working directory
CHDIR > dirname.txt
SET /p INSTALL_DIR=<dirname.txt

REM Start the Java virtual machine and have it load the uninstaller
java -cp %INSTALL_DIR%;%INSTALL_DIR%/info noteLab.gui.uninstall.UninstallFrame %INSTALL_DIR%

REM Finish up by removing the installation directory
REM First change to the home directory so the install directory is not in use.
cd %HOMEDRIVE%%HOMEPATH%

REM Then remove the installation directory
rmdir %INSTALL_DIR%

REM Reset all of the constructed variables
set INSTALL_DIR=
