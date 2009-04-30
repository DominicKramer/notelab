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

import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import noteLab.gui.ToolBarButton;
import noteLab.gui.listener.RepaintBroadcaster;
import noteLab.model.Page;
import noteLab.model.Path;
import noteLab.model.Stroke;
import noteLab.model.binder.Binder;
import noteLab.model.tool.Tool;
import noteLab.util.geom.RectangleUnioner;
import noteLab.util.mod.ModBroadcaster;
import noteLab.util.mod.ModListener;
import noteLab.util.mod.ModType;
import noteLab.util.render.DualRenderable;
import noteLab.util.undoRedo.action.DeletePagedStrokeAction;
import noteLab.util.undoRedo.action.DrawPagedStrokeAction;

public abstract class SubCanvas<T extends Tool, S> 
                         extends RepaintBroadcaster 
                                    implements DualRenderable, 
                                               ModBroadcaster, 
                                               ModListener
{
   public enum MouseButton
   {
      Button1(MouseEvent.BUTTON1), 
      Button2(MouseEvent.BUTTON2), 
      Button3(MouseEvent.BUTTON3),
      Unknown(null);
      
      private Integer javaId;
      private MouseButton(Integer javaId)
      {
         this.javaId = javaId;
      }
      
      public static MouseButton getMouseButton(int javaId)
      {
         for (MouseButton button : values())
            if (button.javaId != null && button.javaId == javaId)
               return button;
         
         return Unknown;
      }
   }
   
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
   
   public final void pathFinished(Path path, MouseButton button)
   {
      this.isPathInProgress = false;
      pathFinishedImpl(path, button);
   }
   
   public final void pathChanged(Path path, MouseButton button)
   {
      this.isPathInProgress = true;
      pathChangedImpl(path, button);
   }
   
   public final void pathStarted(Path path, MouseButton button, 
                                 boolean newPage)
   {
      this.isPathInProgress = true;
      pathStartedImpl(path, button, newPage);
   }
   
   public abstract void start();
   public abstract void finish();
   
   public abstract boolean getRenderBinder();
   
   public abstract void pathFinishedImpl(Path path, MouseButton button);
   public abstract void pathChangedImpl(Path path, MouseButton button);
   public abstract void pathStartedImpl(Path path, MouseButton button, 
                                        boolean newPage);
   
   public abstract T getTool();
   public abstract ToolBarButton getToolBarButton();
   
   public abstract void zoomBy(float val);
   public abstract void zoomTo(float val);
   public abstract void resizeTo(float val);
   
   //-------------=[ Methods shared between various subclasses ]=----------//
   protected Hashtable<Page, Vector<Stroke>> getSelectedStrokeTable()
   {
      Hashtable<Page, Vector<Stroke>> deletedStrokeTable = 
         new Hashtable<Page, Vector<Stroke>>();

      CompositeCanvas canvas = getCompositeCanvas();
      
      for (Page page : canvas.getBinder())
         deletedStrokeTable.put(page, page.getSelectedStrokesCopy());
      
      return deletedStrokeTable;
   }
   
   protected void deleteStrokes(Hashtable<Page, Vector<Stroke>> strokeTable)
   {
      // the region of the session that needs to be repainted 
      // because the strokes in that region have been deleted
      RectangleUnioner dirtyUnioner = new RectangleUnioner();
      
      Enumeration<Page> pages = strokeTable.keys();
      Page curPage;
      Vector<Stroke> strokeVec;
      Stroke selStroke;
      while (pages.hasMoreElements())
      {
         curPage = pages.nextElement();
         strokeVec = strokeTable.get(curPage);
         for (int i=strokeVec.size()-1; i>=0; i--)
         {
            selStroke = strokeVec.elementAt(i);
            curPage.removeStroke(selStroke);
            
            // add the stroke's bounding box to the dirty region
            dirtyUnioner.union(selStroke.getBounds2D());
         }
      }
      
      DeletePagedStrokeAction actionDone = 
         new DeletePagedStrokeAction(canvas, strokeTable);
      DrawPagedStrokeAction undoAction = 
         new DrawPagedStrokeAction(canvas, strokeTable);
      canvas.getUndoRedoManager().actionDone(actionDone, undoAction);
      
      // repaint only the dirty region
      Rectangle2D dirtyRect = dirtyUnioner.getUnion();
      
      // The dirty region is the region relative to the current page.
      // Thus we have to shift the region as if the origin is at 
      // the top left corner of the current page
      Binder binder = getCompositeCanvas().getBinder();
      Page page = binder.getCurrentPage();
      
      float x = (float)(dirtyRect.getX()+page.getX());
      float y = (float)(dirtyRect.getY()+page.getY());
      
      // repaint the dirty region
      doRepaint( x, 
                 y, 
                (float)dirtyRect.getWidth(), 
                (float)dirtyRect.getHeight(), 0);
   }
}
