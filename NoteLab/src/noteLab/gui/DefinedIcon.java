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

package noteLab.gui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Hashtable;

import javax.swing.ImageIcon;

import noteLab.util.InfoCenter;

public enum DefinedIcon
{
   save_as,
   zoom_fit,
   unselect,
   delete_stroke,
   cut,
   wide_rule,
   compose,
   directory,
   paintbrush,
   page,
   paste,
   print_preview,
   select_page,
   dialog_info,
   refresh,
   college_rule,
   dialog_warning,
   noread,
   select,
   nowrite,
   remove,
   configure,
   zoom_in,
   zoom_100,
   revert_to_saved,
   zoom_out,
   copy_page,
   image_page,
   ok,
   pencil,
   copy_stroke,
   graph,
   floppy,
//   black_arrow,
   about,
   un,
   select_stroke,
   resize_stroke,
   dialog_error,
   close,
   delete_page,
   quit,
   preferences,
   export,
   dialog_question,
   book,
   print,
   move_stroke,
   logo, 
   feather, 
   box_select, 
   box_unselect, 
   drive_cdrom, 
   drive_harddisk, 
   jump, 
   server, 
   select_all, 
   unselect_all, 
   paste_down, 
   feather_invert, 
   feather_down, 
   undo, 
   redo, 
   forward, 
   backward, 
   up, 
   down, 
   unselect_page, 
   select_all_page, 
   unselect_all_page, 
   document, 
   new_page, 
   eraser;
   
   private static final ImageIcon EMPTY_ICON = 
                                    new ImageIcon(
                                          new BufferedImage(128, 128, 
                                                            BufferedImage.
                                                               TYPE_INT_ARGB));
   
   private static final Hashtable<Integer, ImageIcon> EMPTY_ICON_TABLE = 
                                                         new Hashtable<Integer, ImageIcon>();
   
   public static final int ORIGINAL_SIZE = -1;
   
   private static final String EXT = ".png";
   
   private ImageIcon initIcon;
   private Hashtable<Integer, ImageIcon> iconTable;
   
   private DefinedIcon()
   {
      this.initIcon = InfoCenter.getImage(this);
      this.iconTable = new Hashtable<Integer, ImageIcon>();
      checkInitIcon();
   }
   
   private void checkInitIcon()
   {
      if (this.initIcon == null)
         this.initIcon = EMPTY_ICON;
   }
   
   @Override
   public String toString()
   {
      return super.toString()+EXT;
   }
   
   public static ImageIcon getEmptyIcon(int size)
   {
      return getIcon(EMPTY_ICON, EMPTY_ICON_TABLE, size);
   }
   
   public ImageIcon getIcon(int size)
   {
      return getIcon(this.initIcon, this.iconTable, size);
   }
   
   private static ImageIcon getIcon(ImageIcon initIcon, 
                                    Hashtable<Integer, ImageIcon> iconTable, 
                                    int size)
   {
      if (size == ORIGINAL_SIZE)
         return initIcon;
      
      ImageIcon sizeIcon = iconTable.get(size);
      if (sizeIcon != null)
         return sizeIcon;
      
      if (initIcon == null)
         initIcon = EMPTY_ICON;
      
      Image scaleImage = initIcon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
      
      ImageIcon newIcon = new ImageIcon(scaleImage);
      iconTable.put(size, newIcon);
      
      return newIcon;
   }
}
