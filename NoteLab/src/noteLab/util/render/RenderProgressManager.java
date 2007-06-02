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

package noteLab.util.render;

import java.util.Vector;

import noteLab.model.Page;
import noteLab.model.Stroke;
import noteLab.model.binder.Binder;
import noteLab.model.canvas.CompositeCanvas;
import noteLab.util.progress.ProgressEvent;
import noteLab.util.progress.ProgressListener;
import noteLab.util.progress.Progressive;

public class RenderProgressManager implements RenderListener, Progressive
{
   private int percent;
   
   private long curStroke;
   private int curPage;
   
   private int numPages;
   private long numTotal;
   
   boolean isComplete;
   
   private String desc;
   private String message;
   private Vector<ProgressListener> listenerVec;
   
   public RenderProgressManager(Renderer2D renderer, 
                                CompositeCanvas canvas, 
                                String desc)
   {
      if (renderer == null || canvas == null || desc == null)
         throw new NullPointerException();
      
      this.percent = 0;
      
      this.curStroke = 0;
      this.curPage = 0;
      
      this.isComplete = false;
      
      this.numPages = canvas.getBinder().getNumberOfPages();
      this.numTotal = this.numPages;
      
      this.desc = desc;
      this.message = "";
      
      this.listenerVec = new Vector<ProgressListener>();
      
      for (Page page : canvas.getBinder())
         this.numTotal += page.getNumStrokes();
      
      renderer.addRenderListener(this);
   }
   
   public void addProgressListener(ProgressListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.listenerVec.contains(listener))
         this.listenerVec.add(listener);
   }
   
   public void removeProgressListener(ProgressListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.listenerVec.remove(listener);
   }
   
   public void objectRendered(Renderable renderable)
   {
      if (renderable == null)
         throw new NullPointerException();
      
      if (renderable instanceof Page)
      {
         this.curPage++;
         this.message = "Saving page "+this.curPage+" of "+this.numPages;
      }
      else if (renderable instanceof Stroke)
      {
         this.curStroke++;
      }
      else if (renderable instanceof Binder)
         this.isComplete = true;
      
      this.percent = (int)(100*((float)(this.curPage+this.curStroke))/this.numTotal);
      
      for (ProgressListener listener : this.listenerVec)
         listener.progressOccured(new ProgressEvent(this.desc, 
                                                    this.message, 
                                                    null, /* Give an empty error message. */
                                                    false, 
                                                    this.percent, 
                                                    this.isComplete));
   }
}
