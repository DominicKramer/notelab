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

package noteLab.model.tool;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.Vector;

import noteLab.model.geom.ScalableFloat;
import noteLab.util.InfoCenter;
import noteLab.util.mod.ModListener;
import noteLab.util.mod.ModType;
import noteLab.util.render.Renderer2D;

/**
 * This class represents a tool that is used to draw <code>Strokes</code> 
 * on a <code>Page</code>.
 * 
 * @author Dominic Kramer
 */
public class Pen implements Tool
{
   /** The default width of the line drawn by this tool. */
   private static final int DEFAULT_WIDTH = 1;
   
   private static final int MIN_CURSOR_WIDTH = 3;
   private static final int MAX_CURSOR_WIDTH = 15;
   private static final String CURSOR_NAME = InfoCenter.getAppName()+
                                                "CustomPenCursor";
   
   /** The default color of the line drawn by this tool. */
   private static final Color DEFAULT_COLOR = Color.BLACK;
   
   private ScalableFloat width;
   
   /** The color of the line drawn by this tool. */
   private Color color;
   
   /** This tool's cursor. */
   private Cursor cursor;
   
   /** The vector of listeners that are notified when this pen is modified. */
   private Vector<ModListener> modListenerVec;
   
   private Pen(Pen pen)
   {
      this.width = pen.width.getCopy();
      
      int red = pen.color.getRed();
      int green = pen.color.getGreen();
      int blue = pen.color.getBlue();
      int alpha = pen.color.getAlpha();
      this.color = new Color(red, green, blue, alpha);
      
      this.cursor = pen.constructCursor();
      
      this.modListenerVec = new Vector<ModListener>();
      for (ModListener listener : pen.modListenerVec)
         this.modListenerVec.add(listener);
   }
   
   /**
    * Constructs a pen such that width of the lines drawn by this pen 
    * is the width after being scaled by the given amount.  The default 
    * width and color of this pen's strokes are used.
    * 
    * @param scaleLevel The amount of the lines drawn by this pen are 
    *                   scaled in width.
    */
   public Pen(float scaleLevel)
   {
      this(DEFAULT_WIDTH, DEFAULT_COLOR, scaleLevel);
   }
   
   /**
    * Constructs a pen such that the lines drawn by it have the given width 
    * and color.  Also, the width of the lines drawn by this pen 
    * is the width after being scaled by the given amount.
    * 
    * @param width The width of the lines that are drawn by this pen.
    * @param color The color of the lines that are drawn by this pen.
    * @param scaleLevel The amount of the lines drawn by this pen are 
    *                   scaled in width.
    */
   public Pen(float width, Color color, float scaleLevel)
   {
      this.width = new ScalableFloat(width, scaleLevel);
      this.color = color;
      this.cursor = getCursor();
      
      this.modListenerVec = new Vector<ModListener>();
   }
   
   /**
    * Used to get this pen's scale level.  That is the level at which 
    * this pen's width is scale to relative its width if it were scaled 
    * at 100%.
    * 
    * @return This pen's scale level.
    */
   public float getScaleLevel()
   {
      return this.width.getScaleLevel();
   }
   
   /**
    * Used to get the color of the lines drawn by this pen.
    * 
    * @return The color of this pen's drawn lines.
    */
   public Color getColor()
   {
      return this.color;
   }
   
   /**
    * Used to set the color of the lines drawn by this pen.
    * 
    * @param color The color of this pen's drawn lines.
    */
   public void setColor(Color color)
   {
      if (color == null)
         throw new NullPointerException();
      
      this.color = color;
      invalidateCursor();
      
      notifyModListeners(ModType.Other);
   }
   
   /**
    * Used to get the width of the lines drawn by this pen.
    * 
    * @return The width of the lines drawn by this pen.
    */
   public float getWidth()
   {
      return this.width.getValue();
   }
   
   /**
    * Used to set the width of the lines drawn by this pen.
    * 
    * @param width The width of the lines drawn by this pen.
    */
   public void setWidth(float width)
   {
      if (width <= 0)
         width = 1;
      
      this.width.setValue(width, getScaleLevel());
      invalidateCursor();
      notifyModListeners(ModType.Other);
   }
   
   public void setRawWidth(float width)
   {
      if (width <= 0)
         width = 1;
      
      this.width.setValue(width, 1);
      invalidateCursor();
      notifyModListeners(ModType.Other);
   }
   
