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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.ToolBarButton;
import noteLab.gui.listener.RepaintBroadcastCenter;
import noteLab.gui.listener.RepaintBroadcaster;
import noteLab.gui.listener.RepaintListener;
import noteLab.model.Page;
import noteLab.model.Path;
import noteLab.model.Paper.PaperType;
import noteLab.model.binder.Binder;
import noteLab.model.binder.FlowBinder;
import noteLab.model.canvas.SubCanvas.MouseButton;
import noteLab.model.geom.FloatPoint2D;
import noteLab.model.tool.Tool;
import noteLab.util.CopyReady;
import noteLab.util.UnitScaleDependent;
import noteLab.util.geom.Bounded;
import noteLab.util.mod.ModBroadcaster;
import noteLab.util.mod.ModListener;
import noteLab.util.mod.ModType;
import noteLab.util.render.Renderer2D;
import noteLab.util.settings.SettingsChangedEvent;
import noteLab.util.settings.SettingsChangedListener;
import noteLab.util.settings.SettingsKeys;
import noteLab.util.settings.SettingsManager;
import noteLab.util.settings.SettingsUtilities;
import noteLab.util.undoRedo.UndoRedoListener;
import noteLab.util.undoRedo.UndoRedoManager;

public class CompositeCanvas 
                extends RepaintBroadcaster 
                           implements RepaintListener, 
                                      RepaintBroadcastCenter, 
                                      ModBroadcaster, 
                                      ModListener, 
                                      Bounded, 
                                      CopyReady<CompositeCanvas>, 
                                      UndoRedoListener, 
                                      SettingsChangedListener, 
                                      UnitScaleDependent
{
   private enum EventType
   {
      MousePressed, 
      MouseReleased, 
      MouseDragged
   }
   
   private Binder binder;
   private SubCanvas curCanvas;
   
   private File file;
   
   private StrokeCanvas printCanvas;
   private StrokeSelectionCanvas selCanvas;
   private PageSelectionCanvas pageSelCanvas;
   
   private float scaleLevel;
   
   private MouseInputListener mouseListener;
   
   private boolean hasBeenModified;
   private Vector<ModListener> modListenerVec;
   
   private UndoRedoManager undoRedoManager;
   
   private boolean isEnabled;
   
   private JComponent displayPanel;
   
   private Queue<StylusEvent> pointQueue;
   
   public CompositeCanvas(float scaleLevel)
   {
      this(new FlowBinder(scaleLevel, scaleLevel), scaleLevel);
   }
   
   public CompositeCanvas(Binder binder, float scaleLevel)
   {
      if (binder == null)
         throw new NullPointerException();
      
      this.pointQueue = new LinkedList<StylusEvent>();
      
      this.displayPanel = null;
      
      this.modListenerVec = new Vector<ModListener>();
      this.undoRedoManager = new UndoRedoManager(this);
      
      this.file = null;
      
      this.scaleLevel = scaleLevel;
      
      this.binder = binder;
      this.binder.addRepaintListener(this);
      this.binder.addModListener(this);
      
      //make the print canvas
      this.printCanvas = new StrokeCanvas(this);
      this.printCanvas.addRepaintListener(this);
      this.printCanvas.addModListener(this);
      
      setCurrentCanvas(this.printCanvas);
      
      //make the selection canvas
      this.selCanvas = new StrokeSelectionCanvas(this);
      this.selCanvas.addRepaintListener(this);
      this.selCanvas.addModListener(this);
      
      this.pageSelCanvas = new PageSelectionCanvas(this);
      this.pageSelCanvas.addRepaintListener(this);
      this.pageSelCanvas.addModListener(this);
      
      new Thread()
      {
         public void run()
         {
            mouseListener = new CanvasMouseListener();
         }
      }.start();
      
      new PointProcessor().start();
      
      this.hasBeenModified = false;
      this.isEnabled = true;
      
      SettingsManager.getSharedInstance().addSettingsListener(this);
   }
   
   public StrokeSelectionCanvas getStrokeSelectionCanvas()
   {
      return this.selCanvas;
   }
   
   public PageSelectionCanvas getPageCanvas()
   {
      return this.pageSelCanvas;
   }
   
   public StrokeCanvas getStrokeCanvas()
   {
      return this.printCanvas;
   }
   
   public JComponent getDisplayPanel()
   {
      return this.displayPanel;
   }
   
   public void setDisplayPanel(JComponent displayPanel)
   {
      this.displayPanel = displayPanel;
   }
   
   public boolean isEnabled()
   {
      return this.isEnabled;
   }
   
   public void setEnabled(boolean isEnabled)
   {
      this.isEnabled = isEnabled;
   }
   
   public UndoRedoManager getUndoRedoManager()
   {
      return this.undoRedoManager;
   }
   
   public File getFile()
   {
      return this.file;
   }
   
   public void setFile(File file)
   {
      if (file == null)
         throw new NullPointerException();
      
      this.file = file;
   }
   
   public boolean hasBeenModified()
   {
      return this.hasBeenModified;
   }
   
   public void setHasBeenModified(boolean hasBeenModified)
   {
      this.hasBeenModified = hasBeenModified;
   }
   
   public Vector<ToolBarButton> getToolBars()
   {
      Vector<ToolBarButton> toolbars = new Vector<ToolBarButton>(3);
        toolbars.add(this.printCanvas.getToolBarButton());
        toolbars.add(this.selCanvas.getToolBarButton());
        toolbars.add(this.pageSelCanvas.getToolBarButton());
      return toolbars;
   }
   
   public MouseInputListener getInputListener()
   {
      return this.mouseListener;
   }
   
   public Binder getBinder()
   {
      return this.binder;
   }
   
   public Rectangle2D getPreferredSize()
   {
      return new Rectangle2D.Float(getX(), getY(), getWidth(), getHeight());
   }
   
   public float getX()
   {
      return this.binder.getX();
   }
   
   public float getY()
   {
      return this.binder.getY();
   }
   
   public float getWidth()
   {
      return this.binder.getWidth();
   }
   
   public float getHeight()
   {
      return this.binder.getHeight();
   }
   
   public void zoomTo(float zoomFactor)
   {
      this.scaleLevel = zoomFactor;
      
      this.binder.scaleTo(zoomFactor, zoomFactor);
      
      this.printCanvas.zoomTo(zoomFactor);
      this.selCanvas.zoomTo(zoomFactor);
      this.pageSelCanvas.zoomTo(zoomFactor);
   }
   
   public void resizeTo(float zoomFactor)
   {
      this.binder.resizeTo(zoomFactor, zoomFactor);
      
      this.printCanvas.resizeTo(zoomFactor);
      this.selCanvas.resizeTo(zoomFactor);
      this.pageSelCanvas.resizeTo(zoomFactor);
   }
   
   public void zoomBy(float val)
   {
      this.binder.scaleBy(val, val);
      
      this.printCanvas.zoomBy(val);
      this.selCanvas.zoomBy(val);
      this.pageSelCanvas.zoomBy(val);
      
      this.scaleLevel *= val;
   }
   
   public float getZoomLevel()
   {
      return this.scaleLevel;
   }
   
   public float getUnitScaleFactor()
   {
      return this.binder.getUnitScaleFactor();
   }
   
   public void setUnitScaleFactor(float unitScaleFactor)
   {
      this.binder.setUnitScaleFactor(unitScaleFactor);
   }
   
   public Rectangle2D.Float getBounds2D()
   {
      Rectangle2D.Float bounds = this.binder.getBounds2D();
      float x = (float)(this.scaleLevel*bounds.getX());
      float y = (float)(this.scaleLevel*bounds.getY());
      float width  = (float)(this.scaleLevel*bounds.getWidth());
      float height = (float)(this.scaleLevel*bounds.getHeight());
      
      return new Rectangle2D.Float(x, y, width, height);
   }
   
   public void renderInto(Renderer2D overlayDisplay, 
                          Renderer2D mainDisplay, 
                          boolean isValid)
   {
      if (overlayDisplay == null || mainDisplay == null)
         throw new NullPointerException();
      
      SubCanvas subCanvas = getCurrentCanvas();
      
      // render the binder if the SubCanvas wants it rendered
      if (!isValid || subCanvas.getRenderBinder())
         this.binder.renderInto(mainDisplay);
      
      // render the current canvas
      subCanvas.renderInto(overlayDisplay, mainDisplay);
      
      // tell the renderers that rendering is complete
      overlayDisplay.finish();
      mainDisplay.finish();
   }
   
   public void setCurrentCanvas(SubCanvas canvas)
   {
      if (canvas == null)
         throw new NullPointerException();
      
      if (this.curCanvas != null)
         this.curCanvas.finish();
      
      this.curCanvas = canvas;
      this.curCanvas.start();
      
      this.curCanvas.getToolBarButton().doClick();
      
      syncCursor();
   }
   
   public void syncCursor()
   {
      Tool tool = this.curCanvas.getTool();
      if (tool != null)
      {
         Cursor cursor = tool.getCursor();
         if (cursor == null)
            cursor = Cursor.getDefaultCursor();
         
         setCursor(cursor);
      }
   }
   
   public SubCanvas getCurrentCanvas()
   {
      return this.curCanvas;
   }
   
   public void setCursor(Cursor cursor)
   {
      for (RepaintListener listener : this)
         listener.setCursor(cursor);
   }
   
   public void repaint()
   {
      for (RepaintListener listener : this)
         listener.repaint();
   }

   public void repaint(float x, float y, float width, float height)
   {
      for (RepaintListener listener : this)
         listener.repaint(x, y, width, height);
   }
   
   public void show(float x, float y, float width, float height)
   {
      for (RepaintListener listener : this)
         listener.show(x, y, width, height);
   }
   
   public CompositeCanvas getCopy()
   {
      return new CompositeCanvas(this.binder.getCopy(), this.scaleLevel);
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
      notifyModListeners(type);
      this.hasBeenModified = true;
   }
   
   /**
    * Informs all of the ModListeners that this binder has been modified.
    * 
    * @param type The type of modification.
    */
   private void notifyModListeners(ModType type)
   {
      for (ModListener listener : this.modListenerVec)
         listener.modOccured(this, type);
   }
   
   private class StylusEvent
   {
      public EventType type;
      public MouseEvent event;
      public MouseButton button;
      
      public StylusEvent(EventType type, MouseEvent event, MouseButton button)
      {
         this.type = type;
         this.event = event;
         this.button = button;
      }
   }
   
   private class CanvasMouseListener implements MouseInputListener
   {
      private MouseButton curMouseButton;
      
      private CanvasMouseListener()
      {
         this.curMouseButton = MouseButton.Unknown;
      }
      
      public void mouseClicked(MouseEvent e)
      {
      }
      
      public void mousePressed(final MouseEvent e)
      {
         if (!isEnabled)
            return;
         
         this.curMouseButton = MouseButton.getMouseButton(e.getButton());
         
         synchronized(pointQueue)
         {
            pointQueue.add(new StylusEvent(EventType.MousePressed, 
                                           e, 
                                           this.curMouseButton));
            pointQueue.notify();
         }
      }

      public void mouseReleased(final MouseEvent e)
      {
         if (!isEnabled)
            return;
         
         synchronized(pointQueue)
         {
            pointQueue.add(new StylusEvent(EventType.MouseReleased, 
                                           e, 
                                           this.curMouseButton));
            pointQueue.notify();
         }
      }

      public void mouseEntered(MouseEvent e)
      {
      }

      public void mouseExited(MouseEvent e)
      {
      }
      
      public void mouseDragged(final MouseEvent e)
      {
         if (!isEnabled)
            return;
         
         synchronized(pointQueue)
         {
            pointQueue.add(new StylusEvent(EventType.MouseDragged, 
                                           e, 
                                           this.curMouseButton));
            pointQueue.notify();
         }
      }

      public void mouseMoved(MouseEvent e)
      {
      }
   }
   
   private class PointProcessor extends Thread
   {
      private Path curPath;
      
      public PointProcessor()
      {
         this.curPath = null;
      }
      
      public void run()
      {
         StylusEvent event;
         while (true)
         {
            synchronized (pointQueue)
            {
               while ( (event = pointQueue.poll()) == null)
               {
                  try
                  {
                     pointQueue.wait();
                  }
                  catch (InterruptedException e)
                  {
                     continue;
                  }
               }
            }
            
            if (event.type == EventType.MouseDragged)
               processMouseDragged(event.event, event.button);
            else if (event.type == EventType.MousePressed)
               processMousePressed(event.event, event.button);
            else if (event.type == EventType.MouseReleased)
               processMouseReleased(event.event, event.button);
         }
      }
      
      // returns the 'real' point relative to the current page
      // that is the point is not what is displayed on the 
      // screen which may be different depending on the zoom factor.  
      // instead this method returns the point on the page as if 
      // the page has not been zoomed
      private FloatPoint2D getClippedPoint(Point point)
      {
         if (point == null)
            throw new NullPointerException();
         
         Page curPage = binder.getCurrentPage();
         FloatPoint2D newPt = null;
         if (getCurrentCanvas().clipPoints())
            newPt = binder.clipPoint(point.x-curPage.getX(), 
                                     point.y-curPage.getY());
         else
            newPt = new FloatPoint2D(point, 
                                     getZoomLevel(), 
                                     getZoomLevel());
         
         return newPt;
      }
      
      private void processMouseDragged(final MouseEvent e, 
                                       final MouseButton button)
      {
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               if (curPath == null)
                  return;
               
               FloatPoint2D point = getClippedPoint(e.getPoint());
               curPath.addItem(point);
               getCurrentCanvas().pathChanged(curPath, button);
            }
         });
      }
      
      private void processMouseReleased(final MouseEvent e, 
                                        final MouseButton button)
      {
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               if (curPath == null)
                  return;
               
               curPath.addItem(getClippedPoint(e.getPoint()));
               getCurrentCanvas().pathFinished(curPath, button);
               curPath = null;
            }
         });
      }
      
      private void processMousePressed(final MouseEvent e, 
                                       final MouseButton button)
      {
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               Point point = e.getPoint();
               
               boolean newCur = binder.setCurrentPage(
                                          new FloatPoint2D(point.x, point.y, 
                                                           scaleLevel, 
                                                           scaleLevel));
               
               boolean firstTime = (curPath == null);
               curPath = new Path(scaleLevel, scaleLevel);
               curPath.addItem(getClippedPoint(point));
               SubCanvas subcanvas = getCurrentCanvas();
               if (firstTime)
                  subcanvas.pathStarted(curPath, button, newCur);
               
               subcanvas.pathChanged(curPath, button);
            }
         });
      }
   }

   public void undoRedoStackChanged(UndoRedoManager manager)
   {
   }

   public void undoRedoStackWarning(UndoRedoManager manager, 
                                    String message)
   {
      if (message == null)
         message = "Unknown";
      
      int size = GuiSettingsConstants.BUTTON_SIZE;
      ImageIcon icon = DefinedIcon.dialog_warning.getIcon(size);
      
      String title = "Warning";
      
      JOptionPane.showMessageDialog(new JFrame(), 
                                    message, 
                                    title, 
                                    JOptionPane.WARNING_MESSAGE, 
                                    icon);
   }
   
   public void settingsChanged(SettingsChangedEvent event)
   {
      if (event == null)
         throw new NullPointerException();

      String key = event.getKey();
      if (key == null)
         return;
      
      if (key.equals(SettingsKeys.UNIT_SCALE_FACTOR))
      {
         Object newOb = event.getNewValue();
         Object oldOb = event.getOldValue();
         if (newOb != null && oldOb != null && 
             newOb instanceof Number && oldOb instanceof Number)
         {
            float newFactor = ((Number)newOb).floatValue();
            float oldFactor = ((Number)oldOb).floatValue();
            
            resizeTo(newFactor/oldFactor);
            
            setUnitScaleFactor(newFactor);
            
            // NOTE:  The entire canvas needs to be repainted here
            doRepaint();
         }
      }
      else if (key.equals(SettingsKeys.PAPER_TYPE_KEY))
      {
         PaperType type = SettingsUtilities.getPaperType();
         for (Page page : this.binder)
            page.getPaper().setPaperType(type);
         
         // NOTE:  The entire canvas needs to be repainted here
         doRepaint();
      }
      else if (key.equals(SettingsKeys.PAPER_COLOR_KEY))
      {
         Color color = SettingsUtilities.getPaperColor();
         for (Page page : this.binder)
            page.getPaper().setBackgroundColor(color);
         
         // NOTE:  The entire canvas needs to be repainted here
         doRepaint();
      }
   }
}
