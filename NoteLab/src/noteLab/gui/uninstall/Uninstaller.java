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

package noteLab.gui.uninstall;

import java.io.File;
import java.io.IOException;

import noteLab.gui.settings.SettingsInfoCenter;
import noteLab.util.InfoCenter;
import noteLab.util.percent.PercentCalculator;
import noteLab.util.percent.PercentChangedListener;

public class Uninstaller
{
   private static boolean HAS_BEEN_UNINSTALLED = false;
   
   private Uninstaller() {}
   
   public static void uninstall(PercentChangedListener listener, 
                                File installDir, 
                                boolean savePrefs) 
                                   throws IOException
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (installDir == null)
         throw new IOException(InfoCenter.getAppName()+" could not be " +
                               "uninstalled because the directory to which it " +
                               "was installed could not be determined.");
      
      if (HAS_BEEN_UNINSTALLED)
      {
         listener.percentChanged(100, "Uninstallation complete");
         return;
      }
      
      File appHome = InfoCenter.getAppHome();
      
      MutableInt mInt = new MutableInt(0);
      countNumFiles(installDir, mInt);
      countNumFiles(appHome, mInt);
      
      // don't count the installation directory
      PercentCalculator calc = new PercentCalculator(Math.max(0, mInt.num-1));
      
      delete(installDir, calc, listener, savePrefs, installDir);
      delete(appHome, calc, listener, savePrefs, installDir);
      
      HAS_BEEN_UNINSTALLED = true;
   }
   
   private static void delete(File file, 
                              PercentCalculator calc, 
                              PercentChangedListener listener, 
                              boolean savePrefs, 
                              File installDir) 
                                 throws IOException
   {
      if (file == null || calc == null || 
          listener == null || installDir == null)
         throw new NullPointerException();
      
      if (file.isDirectory())
      {
         File[] children = file.listFiles();
         if (children != null)
            for (File child : children)
               if (!savePrefs || 
                   !SettingsInfoCenter.getSettingsFile().equals(child))
                      delete(child, calc, listener, savePrefs, installDir);
      }
      
      int numChildren = 0;
      File[] children = file.listFiles();
      if (children != null)
         numChildren = children.length;
      
      // The installation directory will be removed by the 
      // uninstallation scripts.  This is because on Windows based 
      // systems the install directory cannot be deleted since it 
      // is being used by the uninstall program.
      if (numChildren == 0 && !file.equals(installDir))
      {
         boolean del = file.delete();
         if (!del)
            throw new IOException("The file "+file.getAbsolutePath()+
                                  " could not be deleted.");
         
         calc.newItemProcessed();
         listener.percentChanged(calc.getPercent(), 
                                 "Deleted "+file.getAbsolutePath());
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
      mInt.num += children.length;
      
      // scan through each child
      for (File child : children)
         countNumFiles(child, mInt);
   }
   
   private static class MutableInt
   {
      public int num;
      
      public MutableInt(int num)
      {
         this.num = num;
      }
   }
}
