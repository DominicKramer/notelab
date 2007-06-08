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
import java.util.Vector;

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
   
   /** The default color of the line drawn by this tool. */
   private static final Color DEFAULT_COLOR = Color.BLACK;
   
   /** This pen's scale level. */
   private float scaleLevel;
   
   private float initWidth;
   
   /** The width of the line drawn by this tool. */
   private float width;
   
   /** The color of the line drawn by this tool. */
   private Color color;
   
   /** This tool's cursor. */
   private Cursor cursor;
   
   /** The vector of listeners that are notified when this pen is modified. */
   private Vector<ModListener> modListenerVec;
   
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
      this.initWidth = width/scaleLevel;
      this.width = width;
      this.color = color;
      this.cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
      
      this.scaleLevel = scaleLevel;
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
      return this.scaleLevel;
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
      
      notifyModListeners(ModType.Other);
   }
   
   /**
    * Used to get the width of the lines drawn by this pen.
    * 
    * @return The width of the lines drawn by this pen.
    */
   public float getWidth()
   {
      return this.width;
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
      
      float newWidth = width/this.scaleLevel;
      this.initWidth = newWidth;
      this.width = newWidth;
      notifyModListeners(ModType.Other);
   }
   
   public void setRawWidth(float width)
   {
      if (width <= 0)
         width = 1;
      
      this.initWidth = width;
      this.width = width;
      notifyModListeners(ModType.Other);
   }
   
   /**
    * Used to get this pen's cursor.
    * 
    * @return This pen's cursor.
    */
   public Cursor getCursor()
   {
      return this.cursor;
   }
   
   /**
    * Used to get a deep copy of this pen.
    * 
    * @return A deep copy of this pen.
    */
   public Pen getCopy()
   {
      return new Pen(this.width, this.color, this.scaleLevel);
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
      
      float width = this.width;
      
      renderer.setLineWidth(width);
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
      
      this.scaleLevel *= val;
      this.width = this.initWidth*this.scaleLevel;
      
      notifyModListeners(ModType.ScaleBy);
   }
   
   public void resizeTo(float val)
   {
      if (val < 0)
         val = -val;
      
      float oldScale = this.scaleLevel;
      scaleTo(1);
      
      this.initWidth *= val;
      this.width = this.initWidth;
      this.scaleLevel = 1;
      
      scaleTo(oldScale);
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
      
      this.scaleLevel = val;
      this.width = this.initWidth*val;
      
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
