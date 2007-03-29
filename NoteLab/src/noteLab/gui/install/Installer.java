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

/*
 * Some of the code in this class is based on code from the class IsawInstaller 
 * Revision 1.30 stored in the ISAW CVS on 2005/08/04 at 19:32:05.  The IsawInstaller class is 
 * from the ISAW (Integrated Spectral Analysis Workbench) project and is licensed under the GNU 
 * GPL.  The license and contact information for the IsawInstaller class is detailed below.
 * 
 * In particular, the code in the constructor used to find the jar file from which the application 
 * has been started is based on the method 
 *                                private String getJarFileName()
 * from the IsawInstaller class.
 * 
 * Also the method 
 *        private void installFromJar(File installDir, 
 *                                    File jarFileFile) throws IOException
 * in this class is based on the inner class Extractor in the IsawInstaller class.  In 
 * particular it is based on the the run() method in the Extractor inner class.
 */

/*
 * File:  Isawinstaller.java
 *
 * Copyright (C) 2002, Peter Peterson
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307, USA.
 *
 * Contact : Peter F. Peterson <pfpeterson@anl.gov>
 *           Intense Pulsed Neutron Source Division
 *           Argonne National Laboratory
 *           9700 South Cass Avenue, Bldg 360
 *           Argonne, IL 60439-4845, USA
 *
 * This work was supported by the Intense Pulsed Neutron Source Division
 * of Argonne National Laboratory, Argonne, IL 60439-4845, USA.
 *
 * For further information, see <http://www.pns.anl.gov/ISAW/>
 */

package noteLab.gui.install;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import noteLab.util.percent.PercentCalculator;
import noteLab.util.percent.PercentChangedListener;

public class Installer
{
   private static final String FILE_PREFIX = "file:";
   private static final String JAR_PREFIX = "jar:file:";
   
   private File installDir;
   
   /**
    * True if the program that was started to run this class has been 
    * run from a jar file and false if it was run from a .class file 
    * inside a directory on the filesystem.
    */
   private boolean isRootJar;
   
   private File rootFile;
   
   private PercentChangedListener listener;
   
   public Installer(File installDir, 
                    PercentChangedListener listener) throws IOException
   {
      if (installDir == null || listener == null)
         throw new NullPointerException();
      
      this.listener = listener;
      
      // first see if the install directory can be created and 
      // it is writeable
      this.installDir = installDir;
      createFile(this.installDir, true);
      
      if (!this.installDir.isDirectory())
         throw new IOException("The installation directory \""+
                               installDir.getAbsolutePath()+
                               "\" is not actually a directory.");
      
      String classname = getClass().getName();
      classname = classname.replace('.', '/');
      classname += ".class";
      
      URL classURL = ClassLoader.getSystemResource(classname);
      String classURLname = classURL.toString();
      
      if (classURLname.startsWith(JAR_PREFIX))
      {
         this.isRootJar = true;
         String fullPath = classURLname.substring(JAR_PREFIX.length());
         String suffix = "!/"+classname;
         if (!fullPath.endsWith(suffix))
            throw new IOException("The extractor appears to be have been run from an " +
                                  "executable jar file, but the jar filename could not " +
                                  "be found");
         
         this.rootFile = new File(fullPath.substring(0, 
                                                     fullPath.length()-
                                                        suffix.length()));
      }
      else
      {
         this.isRootJar = false;
         String fullPath = classURLname.substring(FILE_PREFIX.length());

         String suffix = "bin/"+classname;
         
         if (!fullPath.endsWith(suffix))
            throw new IOException("The extractor appears to have been run from a " +
                                  "directory, but the directory could not be determined.");
         
         this.rootFile = new File(fullPath.substring(0, fullPath.length()-suffix.length()));
      }
   }
   
   private static void countNumFiles(File dir, MutableInt mInt)
   {
      if (dir == null || mInt == null)
         throw new NullPointerException();
      
      // get 'dir's children
      File[] children = dir.listFiles();
      
      // If 'children == null' then 'dir' is not 
      // actually a directory.  Then its just a 
      // file so it doesn't have children.  
      // Then just return.
      if (children == null)
         return;
      
      // count the children
      mInt.value += children.length;
      
      // scan through each child
      for (File child : children)
         countNumFiles(child, mInt);
   }
   
   public void install() throws IOException
   {
      if (this.isRootJar)
         installFromJar(this.installDir, this.rootFile);
      else
         installFromDir(this.installDir, this.rootFile);
   }
   
