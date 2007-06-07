#!/bin/bash

# This is the NoteLab uninstall script for Mac OS X.

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
INSTALL_DIR=`dirname $0`

# Custom arguments for Mac OS X
DOCK_ARG=-Xdock:name=NoteLab
ICON_ARG=-Xdock:icon=${INSTALL_DIR}/noteLab/icons/feather.png

# Start the Java virtual machine and have it load the uninstaller
java ${DOCK_ARG} ${ICON_ARG} -cp ${INSTALL_DIR}:${INSTALL_DIR}/info noteLab.gui.uninstall.UninstallFrame ${INSTALL_DIR}

# Finish up by removing the installation directory
rmdir ${INSTALL_DIR}
