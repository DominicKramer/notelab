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

import java.util.List;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.WindowConstants;

public class DynamicMenuBar extends JMenuBar
{
   public void addMenued(Menued menued)
   {
      if (menued == null)
         throw new NullPointerException();
      
      List<PathMenuItem> menuItems = menued.getPathMenuItems();
      if (menuItems == null)
         return;
      
      for (PathMenuItem item : menuItems)
         addMenuItem(item);
   }
   
   public void addMenuItem(PathMenuItem menuItem)
   {
      if (menuItem == null)
         throw new NullPointerException();
      
      MenuPath path = menuItem.getMenuPath();
      String[] pathArr = path.getFullPath();
      if (pathArr.length == 0)
         return;
      
      JMenu topMenu = getMenu(this, pathArr[0]);
      
      if (topMenu == null)
      {
         topMenu = new JMenu(pathArr[0]);
         topMenu.setName(pathArr[0]);
      }
      
      add(topMenu);
      JMenu bottomMenu = descend(pathArr, 1, topMenu);
      
      if (bottomMenu != null)
      {
         bottomMenu.add(menuItem.getMenuComponent());
         
         Icon icon = menuItem.getMenuPath().getIcon();
         if (icon != null)
            bottomMenu.setIcon(icon);
      }
   }
   
   private JMenu descend(String[] fullPath, int index, JMenu menu)
   {
      if (index >= fullPath.length)
         return menu;
      
      JMenuItem newItem = getMenu(menu, fullPath[index]);
      if (newItem == null)
      {
         newItem = new JMenu(fullPath[index]);
         newItem.setName(fullPath[index]);
      }
      
      if ( !(newItem instanceof JMenu) )
      {
         System.err.println("Warning:  DynamicMenuBar:  The path "+
                            pathToString(fullPath, fullPath.length-1)+
                            " could not be added to the menu bar because " +
                            "the item at the path "+
                            pathToString(fullPath, index)+
                            " is not a menu");
         return null;
      }
      
      menu.add(newItem);
      return descend(fullPath, index+1, (JMenu)newItem);
   }
   
   private static JMenu getMenu(JMenuBar menuBar, String name)
   {
      if (menuBar == null || name == null)
         throw new NullPointerException();
      
      int numMenus = menuBar.getMenuCount();
      JMenu menu;
      String tmpName;
      for (int i=0; i<numMenus; i++)
      {
         menu = menuBar.getMenu(i);
         tmpName = menu.getName();
         if ( tmpName != null && tmpName.equals(name) )
            return menu;
      }
      
      return null;
   }
   
   private static JMenuItem getMenu(JMenu menu, String name)
   {
      if (menu == null || name == null)
         throw new NullPointerException();
      
      int numItems = menu.getItemCount();
      JMenuItem item;
      String tmpName;
      for (int i=0; i<numItems; i++)
      {
         item = menu.getItem(i);
         tmpName = item.getName();
         if (tmpName != null && tmpName.equals(name))
            return item;
      }
      
      return null;
   }
   
   private static String pathToString(String[] pathArr, int endIndex)
   {
      StringBuffer buffer = new StringBuffer();
      endIndex = Math.min(pathArr.length-1, endIndex);
      for (int i=0; i<=endIndex-1; i++)
      {
         buffer.append(pathArr[i]);
         buffer.append(".");
      }
      buffer.append(pathArr[endIndex]);
      
      return buffer.toString();
   }
   
   public static void main(String[] args)
   {
      String itemName = "Item1";
      
      MenuPath path1 = new MenuPath("1");
      MenuPath path1_1 = new MenuPath(path1, "1.1");
      MenuPath path1_2 = new MenuPath(path1_1, "1.2");
      MenuPath path1_3 = new MenuPath(path1_2, "1.3");
      MenuPath path1_3_item = new MenuPath(path1_3, itemName);
      MenuPath path1_3_item2 = new MenuPath(path1_3_item, itemName+"2");
      
      PathMenuItem item1 = new PathMenuItem(new JMenuItem(itemName), 
                                            path1_3);
      
      PathMenuItem item2 = new PathMenuItem(new JMenuItem("Item2"), 
                                            path1_3_item2);
      
      DynamicMenuBar menuBar = new DynamicMenuBar();
      menuBar.addMenuItem(item1);
      menuBar.addMenuItem(item2);
      
      JFrame frame = new JFrame("DynamicMenuBar Demo");
      frame.setJMenuBar(menuBar);
      frame.setSize(500, 500);
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.setVisible(true);
   }
}
