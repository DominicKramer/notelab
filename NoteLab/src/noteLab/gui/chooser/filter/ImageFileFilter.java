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

package noteLab.gui.chooser.filter;

import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileFilter;

import noteLab.util.InfoCenter;

public class ImageFileFilter extends FileFilter
{
   public static final String[] EXT_ARR;
   static
   {
      String[] tmpArr = ImageIO.getWriterFormatNames();
      String[] newArr = new String[tmpArr.length+2];
      System.arraycopy(tmpArr, 0, newArr, 0, tmpArr.length);
      newArr[newArr.length-2] = InfoCenter.getSVGExt().
                                              replace('.', ' ').trim();
      newArr[newArr.length-1] = InfoCenter.getZippedSVGExt().
                                              replace('.', ' ').trim();
      EXT_ARR = newArr;
   }
   
   private static final String DESCRIPTION;
   static
   {
      final String starStr = "*.";
      final String commaStr = ", ";
      
      StringBuffer buffer = new StringBuffer();
      for (int i=0; i<EXT_ARR.length; i++)
      {
         buffer.append(starStr);
         buffer.append(EXT_ARR[i]);
         if ( i != (EXT_ARR.length-1) )
            buffer.append(commaStr);
      }
      
      DESCRIPTION = buffer.toString();
   };
   
   @Override
   public boolean accept(File f)
   {
      if (f == null)
         return false;
      
      if (f.isDirectory())
         return true;
      
      String path = f.getAbsolutePath().toLowerCase();
      for (String ext : EXT_ARR)
         if (path.endsWith(ext.toLowerCase()))
            return true;
      
      return false;
   }

   @Override
   public String getDescription()
   {
      return DESCRIPTION;
   }
}
