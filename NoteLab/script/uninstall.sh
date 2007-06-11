#!/bin/bash

# This is the NoteLab uninstall script for Unix based systems (Linux and Solaris).

# Please DO NOT EDIT this file as it is responsible for uninstalling NoteLab.  
# If this file is corrupted you may not be able to uninstall NoteLab.
# If you want to edit this file, it is advised that you first make a backup.

# NoteLab's home directory
NOTELAB_HOME=${HOME}/.NoteLab

# Determine the file that contains information about the installation directory
INSTALL_DIR_ENV=${NOTELAB_HOME}/init_install_env.sh

# Determine the installation directory, i.e. the directory that contains this script.  
# '$0' stores the absolute path to this script and the 'dirname' command returns the 
# absolute path of the parent directory of '$0'.
INSTALL_DIR=`dirname "$0"`

# Move to the installation directory
cd "${INSTALL_DIR}"

# Start the Java virtual machine and have it load the uninstaller
java -cp .:./info:"${CLASSPATH}" noteLab.gui.uninstall.UninstallFrame "${INSTALL_DIR}"

# Finish up by removing the installation directory
cd "${HOME}"
rmdir "${INSTALL_DIR}"
