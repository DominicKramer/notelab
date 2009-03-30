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
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.Scrollable;

import noteLab.model.canvas.CompositeCanvas;
import noteLab.util.render.SwingRenderer2D;
import noteLab.util.render.SwingRenderer2D.RenderMode;

public class SwingDrawingBoard extends JComponent implements Scrollable
{
   private static final int SCROLL_STEP = 20;
   
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
   private SwingRenderer2D renderer;
   private BufferedImage drawingboard;
   
   public SwingDrawingBoard(CompositeCanvas canvas, MainPanel mainPanel)
   {
      if (canvas == null || mainPanel == null)
         throw new NullPointerException();
      
      this.canvas = canvas;
      this.canvas.setDisplayPanel(this);
      this.mainPanel = mainPanel;
      this.renderer = new SwingRenderer2D();
      
      
      this.drawingboard = new BufferedImage(SCREEN_WIDTH, 
                                            SCREEN_HEIGHT, 
                                            BufferedImage.TYPE_INT_ARGB);
      
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
         Graphics2D g2d = this.drawingboard.createGraphics();
         
         Rectangle viewRect = this.mainPanel.getViewport().getViewRect();
         g2d.translate(-viewRect.x, -viewRect.y);
         g2d.setClip(g.getClip());
         
         this.renderer.setSwingGraphics(g2d, mode);
         canvas.renderInto(this.renderer);
         g2d.finalize();
         
         g.translate(viewRect.x, viewRect.y);
         g.drawImage(this.drawingboard, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
      }
      else
      {
         this.renderer.setSwingGraphics((Graphics2D)g, mode);
         canvas.renderInto(this.renderer);
      }
      
      revalidate();
   }
   
   public void clear()
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
   
   public Dimension getPreferredScrollableViewportSize()
   {
      Rectangle2D bounds = canvas.getPreferredSize();
      return new Dimension(1+(int)bounds.getWidth(), 
                           1+(int)bounds.getHeight());
   }

   public int getScrollableBlockIncrement(Rectangle visibleRect, 
                                          int orientation, 
                                          int direction)
   {
      return SCROLL_STEP;
   }

   public boolean getScrollableTracksViewportHeight()
   {
      return false;
   }

   public boolean getScrollableTracksViewportWidth()
   {
      return false;
   }

   public int getScrollableUnitIncrement(Rectangle visibleRect, 
                                         int orientation, 
                                         int direction)
   {
      return SCROLL_STEP;
   }
   
   @Override
   public void repaint()
   {
      clear();
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
}
