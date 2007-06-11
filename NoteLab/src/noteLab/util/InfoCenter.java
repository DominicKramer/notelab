/*
 *  NoteLab:  An advanced note taking application for pen-enabled platforms
 *  
 *  Copyright (C) 2006, Dominic Kramer
 *  
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *  
 *  For any questions or comments please contact:  
 *    Dominic Kramer
 *    kramerd@iastate.edu
 */

package noteLab.util;

import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;

import noteLab.gui.DefinedIcon;

/**
 * This class is used to get information about the application such 
 * as its name, current version, and file extension.
 * 
 * @author Dominic Kramer
 */
public class InfoCenter
{
   /**
    * Represents an operating system, either Windows-based or UNIX-based.
    * 
    * @author Dominic Kramer
    */
   public enum OSType
   {
      /** Represents a Windows-based operating system. */
      Windows, 
      
      /** Represents a UNIX-based operating system. */
      Unix;
      
      public boolean isMacOSX()
      {
         String name = System.getProperty("os.name");
         if (name == null)
            return false;
         
         return name.toLowerCase().contains("mac");
      }
   }
   
   /** The application's name. */
   private static final String NAME = "NoteLab";
   
   /** The application's version. */
   private static final String VERSION = "0.1.4rc14";
   
   /** The file extension for this application's native file. */
   private static final String NATIVE_EXT = ".ntlb";
   
   /** The name of this application's author. */
   private static final String AUTHOR = "Dominic Kramer";
   
   /** This application's author's email address. */
   private static final String AUTHOR_EMAIL = "kramerd@users.sourceforge.net";
   
   /** This application's homepage. */
   private static final String HOMEPAGE = "java-notelab.sourceforge.net";
   
   /** The application's copyright statement. */
   private static final String COPYRIGHT = "Copyright (C) 2006-2007 "+AUTHOR;
   
   /** A description of this application. */
   private static final String DESCRIPTION;
   static
   {
      StringBuffer buffer = new StringBuffer(getAppName());
      buffer.append(" version ");
      buffer.append(getAppVersion());
      buffer.append("\n");
      buffer.append(getCopyright());
      buffer.append("\n\n");
      buffer.append("This application is open source software proudly licensed under the ");
      buffer.append("GNU GPL (www.gnu.org).\n\n");
      buffer.append(getAppName());
      buffer.append(" is an advanced \"digital notebook\" specifically designed for tablet ");
      buffer.append("computers.  With its stroke smoothing, antialiasing, \"smart rendering\" ");
      buffer.append("(for increased performance), and document storage in the industry ");
      buffer.append("standard SVG format, ");
      buffer.append(getAppName());
      buffer.append(" provides a beautiful and powerful note taking environment ");
      buffer.append("on any operating system.\n\n");
      
      buffer.append("Webpage:   ");
      buffer.append(getHomepage());
      buffer.append("\n");
      
      buffer.append("Author:    ");
      buffer.append(getAuthor());
      buffer.append("\n");
      
      buffer.append("Email:     ");
      buffer.append(getAuthorEmail());
      buffer.append("\n");
      
      buffer.append("License:   GNU GPL\n");
      
      buffer.append("Filetype:  ");
      buffer.append(getFileExtension());
      buffer.append("\n");
      
      DESCRIPTION = buffer.toString();
   }
   
   /**
    * The path in the classpath that points to the directory that 
    * contains the application's icons.
    */
   private static final String ICON_PATH_NAME = "noteLab/icons/";
   
   /**
    * The path, relative to the application's home directory, that points 
    * to the directory that contains the application's preference 
    * information.
    */
   private static final String PREF_HOME_NAME = "preferences";
   
   /**
    * The file on the user's hard drive that points to the 
    * application's home directory.
    */
   private static final File APP_HOME;
   static
   {
      File userHome = new File(System.getProperty("user.home"));
      APP_HOME = new File(userHome, "."+getAppName());
   }
   
   /**
    * The file on the user's hard drive that points to the 
    * directory that contains the application's preferences.
    */
   private static final File PREF_HOME = 
                                new File(getAppHome(), PREF_HOME_NAME);
   
   /** The file extension of executable scripts on Windows-based systems. */
   private static final String WINDOWS_SCRIPT_EXT = ".bat";
   
   /** The file extension of executable scripts on UNIX-based systems. */
   private static final String UNIX_SCRIPT_EXT = ".sh";
   
   /**
    * The file extension of scripts that can be executed through the 
    * Mac OS X's Finder application.
    */
   private static final String MAC_SCRIPT_EXT = ".command";
   
   /** The file extension of Jarnal files. */
   private static final String JARNAL_EXT = ".jaj";
   
   /** The array of file extensions that NoteLab supports opening. */
   private static final String[] SUPPORTED_EXT_ARR = 
                                    new String[] {
                                                    NATIVE_EXT, 
                                                    JARNAL_EXT
                                                 };
   
