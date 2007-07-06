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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;
import javax.swing.Scrollable;

import noteLab.model.canvas.CompositeCanvas;
import noteLab.util.render.SwingRenderer2D;
import noteLab.util.settings.DebugSettings;

public class SwingDrawingBoard extends JPanel implements Scrollable
{
   private static final boolean ANTIALIAS = true;
   private static final int SCROLL_STEP = 20;
   
   private CompositeCanvas canvas;
   private MainPanel mainPanel;
   private SwingRenderer2D renderer;
   
   public SwingDrawingBoard(CompositeCanvas canvas, MainPanel mainPanel)
   {
      if (canvas == null || mainPanel == null)
         throw new NullPointerException();
      
      this.canvas = canvas;
      this.canvas.setDisplayPanel(this);
      this.mainPanel = mainPanel;
      this.renderer = new SwingRenderer2D();
      
      //setting this to true means that this panel agrees to 
      //paint its entire area.  By setting this value to true, 
      //painting is completed more quickly by swing.  However, 
      //if the entire area of the panel is not painted by this 
      //class (as this class agrees to do), small painting 
      //anamolies may result.
      setOpaque(true);
      setDoubleBuffered(true);
   }
   
   public void paintComponent(Graphics g)
   {
      if ( !(g instanceof Graphics2D) )
      {
         System.out.println("Warning:  The screen could not be painted " +
                            "because its canvas is not a Graphics2D " +
                            "object.");
         return;
      }
      
      this.mainPanel.updatePreferredSize();
      
      Graphics2D g2d = (Graphics2D)g;
      super.paintComponent(g2d);
      this.renderer.setSwingGraphics(g2d, ANTIALIAS);
      canvas.renderInto(this.renderer);
      
      if (DebugSettings.getSharedInstance().displayUpdateBox())
      {
         g2d.setColor(Color.MAGENTA);
         g2d.setStroke(new BasicStroke(1));
         
         Rectangle clip = g2d.getClipBounds();
         g2d.drawRect(clip.x+1, clip.y+1, clip.width-3, clip.height-3);
      }
      
      revalidate();
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
   
   /*
    * Overriden so that multiple mouse dragged events are not coalesced into 
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
