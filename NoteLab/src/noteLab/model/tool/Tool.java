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

import noteLab.util.CopyReady;
import noteLab.util.mod.ModBroadcaster;
import noteLab.util.render.Renderer2D;

/**
 * A class that implements this interface serves as a tool that the user 
 * can use when drawing on a <code>Page</code>.  Specifically, a 
 * <code>Tool</code> has a special cursor and implements the ability to 
 * adjust a renderer so that the curves drawn on the <code>Page</code> look 
 * like the <code>Tool</code> made them.
 * 
 * @author Dominic Kramer
 */
public interface Tool extends CopyReady<Tool>, ModBroadcaster
{
   /**
    * Used to get this tool's cursor.
    * 
    * @return This tool's cursor.
    */
   public Cursor getCursor();
   
   /**
    * Used to adjust the given renderer so that when curves are drawn on 
    * a <code>Page</code>, they look like this tool has drawn them.
    * 
    * @param display The renderer that renders the display.
    */
   public void adjustRenderer(Renderer2D display);
   
   /**
    * Instructs this tool that it has been scaled by the amount given.
    * 
    * @param val The amount this tool has been scaled by.
    */
   public void scaleBy(float val);
   
   /**
    * Instructs this tool that it has been scaled to the amount given.
    * 
    * @param val The amount this tool has been scaled to.
    */
   public void scaleTo(float val);
   
   public void resizeTo(float val);
}