   private void installFromJar(File installDir, 
                               File jarFileFile) throws IOException
   {
      if (installDir == null || jarFileFile == null)
         throw new NullPointerException();
      
      String jarFilename = jarFileFile.getAbsolutePath();
      jarFilename = jarFilename.replace("%20", " ");
      
      System.err.println("Installing from jar file:  "+jarFilename);
      
      JarFile jarFile = new JarFile(jarFilename);
      Enumeration<JarEntry> enteries =  jarFile.entries();
      JarEntry curEntry;
      String entryName;
      File newFile;
      InputStream inStream;
      OutputStream outStream;
      boolean isDir;
      
      PercentCalculator calc = new PercentCalculator(jarFile.size());
      
      byte[] bytesRead = new byte[1024];
      int numRead;
      
      while (enteries.hasMoreElements())
      {
         curEntry = enteries.nextElement();
         entryName = curEntry.getName();
         isDir = curEntry.isDirectory();
         
         calc.newItemProcessed();
         String message = "Installing "+entryName;
         this.listener.percentChanged(calc.getPercent(), message);
         
         newFile = new File(installDir, entryName);
         createFile(newFile, isDir);
         
         if (isDir)
            continue;
         
         inStream = jarFile.getInputStream(curEntry);
         outStream = new FileOutputStream(newFile);
         
         while ( (numRead = inStream.read(bytesRead)) > 0 )
            outStream.write(bytesRead, 0, numRead);
         
         inStream.close();
         outStream.close();
      }
      
      jarFile.close();
   }
   
   private void installFromDir(File installDir, File rootDir) throws IOException
   {
      MutableInt mInt = new MutableInt(0);
      countNumFiles(rootDir, mInt);
      
      installFromDirImpl(installDir, rootDir, rootDir, 
                         new PercentCalculator(mInt.value));
   }
   
   /**
    * 
    * @param installDir The directory to which files are written to.
    * 
    * @param rootDir The directory from which files are read from.
    * 
    * @throws IOException
    */
   private void installFromDirImpl(File installDir, 
                                   File rootDir, 
                                   File startDir, 
                                   PercentCalculator calc) throws IOException
   {
      if (installDir == null || rootDir == null || startDir == null || calc == null)
         throw new NullPointerException();
      
      if (!rootDir.isDirectory())
         throw new IOException("The file, \""+
                               rootDir.getAbsolutePath()+
                               "\" should be a directory but isn't.");
      
      createFile(installDir, true);
      
      String rootDirName = rootDir.getAbsolutePath();
      
      InputStream inStream;
      OutputStream outStream;
      
      File outFile;
      String childPath;
      String relPath;
      
      byte[] bytesRead = new byte[1024];
      int numRead = 0;
      
      for (File child : startDir.listFiles())
      {
         childPath = child.getAbsolutePath();
         relPath = childPath.substring(rootDirName.length(), childPath.length());
         
         calc.newItemProcessed();
         String message = "Installing:  "+relPath;
         this.listener.percentChanged(calc.getPercent(), message);
         
         if (child.isDirectory())
            installFromDirImpl(installDir, rootDir, child, calc);
         
         outFile = new File(installDir, relPath);
         
         createFile(outFile, child.isDirectory());
         if (outFile.isDirectory() != child.isDirectory())
            throw new IOException("The file \""+
                                  outFile.getAbsolutePath()+
                                  "\" is a directory but needs to be a regular file.");
         
         // if outfile is a directory keep going
         if (outFile.isDirectory())
            continue;
         
         inStream = new FileInputStream(child);
         outStream = new FileOutputStream(outFile);
         
         while ( (numRead = inStream.read(bytesRead)) > 0 )
            outStream.write(bytesRead, 0, numRead);
         
         inStream.close();
         outStream.close();
      }
   }
   
   private static void createFile(File file, boolean isDir) throws IOException
   {
      if (file == null)
         throw new NullPointerException();
      
      if (isDir)
      {
         if (!file.exists())
         {
            boolean dirMade = file.mkdirs();
            if (!dirMade)
               throw new IOException("The directory \""+
                                     file.getAbsolutePath()+
                                     "\" could not be constructed.");
         }
         else
         {
            boolean canWrite = file.canWrite();
            if (!canWrite)
               throw new IOException("The directory \""+
                                     file.getAbsolutePath()+
                                     "\" exists but cannot be written to.");
         }
      }
      else
      {
         if (!file.exists())
         {
            // if the file is not a directory and doesn't exist 
            // make the parent directories
            createFile(file.getParentFile(), true);
            
            try
            {
               boolean fileMade = file.createNewFile();
               if (!fileMade)
                  throw new IOException();
            }
            catch (IOException e)
            {
               throw new IOException("The file \""+
                                     file.getAbsolutePath()+
                                     "\" could not be constructed.");
            }
         }
         else
         {
            boolean canWrite = file.canWrite();
            if (!canWrite)
               throw new IOException("The file \""+
                                     file.getAbsolutePath()+
                                     "\" exists but cannot be written to.");
         }
      }
   }
   
   private class MutableInt
   {
      public int value;
      
      public MutableInt(int num)
      {
         this.value = num;
      }
   }
}
