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

package noteLab.gui.chooser;

import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileView;

import noteLab.gui.DefinedIcon;
import noteLab.util.InfoCenter;

public class NoteLabFileView extends FileView
{
   private static final int SIZE = 24;
   
   private static final ImageIcon FILE_ICON = 
                                     DefinedIcon.page.getIcon(SIZE);
   private static final ImageIcon DIR_ICON = 
                                     DefinedIcon.directory.getIcon(SIZE);
   private static final ImageIcon IMAGE_ICON = 
                                     DefinedIcon.image_page.getIcon(SIZE);
   private static final ImageIcon NTLB_ICON = 
                                     DefinedIcon.feather.getIcon(SIZE);
   private static final ImageIcon JARNAL_ICON = 
                                     DefinedIcon.college_rule.getIcon(SIZE);
   private static final ImageIcon DRIVE_ICON = 
                                     DefinedIcon.server.getIcon(SIZE);
   
   public NoteLabFileView()
   {
   }

   @Override
   public String getDescription(File f)
   {
      return super.getDescription(f);
   }

   @Override
   public Icon getIcon(File f)
   {
      if (f == null)
         throw new NullPointerException();
      
      if (f.isDirectory())
         return DIR_ICON;
      
      for (File root : File.listRoots())
         if (f.equals(root))
            return DRIVE_ICON;
      
      String nativeExt = InfoCenter.getFileExtension().toLowerCase();
      String jarnalExt = InfoCenter.getJarnalExtension().toLowerCase();
      
      String name = f.getName().toLowerCase();
      if (name.endsWith(nativeExt))
         return NTLB_ICON;
      else if (name.endsWith(jarnalExt))
         return JARNAL_ICON;
      else if (name.endsWith(".svg") || isImage(name))
         return IMAGE_ICON;
      
      return FILE_ICON;
   }
   
   private boolean isImage(String name)
   {
      String[] exts = ImageIO.getWriterFormatNames();
      for (String ext : exts)
         if (name.endsWith(ext.toLowerCase()))
            return true;
      
      return false;
   }
   
   @Override
   public String getTypeDescription(File f)
   {
      return super.getTypeDescription(f);
   }
}
