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

import javax.swing.filechooser.FileFilter;

import noteLab.util.InfoCenter;

public class JarnalFileFilter extends FileFilter
{
   private final String DESC = "Jarnal files (*"+
                               InfoCenter.getJarnalExtension()+")";

   public boolean accept(File pathname)
   {
      if (pathname == null)
         return false;

      if (pathname.isDirectory())
         return true;

      String path = pathname.getPath().toLowerCase();
      String suffix = InfoCenter.getJarnalExtension().toLowerCase();
      return path.endsWith(suffix);
   }
   
   @Override
   public String getDescription()
   {
      return this.DESC;
   }
}
