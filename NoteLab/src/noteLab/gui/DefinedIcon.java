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
   goto_top,
   goto_first_rtl,
   undo,
   save_as,
   zoom_fit,
   redo_rtl,
   unselect,
   delete_stroke,
   cut,
   wide_rule,
   go_down,
   compose,
   goto_first_ltr,
   directory,
   redo_ltr,
   paintbrush,
   page,
   go_up,
   paste,
   print_preview,
   select_page,
   goto_last_rtl,
   dialog_info,
   refresh,
   college_rule,
   dialog_warning,
   noread,
   select,
   nowrite,
   go_forward_rtl,
   goto_bottom,
   remove,
   go_back_rtl,
   goto_last_ltr,
   configure,
   zoom_in,
   zoom_100,
   revert_to_saved,
   go_forward_ltr,
   zoom_out,
   copy_page,
   image_page,
   go_back_ltr,
   ok,
   pencil,
   copy_stroke,
   graph,
   floppy,
   black_arrow,
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
   redo,
   print,
   move_stroke,
   logo, 
   feather, 
   box_select, 
   box_unselect, 
//   empty, 
   drive_cdrom, 
   drive_harddisk, 
   jump, 
   server, 
   select_all, 
   unselect_all;
   
   private static final ImageIcon EMPTY_ICON = 
                                    new ImageIcon(
                                          new BufferedImage(128, 128, 
                                                            BufferedImage.
                                                               TYPE_INT_ARGB));
   
   public static final int ORIGINAL_SIZE = -1;
   
   private static final String EXT = ".png";
   
   private ImageIcon initIcon;
   private Hashtable<Integer, ImageIcon> iconTable;
   
   private DefinedIcon()
   {
//      if (this != DefinedIcon.empty)
         this.initIcon = InfoCenter.getImage(this);
      this.iconTable = new Hashtable<Integer, ImageIcon>();
      checkInitIcon();
   }
   
   private void checkInitIcon()
   {
      if (this.initIcon == null)
         this.initIcon = EMPTY_ICON;
   }
   
   public static ImageIcon getEmptyIcon(int size)
   {
      Image scaleImage = EMPTY_ICON.getImage().getScaledInstance(size, size, Image.SCALE_FAST);
      return new ImageIcon(scaleImage);
   }
   
   @Override
   public String toString()
   {
      return super.toString()+EXT;
   }
   
   public ImageIcon getIcon(int size)
   {
      if (size == ORIGINAL_SIZE)
         return this.initIcon;
      
      ImageIcon sizeIcon = this.iconTable.get(size);
      if (sizeIcon != null)
         return sizeIcon;
      
      if (this.initIcon == null)
         this.initIcon = EMPTY_ICON;
      
      Image scaleImage = this.initIcon.getImage().
                            getScaledInstance(size, size, 
                                              Image.SCALE_SMOOTH);
      
      ImageIcon newIcon = new ImageIcon(scaleImage);
      this.iconTable.put(size, newIcon);
      
      return newIcon;
   }
}
