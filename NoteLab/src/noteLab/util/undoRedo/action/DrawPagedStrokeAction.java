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

package noteLab.util.undoRedo.action;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import noteLab.model.Page;
import noteLab.model.Stroke;
import noteLab.model.canvas.CompositeCanvas;

public class DrawPagedStrokeAction extends CanvasAction
{
   private Vector<DrawStrokeAction> drawStrokeActionVec;
   
   public DrawPagedStrokeAction(CompositeCanvas canvas, 
                                Hashtable<Page, Vector<Stroke>> strokes)
   {
      super(canvas);
      
      if (strokes == null)
         throw new NullPointerException();
      
      this.drawStrokeActionVec = new Vector<DrawStrokeAction>();
      
      Enumeration<Page> pages = strokes.keys();
      Page page;
      Vector<Stroke> strokeVec;
      while (pages.hasMoreElements())
      {
         page = pages.nextElement();
         if (page == null)
            continue;
         
         strokeVec = strokes.get(page);
         if (strokeVec == null)
            continue;
         
         this.drawStrokeActionVec.add(
               new DrawStrokeAction(canvas, strokeVec, page));
      }
   }
   
   public void run()
   {
      for (DrawStrokeAction action : this.drawStrokeActionVec)
         action.run();
   }

}