   /** The initial amount of memory given to the application. */
   private static final double INIT_MEMORY_MB = getTotalUsedMemoryMb();
   
   private static final String SVG_EXT = "svg";
   private static final String SVGZ_EXT = "svgz";
   
   /** Private to enforce static access to this class's members. */
   private InfoCenter()
   {}
   
   /**
    * Used to retrieve the application's name.
    * 
    * @return The application's name.
    */
   public static final String getAppName()
   {
      return NAME;
   }
   
   /**
    * Used to retrieve the URL of this application's homepage.
    * 
    * @return The application's homepage.
    */
   public static final String getHomepage()
   {
      return HOMEPAGE;
   }
   
   /**
    * Used to retrieve the application's author.
    * 
    * @return The author's name.
    */
   public static final String getAuthor()
   {
      return AUTHOR;
   }
   
   /**
    * Used to retrieve the email address of this 
    * application's author.
    * 
    * @return The author's email address.
    */
   public static final String getAuthorEmail()
   {
      return AUTHOR_EMAIL;
   }
   
   /**
    * Used to retrieve this application's copyright 
    * description.
    * 
    * @return The application's copyright description.
    */
   public static final String getCopyright()
   {
      return COPYRIGHT;
   }
   
   /**
    * Used to retrieve a human readable description of 
    * this application.
    * 
    * @return A description of this application.
    */
   public static final String getDescription()
   {
      return DESCRIPTION;
   }
   
   /**
    * Used to retrieve the application's current version.
    * 
    * @return The application's version.
    */
   public static final String getAppVersion()
   {
      return VERSION;
   }
   
   /**
    * Used to retrieve the application's native file's extension.
    * 
    * @return The application's native file's extension.
    */
   public static final String getFileExtension()
   {
      return NATIVE_EXT;
   }
   
   /**
    * Used to retrieve the directory where the application stores any 
    * files that it needs.
    * 
    * @return The application's home directory.
    */
   public static final File getAppHome()
   {
      return APP_HOME;
   }
   
   /**
    * Used to retrieve the directory where the application's 
    * preference information is stored.  (This directory is a 
    * subdirectory of the application's home directory).
    * 
    * @return The application's preference directory.
    */
   public static final File getPreferencesHome()
   {
      return PREF_HOME;
   }
   
   /**
    * Used to determine if the application's home directory is valid.  
    * The directory is valid, if it and its subdirectories either 
    * exist, or can be made, and are readable and writable.
    * 
    * @return <code>null</code> if the directory is valid and a 
    *         <code>non-null<code> string describing the error if 
    *         one exists.
    */
   public static final String isAppHomeValid()
   {
      String result = isDirValid(getAppHome());
      if (result != null)
         return result;
      
      result = isDirValid(getPreferencesHome());
      if (result != null)
         return result;
      
      return null;
   }
   
   /**
    * Used to build the application's home directory if it doesn't 
    * already exist.  If any of the subdirectories in the application's 
    * home directory already exist, they will not be made.
    * 
    * @return <code>null</code> if the directory was successfully 
    *         made and a <code>non-null</code> string describing 
    *         the error if one occured.
    */
   public static final String buildAppHome()
   {
      String result = mkdir(getAppHome());
      if (result != null)
         return result;
      
      result = mkdir(getPreferencesHome());
      if (result != null)
         return result;
      
      return null;
   }
   
   /**
    * Used to make the given directory if it does not already exist.
    * 
    * @param dir The directory to make.
    * 
    * @return <code>null</code> if the directory either exists or 
    *         was constructed if it doesn't exist.  A 
    *         <code>non-null</code> string is returned describing 
    *         that the directory could not be constructed.
    */
   private static final String mkdir(File dir)
   {
      if (dir == null)
         throw new NullPointerException();
      
      if (!dir.exists())
      {
         boolean success = dir.mkdir();
         if (!success)
            return "The directory '"+dir.getAbsolutePath()+ 
                   "' does not exist and could not be created.";
      }
      
      return null;
   }
   
   /**
    * Used to determine if the given file corresponds to a valid 
    * directory.  The file is considered a valid directory if:
    * <ol>
    *   <li>It exists</li>
    *   <li>It is a directory</li>
    *   <li>It has read access</li>
    *   <li>It has write access</li>
    * </ol>
    * 
    * @param dir The file to analyze.
    * 
    * @return <code>null</code> if the file is a valid directory 
    *         and a <code>non-null</code> string describing the 
    *         error if one exists.
    */
   private static final String isDirValid(File dir)
   {
      if (dir == null)
         throw new NullPointerException();
      
      if (!dir.exists())
         return "The directory '"+dir.getAbsolutePath()+
                "' does not exist.";
      
      if (!dir.isDirectory())
         return "The file '"+dir.getAbsolutePath()+"' needs to be a " +
                "directory but is not.";
      
      if (!dir.canRead())
         return "The directory '"+dir.getAbsolutePath()+
                "' does not have read access.";
      
      if (!dir.canWrite())
         return "The directory '"+dir.getAbsolutePath()+
                "' does not have write access.";
      
      return null;
   }
   
