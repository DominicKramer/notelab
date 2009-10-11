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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.MouseInputListener;

import noteLab.gui.listener.RepaintListener;
import noteLab.model.Page;
import noteLab.model.canvas.CompositeCanvas;
import noteLab.util.settings.DebugSettings;

public class MainPanel 
                extends JPanel implements 
                                  RepaintListener, 
                                  AdjustmentListener, 
                                  MouseWheelListener
{
   private static final int WHEEL_SCROLL_INCREMENT = 200;
   private static final int BUTTON_SCROLL_INCREMENT = 75;
   
   private CompositeCanvas canvas;
   private JComponent paintPanel;
   private JScrollPane scrollPane;
   
   public MainPanel(CompositeCanvas canvas)
   {
      if (canvas == null)
         throw new NullPointerException();
      
      this.canvas = canvas;
      this.canvas.addRepaintListener(this);
      this.canvas.syncCursor();
      
      FlowLayout centerLayout = new FlowLayout(FlowLayout.CENTER, 0, 0);
      JPanel centerPanel = new JPanel(centerLayout);
      
      this.scrollPane = new JScrollPane(centerPanel);
      this.scrollPane.setWheelScrollingEnabled(false);
      this.scrollPane.addMouseWheelListener(this);
      this.scrollPane.getVerticalScrollBar().addAdjustmentListener(this);
      this.scrollPane.getHorizontalScrollBar().addAdjustmentListener(this);
      // The scroll mode must be BLIT_SCROLL_MODE and not 
      // BACKINGSTORE_SCROLL_MODE.  This second mode causes 
      // rendering artifacts.
      this.scrollPane.getViewport().
                         setScrollMode(JViewport.BLIT_SCROLL_MODE);
      this.scrollPane.getVerticalScrollBar().
                         setUnitIncrement(BUTTON_SCROLL_INCREMENT);
      this.scrollPane.
         setHorizontalScrollBarPolicy(
               ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      this.scrollPane.
         setVerticalScrollBarPolicy(
               ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      
      this.paintPanel = new SwingDrawingBoard(this.canvas, this);
      updatePreferredSize();
      
      MouseInputListener inputListener = this.canvas.getInputListener();
      this.paintPanel.addMouseListener(inputListener);
      this.paintPanel.addMouseMotionListener(inputListener);
      
      centerPanel.add(this.paintPanel);
      
      setLayout(new GridLayout(1, 1));
      add(this.scrollPane);
   }
   
   public JViewport getViewport()
   {
      return this.scrollPane.getViewport();
   }
   
   public CompositeCanvas getCanvas()
   {
      return this.canvas;
   }
   
   public boolean isScrolling()
   {
      return this.scrollPane.getVerticalScrollBar().getValueIsAdjusting() || 
             this.scrollPane.getHorizontalScrollBar().getValueIsAdjusting();
   }
   
   public void updatePreferredSize()
   {
      int width = 1+(int)(this.canvas.getWidth()+this.canvas.getX());
      int height = 1+(int)(this.canvas.getHeight()+this.canvas.getY());
      
      Dimension size = new Dimension(width, height);
      
      setPreferredSize(size);
      this.paintPanel.setPreferredSize(size);
      invalidate();
   }
   
   @Override
   public void repaint()
   {
      super.repaint();
      
      if (this.paintPanel != null)
         this.paintPanel.repaint();
      
      if (DebugSettings.getSharedInstance().notifyOfRepaints())
         System.err.println("repaint() called");
   }
   
   public void repaint(float x, float y, float width, float height)
   {
      if (DebugSettings.getSharedInstance().forceGlobalRepaints())
      {
         repaint();
         return;
      }
      
      int iX = (int)x;
      int iY = (int)y;
      
      int iWidth = 1+(int)width;
      if (iWidth == 0)
         iWidth = 1;
      
      int iHeight = 1+(int)height;
      if (iHeight == 0)
         iHeight = 1;
      
      //only painting the 'paint panel' which holds the painted binder 
      //and not 'this' panel (which contains the paint panel) needs to 
      //be repainted.
      
      // The method below adds the specified rectangle to the list of 
      // dirty rectangles that need repainting.  The first parameter 
      // is not used.
      this.paintPanel.repaint(0, iX, iY, iWidth, iHeight);
   }
   
   public void show(float x, float y, float width, float height)
   {
      int xPix = (int)x;
      int yPix = (int)y;
      int widthPix = (int)width;
      int heightPix = (int)height;
      
      Rectangle rect = new Rectangle(xPix, yPix, widthPix, heightPix);
      this.paintPanel.scrollRectToVisible(rect);
   }
   
   public void currentPageChanged(Page newCurPage)
   {
   }

   public void pageAdded(Page newPage)
   {
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
      //Indicates no coalescing has been done
      return null;
   }
   */
   
   public void adjustmentValueChanged(AdjustmentEvent e)
   {
      if (e == null)
         throw new NullPointerException();
      
      if (!e.getValueIsAdjusting())
         repaint();
   }

   public void mouseWheelMoved(MouseWheelEvent e)
   {
      if (e == null)
         throw new NullPointerException();
      
      int clickCount = e.getWheelRotation();
      JScrollBar scrollBar = this.scrollPane.getVerticalScrollBar();
      scrollBar.setValueIsAdjusting(true);
      scrollBar.setValue(scrollBar.getValue()+
                            clickCount*WHEEL_SCROLL_INCREMENT);
      scrollBar.setValueIsAdjusting(false);
   }
}
