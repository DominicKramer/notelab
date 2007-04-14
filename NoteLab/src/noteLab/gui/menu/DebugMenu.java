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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;

import noteLab.model.canvas.CompositeCanvas;
import noteLab.util.settings.DebugSettings;

public class DebugMenu implements Menued, MenuConstants, ActionListener
{
   private static final String BOUNDING_BOX_NAME = "Display Bounding Box";
   private static final String UPDATE_BOX_NAME = "Display Update Box";
   private static final String DISABLE_PAPER_NAME = "Disable Paper";
   private static final String NOTIFY_OF_REPAINTS_NAME = "Notify Of Repaints";
   private static final String USE_CACHE_NAME = "Use Cache";
   private static final String FORCE_GLOBAL_REPAINTS_NAME = "Force Global Repaints";
   private static final String DISPLAY_KNOTS_NAME = "Display Knots";
   
   private JCheckBoxMenuItem boundingBoxItem;
   private JCheckBoxMenuItem updateBoxItem;
   private JCheckBoxMenuItem disablePaperItem;
   private JCheckBoxMenuItem notifyRepaintsItem;
   private JCheckBoxMenuItem useCacheItem;
   private JCheckBoxMenuItem forceGlobalRepaintsItem;
   private JCheckBoxMenuItem knotsItem;
   
   private Vector<PathMenuItem> menuItemVec;
   
   private CompositeCanvas canvas;
   
   public DebugMenu(CompositeCanvas canvas)
   {
      if (canvas == null)
         throw new NullPointerException();
      
      this.canvas = canvas;
      
      this.boundingBoxItem = new JCheckBoxMenuItem(BOUNDING_BOX_NAME);
      this.boundingBoxItem.addActionListener(this);
      
      this.updateBoxItem = new JCheckBoxMenuItem(UPDATE_BOX_NAME);
      this.updateBoxItem.addActionListener(this);
      
      this.disablePaperItem = new JCheckBoxMenuItem(DISABLE_PAPER_NAME);
      this.disablePaperItem.addActionListener(this);
      
      this.notifyRepaintsItem = new JCheckBoxMenuItem(NOTIFY_OF_REPAINTS_NAME);
      this.notifyRepaintsItem.addActionListener(this);
      
      this.useCacheItem = new JCheckBoxMenuItem(USE_CACHE_NAME);
      this.useCacheItem.addActionListener(this);
      
      this.forceGlobalRepaintsItem = new JCheckBoxMenuItem(FORCE_GLOBAL_REPAINTS_NAME);
      this.forceGlobalRepaintsItem.addActionListener(this);
      
      this.knotsItem = new JCheckBoxMenuItem(DISPLAY_KNOTS_NAME);
      this.knotsItem.addActionListener(this);
      
      this.menuItemVec = new Vector<PathMenuItem>();
      this.menuItemVec.add(new PathMenuItem(this.boundingBoxItem, DEBUG_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(this.updateBoxItem, DEBUG_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(this.disablePaperItem, DEBUG_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(this.notifyRepaintsItem, DEBUG_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(this.useCacheItem, DEBUG_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(this.forceGlobalRepaintsItem, DEBUG_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(this.knotsItem, DEBUG_MENU_PATH));
      
      syncDisplay();
   }
   
   private void syncDisplay()
   {
      DebugSettings settings = DebugSettings.getSharedInstance();
      boolean boundingBox    = settings.displayBoundingBox();
      boolean updateBox      = settings.displayUpdateBox();
      boolean disablePaper   = settings.disablePaper();
      boolean notifyRepaints = settings.notifyOfRepaints();
      boolean useCache       = settings.useCache();
      boolean forceRepaints  = settings.forceGlobalRepaints();
      boolean useKnots       = settings.displayKnots();
      
      this.boundingBoxItem.setSelected(boundingBox);
      this.updateBoxItem.setSelected(updateBox);
      this.disablePaperItem.setSelected(disablePaper);
      this.notifyRepaintsItem.setSelected(notifyRepaints);
      this.useCacheItem.setSelected(useCache);
      this.forceGlobalRepaintsItem.setSelected(forceRepaints);
      this.knotsItem.setSelected(useKnots);
   }
   
   public List<PathMenuItem> getPathMenuItems()
   {
      return this.menuItemVec;
   }

   public void actionPerformed(ActionEvent e)
   {
      DebugSettings settings = DebugSettings.getSharedInstance();
      
      String cmmd = e.getActionCommand();
      if (cmmd.equals(BOUNDING_BOX_NAME))
         settings.flipDisplayBoundingBox();
      else if (cmmd.equals(UPDATE_BOX_NAME))
         settings.flipDisplayUpdateBox();
      else if (cmmd.equals(DISABLE_PAPER_NAME))
         settings.flipDisablePaper();
      else if (cmmd.equals(NOTIFY_OF_REPAINTS_NAME))
         settings.flipNotifyOfRepaints();
      else if (cmmd.equals(USE_CACHE_NAME))
         settings.flipUseCache();
      else if (cmmd.equals(FORCE_GLOBAL_REPAINTS_NAME))
         settings.flipForceGlobalRepaints();
      else if (cmmd.equals(DISPLAY_KNOTS_NAME))
         settings.flipDisplayKnots();
      
      canvas.doRepaint();
   }
}
