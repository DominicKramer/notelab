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

package noteLab.model.tool;

import java.awt.Cursor;

import noteLab.util.mod.ModListener;
import noteLab.util.render.Renderer2D;

/**
 * This class represents a tool that is used to select <code>Pages</code> 
 * from a <code>Binder</code>.
 * 
 * @author Dominic Kramer
 */
public class PageSelector implements Tool
{
   /** This tool's cursor. */
   private Cursor cursor;
   
   /**
    * Constructs this tool.
    */
   public PageSelector()
   {
      this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
   }
   
   /**
    * Used to get this tool's cursor.
    * 
    * @return This tool's cursor.
    */
   public Cursor getCursor()
   {
      return this.cursor;
   }
   
   /**
    * Used to get a deep copy of this tool.
    * 
    * @return A deep copy of this tool.
    */
   public Tool getCopy()
   {
      return new PageSelector();
   }
   
   /** This method does nothing. */
   public void adjustRenderer(Renderer2D display)
   {
   }
   
   /** This method does nothing. */
   public void scaleBy(float val)
   {
   }
   
   /** This method does nothing. */
   public void scaleTo(float val)
   {
   }
   
   public void resizeTo(float val)
   {
   }
   
   /**
    * Used to add a listener that is notified when this tool is 
    * modified.
    * 
    * @param listener The listener to add.
    */
   public void addModListener(ModListener listener)
   {
   }
   
   /**
    * Used to removed a listener from the list of listeners that are 
    * notified when this tool is modified.
    * 
    * @param listener The listener to remove.
    */
   public void removeModListener(ModListener listener)
   {
   }
}
