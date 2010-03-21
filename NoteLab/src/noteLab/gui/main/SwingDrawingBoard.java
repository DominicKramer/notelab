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

package noteLab.gui.main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import noteLab.model.Page;
import noteLab.model.binder.Binder;
import noteLab.model.binder.BinderListener;
import noteLab.model.canvas.CompositeCanvas;
import noteLab.model.canvas.StrokeCanvas;
import noteLab.util.mod.ModListener;
import noteLab.util.mod.ModType;
import noteLab.util.render.LoggedSwingRenderer2D;
import noteLab.util.render.QueuedRenderer2D;
import noteLab.util.render.SwingRenderer2D;
import noteLab.util.render.SwingRenderer2D.RenderMode;
import noteLab.util.settings.DebugSettings;

public class SwingDrawingBoard 
                extends JComponent 
                           implements ComponentListener, 
                                      ChangeListener, 
                                      ModListener, 
                                      BinderListener
{
   private static final int SCREEN_MAX_DIM;
   static
   {
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      SCREEN_MAX_DIM = Math.max(screenSize.width, screenSize.height);
   }
   
   private CompositeCanvas canvas;
   private MainPanel mainPanel;
   
   private LoggedSwingRenderer2D imageRenderer;
   private SwingRenderer2D screenRenderer;
   private QueuedRenderer2D queuedRenderer;
   
   private BufferedImage drawingboard;
   
   private boolean isImageValid;
   
   public SwingDrawingBoard(CompositeCanvas canvas, MainPanel mainPanel)
   {
      if (canvas == null || mainPanel == null)
         throw new NullPointerException();
      
      this.canvas = canvas;
      this.canvas.setDisplayPanel(this);
      this.mainPanel = mainPanel;
      this.imageRenderer = new LoggedSwingRenderer2D();
      this.screenRenderer = new SwingRenderer2D();
      this.queuedRenderer = new QueuedRenderer2D(this.screenRenderer);
      
      this.drawingboard = new BufferedImage(SCREEN_MAX_DIM, 
                                            SCREEN_MAX_DIM, 
                                            BufferedImage.TYPE_INT_RGB);
      
      this.isImageValid = false;
      
      // Add listeners
      this.canvas.addModListener(this);
      addComponentListener(this);
      this.mainPanel.getViewport().addChangeListener(this);
      this.canvas.getBinder().addBinderListener(this);
      
      //setting this to true means that this panel agrees to 
      //paint its entire area.  By setting this value to true, 
      //painting is completed more quickly by swing.  However, 
      //if the entire area of the panel is not painted by this 
      //class (as this class agrees to do), small painting 
      //anamolies may result.
      setOpaque(false);
      setDoubleBuffered(false);
      
      // clear the drawing board
      clear();
   }
   
   @Override
   public void paintComponent(Graphics g)
   {
      /* 
      if ( !(g instanceof Graphics2D) )
      {
         System.out.println("Warning:  The screen could not be painted " +
                            "because its canvas is not a Graphics2D " +
                            "object.");
         return;
      }
      */
      
      final boolean isScrolling = this.mainPanel.isScrolling() || 
                                     this.canvas.isBeingDragged();
      
      RenderMode mode = RenderMode.Appearance;
      if (isScrolling)
         mode = RenderMode.Performance;
      
      Graphics2D g2d = (Graphics2D)g;
      
      if (!isScrolling)
      {
         // Before the Graphics object is modified, configure the 
         // screenRenderer to use a copy of it.  This renderer will 
         // be used later
         this.screenRenderer.setSwingGraphics((Graphics2D)g2d.create(), mode);
         this.screenRenderer.setScrolling(isScrolling);
         
         // Get the current view rectangle
         Rectangle viewRect = this.mainPanel.getViewport().getViewRect();
         
         // Get the current clip
         Shape clip = g2d.getClip();
         
         // Configure the drawing board
         Graphics2D imageG2d = this.drawingboard.createGraphics();
         imageG2d.translate(-viewRect.x, -viewRect.y);
         imageG2d.setClip(clip);
         this.imageRenderer.setSwingGraphics(imageG2d, mode);
         this.imageRenderer.setScrolling(false);
         
         // Translate to the top left corner of the view rectangle
         g2d.translate(viewRect.x, viewRect.y);
         
         this.queuedRenderer.setRenderer(this.screenRenderer);
         
         // Render the canvas ignoring the overlay
         this.imageRenderer.resetModifiedFlag();
         this.canvas.renderInto(this.queuedRenderer, 
                                this.imageRenderer, 
                                this.isImageValid);
         
         // Paint the canvas on the screen
         if (!this.canvas.isProcessingPath() || 
                this.imageRenderer.hasBeenModified())
         {
            g2d.drawImage(this.drawingboard, null, 0, 0);
         }
         
         this.queuedRenderer.replay();
         
         // Since the image has just been rendered, it is now 
         // consistent with the current state of the canvas
         this.isImageValid = true;
         imageG2d.dispose();
      }
      else
      {
         // Configure the renderer for the screen
         this.screenRenderer.setSwingGraphics(g2d, mode);
         this.screenRenderer.setScrolling(isScrolling);
         this.canvas.renderInto(this.screenRenderer, 
                                this.screenRenderer, 
                                false);
      }
      
      if (DebugSettings.getSharedInstance().displayUpdateBox())
      {
         Rectangle clip = g.getClipBounds();
         if (clip != null)
         {
            g.setColor(Color.MAGENTA);
            g.drawRect(clip.x, clip.y, clip.width-1, clip.height-1);
         }
      }
   }
   
   private void clear()
   {
      clear(0, 0, SCREEN_MAX_DIM, SCREEN_MAX_DIM);
   }
   
   private void clear(int x, int y, int width, int height)
   {
      if (this.drawingboard == null)
         return;
      
      Graphics2D g2d = this.drawingboard.createGraphics();
      if (g2d == null)
         return;
      
      if (x < 0)
         x = 0;
      
      if (y < 0)
         y = 0;
      
      if (width > SCREEN_MAX_DIM)
         width = SCREEN_MAX_DIM;
      
      if (height > SCREEN_MAX_DIM)
         height = SCREEN_MAX_DIM;
      
      g2d.setBackground(getBackground());
      g2d.clearRect(x, y, width, height);
      g2d.finalize();
      
      this.isImageValid = false;
   }
   
   @Override
   public void repaint()
   {
      clear();
      super.repaint();
   }
   
   @Override
   public void repaint(long tm)
   {
      clear();
      super.repaint(tm);
   }
   
   @Override
   public void repaint(Rectangle r)
   {
      if (r == null)
         throw new NullPointerException();
      
      clear((int)r.getX(), (int)r.getY(), 
            (int)r.getWidth(), (int)r.getHeight());
      super.repaint(r);
   }
   
   @Override
   public void repaint(int x, int y, int width, int height)
   {
      clear(x, y, width, height);
      super.repaint(x, y, width, height);
   }
   
   @Override
   public void repaint(long tm, int x, int y, int width, int height)
   {
      clear(x, y, width, height);
      super.repaint(tm, x, y, width, height);
   }
   
   public void redrawOverlay(int x, int y, int width, int height)
   {
      Graphics g = getGraphics();
      g.setClip(x, y, width, height);
      
      paintComponent(g);
   }
   
   public void redrawOverlay()
   {
      paintComponent(getGraphics());
   }
   
   public void redraw(int x, int y, int width, int height)
   {
      repaint(x, y, width, height);
   }
   
   public void redraw()
   {
      repaint();
   }
   
   /*
    * Overridden so that multiple mouse dragged events are not coalesced into 
    * one.  If this were done, drawing would look choppy.  By disabling 
    * coalescing, drawing looks smooth.
    * 
    * @return <code>null</code> to indicate that no coalescing has taken place
    *
   @Override
   protected AWTEvent coalesceEvents(AWTEvent existingEvent, AWTEvent newEvent)
   {
      //Indicates no coalescing has done
      return null;
   }
   */

   public void componentHidden(ComponentEvent e)
   {
      this.isImageValid = false;
   }

   public void componentMoved(ComponentEvent e)
   {
   }

   public void componentResized(ComponentEvent e)
   {
      this.isImageValid = false;
   }

   public void componentShown(ComponentEvent e)
   {
   }
   
   public void stateChanged(ChangeEvent e)
   {
      this.isImageValid = false;
   }
   
   public void modOccured(Object source, ModType type)
   {
      if (source == null || type == null)
         throw new NullPointerException();
      
      // True if the modification was from the CompositeCanvas 
      // and wasn't a result of transforming the canvas
      boolean otherFromCompCanvas = 
                 (source instanceof CompositeCanvas) && 
                    (type.equals(ModType.Other));
      
      // True if the current SubCanvas is the StrokeCanvas
      boolean strokeCanvasCurrent = 
                 (this.canvas.getCurrentCanvas() instanceof StrokeCanvas);
      
      // If the current SubCanvas is a StrokeCanvas, and the 
      // modification wasn't caused by a transformation, ignore 
      // the change since the StrokeCanvas is only modifying the 
      // overlay.  Otherwise, the drawing board is invalid and needs 
      // to be re-rendered.
      if ( !(otherFromCompCanvas && strokeCanvasCurrent) )
      {
         updateBoardSize();
         this.isImageValid = false;
      }
   }
   
   private void updateBoardSize()
   {
      this.mainPanel.updatePreferredSize();
      revalidate();
   }
   
   @Override
   public void currentPageChanged(Binder source)
   {
   }
   
   @Override
   public void pageAdded(Binder source, Page page)
   {
      updateBoardSize();
   }
   
   @Override
   public void pageRemoved(Binder source, Page page)
   {
      updateBoardSize();
   }
}
