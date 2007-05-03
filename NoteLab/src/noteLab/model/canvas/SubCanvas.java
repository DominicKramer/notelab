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

package noteLab.model.canvas;

import java.util.Vector;

import noteLab.gui.ToolBarButton;
import noteLab.gui.listener.RepaintBroadcaster;
import noteLab.model.Path;
import noteLab.model.tool.Tool;
import noteLab.util.mod.ModBroadcaster;
import noteLab.util.mod.ModListener;
import noteLab.util.mod.ModType;
import noteLab.util.render.Renderable;

public abstract class SubCanvas<T extends Tool, S> 
                         extends RepaintBroadcaster 
                                    implements Renderable, 
                                               ModBroadcaster, 
                                               ModListener
{
   private CompositeCanvas canvas;
   private Vector<ModListener> modListenerVec;
   private boolean isPathInProgress;
   private boolean clipPoints;
   
   public SubCanvas(CompositeCanvas canvas, boolean clipPoints)
   {
      if (canvas == null)
         throw new NullPointerException();
      
      this.canvas = canvas;
      this.modListenerVec = new Vector<ModListener>();
      this.isPathInProgress = false;
      this.clipPoints = clipPoints;
   }
   
   protected void setClipPoints(boolean clipPoints)
   {
      this.clipPoints = clipPoints;
   }
   
   public boolean clipPoints()
   {
      return this.clipPoints;
   }
   
   public CompositeCanvas getCompositeCanvas()
   {
      return this.canvas;
   }
   
   /**
    * Used to add a listener that is notified when this canvas is 
    * modified.
    * 
    * @param listener The listener to add.
    */
   public void addModListener(ModListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.modListenerVec.contains(listener))
         this.modListenerVec.add(listener);
   }
   
   /**
    * Used to removed a listener from the list of listeners that are 
    * notified when this canvas is modified.
    * 
    * @param listener The listener to remove.
    */
   public void removeModListener(ModListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.modListenerVec.remove(listener);
   }
   
   /**
    * Invoked when a modification to this canvas has occured.
    * 
    * @param source The page that was modified.
    * @param type The type of modification.
    */
   public void modOccured(Object source, ModType type)
   {
      if (type == ModType.Other)
         notifyModListeners(type);
   }
   
   /**
    * Informs all of the ModListeners that this canvas has been modified.
    * 
    * @param type The type of modification.
    */
   private void notifyModListeners(ModType type)
   {
      for (ModListener listener : this.modListenerVec)
         listener.modOccured(this, type);
   }
   
   public boolean isPathInProgress()
   {
      return this.isPathInProgress;
   }
   
   public final void pathFinished(Path path)
   {
      this.isPathInProgress = false;
      pathFinishedImpl(path);
   }
   
   public final void pathChanged(Path path)
   {
      this.isPathInProgress = true;
      pathChangedImpl(path);
   }
   
   public final void pathStarted(Path path, boolean newPage)
   {
      this.isPathInProgress = true;
      pathStartedImpl(path, newPage);
   }
   
   public abstract void start();
   public abstract void finish();
   
   public abstract void pathFinishedImpl(Path path);
   public abstract void pathChangedImpl(Path path);
   public abstract void pathStartedImpl(Path path, boolean newPage);
   
   public abstract T getTool();
   public abstract ToolBarButton getToolBarButton();
   
   public abstract void zoomBy(float val);
   public abstract void zoomTo(float val);
   public abstract void resizeTo(float val);
}
