#!/bin/bash

# This is the NoteLab startup script for Unix based systems (Linux and Solaris).  It is used to start 
# NoteLab with specific virtual machine arguments as well as NoteLab specific arguments.  These arguments are those 
# that are specified by the user using NoteLab's settings control.

# Please DO NOT EDIT this file as it is responsible for starting NoteLab.  If this file is corrupted NoteLab
# may not start.  If you want to edit this file, it is advised that you first make a backup.

# NoteLab's home directory
NOTELAB_HOME=${HOME}/.NoteLab

# Determine the shell script that serves to initialize the environment for the Java virtual machine and NoteLab.
INIT_FILE=${NOTELAB_HOME}/initenv.sh

# If the file above exists source it so that the environment is initialized.
if [ -e "${INIT_FILE}" ]; then 
  source "${INIT_FILE}"; 
fi

# Determine the installation directory, i.e. the directory that contains this script.  
# '$0' stores the absolute path to this script and the 'dirname' command returns the 
# absolute path of the parent directory of '$0'.
INSTALL_DIR=`dirname "$0"`

# Move to the installation directory
cd "${INSTALL_DIR}"

# Start the Java virtual machine with the given VM arguments and instruct it to load NoteLab with NoteLab's arguments
java ${NOTELAB_VM_ARGS} -DNOTELAB_SETTINGS_FILENAME="${INIT_FILE}" -cp .:./info:${CLASSPATH} noteLab.util.StartupUtilities ${NOTELAB_ARGS} $*
