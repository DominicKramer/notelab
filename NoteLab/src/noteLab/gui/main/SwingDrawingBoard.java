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

import java.awt.AWTEvent;
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

import noteLab.model.canvas.CompositeCanvas;
import noteLab.model.canvas.StrokeCanvas;
import noteLab.util.mod.ModListener;
import noteLab.util.mod.ModType;
import noteLab.util.render.EmptyRenderer2D;
import noteLab.util.render.SwingRenderer2D;
import noteLab.util.render.SwingRenderer2D.RenderMode;

public class SwingDrawingBoard 
                extends JComponent 
                           implements ComponentListener, 
                                      ChangeListener, 
                                      ModListener
{
   private static final EmptyRenderer2D EMPTY_RENDERER = new EmptyRenderer2D();
   
   private static final int SCREEN_WIDTH;
   private static final int SCREEN_HEIGHT;
   static
   {
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      SCREEN_WIDTH = screenSize.width;
      SCREEN_HEIGHT = screenSize.height;
   }
   
   private CompositeCanvas canvas;
   private MainPanel mainPanel;
   
   private SwingRenderer2D imageRenderer;
   private SwingRenderer2D screenRenderer;
   
   private BufferedImage drawingboard;
   
   private boolean isImageValid;
   
   public SwingDrawingBoard(CompositeCanvas canvas, MainPanel mainPanel)
   {
      if (canvas == null || mainPanel == null)
         throw new NullPointerException();
      
      this.canvas = canvas;
      this.canvas.setDisplayPanel(this);
      this.mainPanel = mainPanel;
      this.imageRenderer = new SwingRenderer2D();
      this.screenRenderer = new SwingRenderer2D();
      
      this.drawingboard = new BufferedImage(SCREEN_WIDTH, 
                                            SCREEN_HEIGHT, 
                                            BufferedImage.TYPE_INT_ARGB);
      
      this.isImageValid = false;
      
      // Add listeners
      this.canvas.addModListener(this);
      addComponentListener(this);
      this.mainPanel.getViewport().addChangeListener(this);
      
      //setting this to true means that this panel agrees to 
      //paint its entire area.  By setting this value to true, 
      //painting is completed more quickly by swing.  However, 
      //if the entire area of the panel is not painted by this 
      //class (as this class agrees to do), small painting 
      //anamolies may result.
      setOpaque(true);
      setDoubleBuffered(true);
      
      // clear the drawing board
      clear();
   }
   
   @Override
   public void paintComponent(Graphics g)
   {
      super.paintComponent(g);
      
      if ( !(g instanceof Graphics2D) )
      {
         System.out.println("Warning:  The screen could not be painted " +
                            "because its canvas is not a Graphics2D " +
                            "object.");
         return;
      }
      
      this.mainPanel.updatePreferredSize();
      
      final boolean isScrolling = this.mainPanel.isScrolling();
      
      RenderMode mode = RenderMode.Appearance;
      if (isScrolling)
         mode = RenderMode.Performance;
      
      if (!isScrolling)
      {
         // Before the Graphics object is modified, configure the 
         // screenRenderer to use a copy of it.  This renderer will 
         // be used later
         this.screenRenderer.setSwingGraphics((Graphics2D)g.create(), mode);
         this.screenRenderer.setScrolling(isScrolling);
         
         // Get the current view rectangle
         Rectangle viewRect = this.mainPanel.getViewport().getViewRect();
         
         // Get the current clip
         Shape clip = g.getClip();
         
         // Configure the drawing board
         Graphics2D imageG2d = this.drawingboard.createGraphics();
         imageG2d.translate(-viewRect.x, -viewRect.y);
         imageG2d.setClip(clip);
         this.imageRenderer.setSwingGraphics(imageG2d, mode);
         this.imageRenderer.setScrolling(false);
         
         // Render the canvas ignoring the overlay
         this.canvas.renderInto(EMPTY_RENDERER, 
                                this.imageRenderer, 
                                this.isImageValid);
         // Since the image has just been rendered, it is now 
         // consistent with the current state of the canvas
         this.isImageValid = true;
         imageG2d.finalize();
         
         // Translate to the top left corner of the view rectangle
         g.translate(viewRect.x, viewRect.y);
         
         // Paint the canvas on the screen
         g.drawImage(this.drawingboard, 0, 0, 
                     SCREEN_WIDTH, SCREEN_HEIGHT, null);
         
         // Now render the overlay using the current screen's renderer 
         // (which was configured above) ignoring the main canvas
         this.canvas.renderInto(this.screenRenderer, 
                                EMPTY_RENDERER, 
                                true);
      }
      else
      {
         // Configure the renderer for the screen
         this.screenRenderer.setSwingGraphics((Graphics2D)g, mode);
         this.screenRenderer.setScrolling(isScrolling);
         this.canvas.renderInto(this.screenRenderer, 
                                this.screenRenderer, 
                                false);
      }
      
      revalidate();
   }
   
   private void clear()
   {
      if (this.drawingboard == null)
         return;
      
      Graphics2D g2d = this.drawingboard.createGraphics();
      if (g2d == null)
         return;
      
      g2d.setBackground(getBackground());
      g2d.clearRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
      g2d.finalize();
   }
   
   @Override
   public void repaint()
   {
      clear();
      this.isImageValid = false;
      super.repaint();
   }
   
   /**
    * Overridden so that multiple mouse dragged events are not coalesced into 
    * one.  If this were done, drawing would look choppy.  By disabling 
    * coalescing, drawing looks smooth.
    * 
    * @return <code>null</code> to indicate that no coalescing has taken place
    */
   @Override
   protected AWTEvent coalesceEvents(AWTEvent existingEvent, AWTEvent newEvent)
   {
      //Indicates no coalescing has done
      return null;
   }

   public void componentHidden(ComponentEvent e)
   {
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
         this.isImageValid = false;
   }
}