   /**
    * Used to construct an image from the given <code>DefinedIcon</code>.
    * 
    * @param icon Describes an icon located in the directory that holds 
    *             the application's icons.
    * 
    * @return The constructed image or <code>null</code> if it could not 
    *         be constructed.
    */
   public static ImageIcon getImage(DefinedIcon icon)
   {
      String name = ICON_PATH_NAME+icon.toString();
      URL imageURL = ClassLoader.getSystemClassLoader().getResource(name);
      if (imageURL == null)
         return null;
      
      return new ImageIcon(imageURL);
   }
   
   /**
    * Used to get the operating system that NoteLab is currently running on.
    * 
    * @return The operating system NoteLab is currently running on, either 
    *         Windows-based or UNIX-based.
    */
   public static OSType getOperatingSystem()
   {
      String name = System.getProperty("os.name");
      if (name == null)
         return OSType.Unix;
      
      if (name.toLowerCase().contains("windows"))
         return OSType.Windows;
      
      return OSType.Unix;
   }
   
   /**
    * Used to get the extension of the executable script of the operating system 
    * NoteLab is currently running on.
    * 
    * @return The extension of the executable script on the current operating system.
    */
   public static String getScriptExtension()
   {
      OSType os = getOperatingSystem();
      if (os.equals(OSType.Windows))
         return WINDOWS_SCRIPT_EXT;
      
      // the operating system is a UNIX system
      if (os.isMacOSX())
         return MAC_SCRIPT_EXT;
      
      return UNIX_SCRIPT_EXT;
   }
   
   public static String getUnixScriptExtension()
   {
      return UNIX_SCRIPT_EXT;
   }
   
   public static String getWindowsScriptExtension()
   {
      return WINDOWS_SCRIPT_EXT;
   }
   
   public static String getMacScriptExtension()
   {
      return MAC_SCRIPT_EXT;
   }
   
   /**
    * Returns the maximum amount of memory, in megabytes, that NoteLab can use.
    * 
    * @return The maximum number of megabytes of memory that NoteLab can use.
    */
   public static double getMaxMemoryMb()
   {
      return convertToMb(Runtime.getRuntime().maxMemory());
   }
   
   /**
    * Returns the total amount of memory, in megabytes, currently used by NoteLab.
    * 
    * @return The number of megabytes of memory currently used by NoteLab.
    */
   public static double getTotalUsedMemoryMb()
   {
      return convertToMb(Runtime.getRuntime().totalMemory());
   }
   
   /**
    * Returns the amount of memory free memory, in megabytes, that NoteLab can still use.  
    * This includes the amount of memory allocated for NoteLab, but not already used, in 
    * addition to the amount of memory that can be additionally allocated for NoteLab.
    * 
    * @return The number of megabytes of free memory NoteLab can still use.
    */
   public static double getTotalFreeMemoryMb()
   {
      Runtime run = Runtime.getRuntime();
      double freeMB = convertToMb(run.freeMemory());
      double maxMB = convertToMb(run.maxMemory());
      double totalMB = convertToMb(run.totalMemory());
      
      // the total free is the amount free inside the 
      // virtual machine plus the extra memory not 
      // already used by the virtual machine.
      return freeMB+(maxMB-totalMB);
   }
   
   /**
    * Returns the amount of memory given to the application when it was first 
    * started.  This value is the amount reported by the runtime system at 
    * startup.
    * 
    * @return The amount of memory given to the application at startup in 
    *         megabytes.
    */
   public static double getInitialMemoryMb()
   {
      return INIT_MEMORY_MB;
   }
   
   /**
    * Used to convert a number of bytes to megabytes.
    * 
    * @param numBytes The number of bytes.
    * 
    * @return The corresponding number of megabytes.
    */
   private static double convertToMb(long numBytes)
   {
      double numKb = numBytes/1024.0;
      return numKb/1024.0;
   }
   
   /**
    * Used to get the file extension of Jarnal files.
    * 
    * @return The file extension of Jarnal files.
    */
   public static String getJarnalExtension()
   {
      return JARNAL_EXT;
   }
   
   /**
    * Used to get the file extensions of the files that NoteLab supports opening.
    * 
    * @return The array of file extensions that NoteLab supports opening.
    */
   public static String[] getSupportedExtensions()
   {
      return SUPPORTED_EXT_ARR;
   }
   
   public static String getSVGExt()
   {
      return SVG_EXT;
   }
   
   public static String getZippedSVGExt()
   {
      return SVGZ_EXT;
   }
   
   /**
    * Testbed.
    * 
    * @param args Unused.
    */
   public static void main(String[] args)
   {
      System.out.println("Building the app home:  '"+getAppHome()+"'");
      System.out.println(buildAppHome());
      
      System.out.println("Validating the app home");
      System.out.println(isAppHomeValid());
      
      System.out.println("Description");
      System.out.println(getDescription());
   }
}