   /**
    * Used to get this pen's cursor.
    * 
    * @return This pen's cursor.
    */
   public Cursor getCursor()
   {
      if (this.cursor == null)
         this.cursor = constructCursor();
      
      return this.cursor;
   }

   private Cursor constructCursor()
   {
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      
      int size = (int)Math.min(Math.max(getWidth(), MIN_CURSOR_WIDTH), 
                               MAX_CURSOR_WIDTH);
      Dimension bestSize = toolkit.getBestCursorSize(size, size);
      
      int bestWidth = Math.max(1, (int)bestSize.getWidth());
      int bestHeight = Math.max(1, (int)bestSize.getHeight());
      
      BufferedImage image = new BufferedImage(bestWidth, 
                                              bestHeight, 
                                              BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2d = (Graphics2D)image.createGraphics();
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                          RenderingHints.VALUE_ANTIALIAS_ON);
      
      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                                          RenderingHints.VALUE_RENDER_QUALITY);
      
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                          RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      
      g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, 
                                          RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      
      g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, 
                                          RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      
      g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
                                          RenderingHints.VALUE_STROKE_PURE);
      
      g2d.setRenderingHint(RenderingHints.KEY_DITHERING, 
                                          RenderingHints.VALUE_DITHER_ENABLE);
      g2d.setColor(this.color);
      g2d.fillOval(0, 0, size, size);
      
      return toolkit.createCustomCursor(image, 
                                        new Point(size/2, size/2), 
                                        CURSOR_NAME);
   }
   
   private void invalidateCursor()
   {
      this.cursor = null;
   }
   
   /**
    * Used to get a deep copy of this pen.
    * 
    * @return A deep copy of this pen.
    */
   public Pen getCopy()
   {
      return new Pen(this);
   }
   
   /**
    * Adjusts the given renderer such that the lines drawn by the 
    * renderer have the width and color as specified by this pen.
    * 
    * @param renderer The renderer to adjust.
    */
   public void adjustRenderer(Renderer2D renderer)
   {
      if (renderer == null)
         throw new NullPointerException();
      
      renderer.setLineWidth(getWidth());
      renderer.setColor(this.color);
   }
   
   /**
    * Scales this pen's line width by the given amount.
    * 
    * @param val The amount this pen's line width should be scaled.
    */
   public void scaleBy(float val)
   {
      if (val < 0)
         val = -val;
      
      this.width.scaleBy(val);
      invalidateCursor();
      notifyModListeners(ModType.ScaleBy);
   }
   
   public void resizeTo(float val)
   {
      if (val < 0)
         val = -val;
      
      this.width.resizeTo(val);
      invalidateCursor();
      notifyModListeners(ModType.ScaleBy);
   }
   
   /**
    * Scales this pen's line width to the given amount.
    * 
    * @param val The line width this pen's line width should be 
    *            scaled to.
    */
   public void scaleTo(float val)
   {
      if (val < 0)
         val = -val;
      
      this.width.scaleTo(val);
      invalidateCursor();
      notifyModListeners(ModType.ScaleTo);
   }
   
   /**
    * Used to add a listener that is notified when this pen is 
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
    * notified when this pen is modified.
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
    * Informs all of the ModListeners that this pen has been modified.
    * 
    * @param type The type of modification.
    */
   private void notifyModListeners(ModType type)
   {
      for (ModListener listener : this.modListenerVec)
         listener.modOccured(this, type);
   }
   
   @Override
   public String toString()
   {
      StringBuffer buffer = new StringBuffer(getClass().getSimpleName());
      buffer.append(":  Width=");
      buffer.append(this.width);
      buffer.append(" Color(r,g,b)=(");
      buffer.append(this.color.getRed());
      buffer.append(",");
      buffer.append(this.color.getGreen());
      buffer.append(",");
      buffer.append(this.color.getBlue());
      buffer.append(")");
      
      return buffer.toString();
   }
   
   public static void main(String[] args)
   {
      Pen pen = new Pen(2, Color.BLACK, 1);
      System.err.println("pen="+pen);
      
      pen.scaleTo(0.5f);
      System.err.println("scale to 0.5f = "+pen);
      
      pen.resizeTo(2);
      System.err.println("resize to 2 = "+pen);
      
      pen.scaleTo(1);
      System.err.println("scale to 1 = "+pen);
   }
}
