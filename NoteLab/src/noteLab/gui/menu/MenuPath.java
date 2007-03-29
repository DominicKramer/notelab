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

package noteLab.gui.menu;

import javax.swing.Icon;

public class MenuPath
{
   private MenuPath parent;
   private String name;
   private Icon icon;
   
   public MenuPath(String name)
   {
      this(null, name, null);
   }
   
   public MenuPath(MenuPath parent, String name)
   {
      this(parent, name, null);
   }
   
   public MenuPath(MenuPath parent, String name, Icon icon)
   {
      if (name == null)
         throw new NullPointerException();
      
      this.parent = parent;
      this.name = name;
      this.icon = icon;
   }
   
   public MenuPath getParent()
   {
      return this.parent;
   }
   
   public String getName()
   {
      return this.name;
   }
   
   public Icon getIcon()
   {
      return this.icon;
   }
   
   public int getDepth()
   {
      int depth = 0;
      MenuPath tmpPath = this;
      while (tmpPath != null)
      {
         depth++;
         tmpPath = tmpPath.getParent();
      }
      
      return depth;
   }
   
   public String[] getFullPath()
   {
      int depth = getDepth();
      String[] path = new String[depth];
      int i = depth-1;
      MenuPath tmpPath = this;
      while (tmpPath != null)
      {
         path[i] = tmpPath.getName();
         i--;
         tmpPath = tmpPath.getParent();
      }
      
      return path;
   }
   
   public static void main(String[] args)
   {
      MenuPath path1 = new MenuPath("1");
      MenuPath path1_1 = new MenuPath(path1, "1.1");
      MenuPath path1_2 = new MenuPath(path1_1, "1.2");
      MenuPath path1_3 = new MenuPath(path1_2, "1.3");
      
      System.out.println("path1_3.getDepth() = "+path1_3.getDepth());
      System.out.println("path1_3.getFullPath():  ");
      String[] pathArr = path1_3.getFullPath();
      for (String name : pathArr)
         System.out.println("  "+name);
   }
}
