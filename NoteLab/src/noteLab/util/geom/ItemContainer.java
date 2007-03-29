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

package noteLab.util.geom;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import noteLab.util.mod.ModBroadcaster;
import noteLab.util.mod.ModListener;
import noteLab.util.mod.ModType;

public class ItemContainer<T extends Transformable & Bounded & ModBroadcaster> 
                           implements Transformable, Iterable<T>, 
                                      ModBroadcaster, 
                                      ModListener, 
                                      Bounded
{
   private LinkedList<T> itemList;
   protected Vector<ModListener> modListenerVec;
   
   protected float xScaleLevel;
   protected float yScaleLevel;
   
   public ItemContainer(float xScaleLevel, float yScaleLevel)
   {
      this.itemList = new LinkedList<T>();
      this.modListenerVec = new Vector<ModListener>();
      
      this.xScaleLevel = xScaleLevel;
      this.yScaleLevel = yScaleLevel;
   }
   
   public int getNumItems()
   {
      return this.itemList.size();
   }
   
   public boolean isEmpty()
   {
      return this.itemList.isEmpty();
   }
   
   public boolean isIndexValid(int index)
   {
      return (index >= 0) && (index < getNumItems());
   }
   
   public T getItemAt(int index)
   {
      if (!isIndexValid(index))
         return null;
      
      return this.itemList.get(index);
   }
   
   public T getFirst()
   {
      if (getNumItems() == 0)
         return null;
      
      return this.itemList.getFirst();
   }
   
   public T getLast()
   {
      if (getNumItems() == 0)
         return null;
      
      return this.itemList.getLast();
   }
   
   protected void clear()
   {
      this.itemList.clear();
      
      notifyModListeners(ModType.Other);
   }
   
   public void addItem(T item)
   {
      if (item == null)
         throw new NullPointerException();
      
      this.itemList.add(item);
      item.addModListener(this);
      
      notifyModListeners(ModType.Other);
   }
   
   public void translateBy(float x, float y)
   {
      for (T item : this.itemList)
         item.translateBy(x, y);
      
      notifyModListeners(ModType.TranslateBy);
   }

   public void scaleBy(float x, float y)
   {
      this.xScaleLevel *= x;
      this.yScaleLevel *= y;
      
      for (T item : this.itemList)
         item.scaleBy(x, y);
      
      notifyModListeners(ModType.ScaleBy);
   }

   public void translateTo(float x, float y)
   {
      Rectangle2D bounds = getBounds2D();
      
      final float minX = (float)bounds.getMinX();
      final float minY = (float)bounds.getMinY();
      
      float xDiff = x-minX;
      float yDiff = y-minY;
      
      for (T item : this.itemList)
         item.translateBy(xDiff, yDiff);
      
      notifyModListeners(ModType.TranslateTo);
   }

   public void scaleTo(float x, float y)
   {
      this.xScaleLevel = x;
      this.yScaleLevel = y;
      
      for (T item : this.itemList)
         item.scaleTo(x, y);
      
      notifyModListeners(ModType.ScaleTo);
   }
   
   public void resizeTo(float x, float y)
   {
      for (T item : this.itemList)
         item.resizeTo(x, y);
      
      this.xScaleLevel = 1;
      this.yScaleLevel = 1;
      
      notifyModListeners(ModType.ScaleTo);
   }

   public Iterator<T> iterator()
   {
      return this.itemList.iterator();
   }
   
   public Rectangle2D.Float getBounds2D()
   {
      RectangleUnioner unioner = new RectangleUnioner();
      for (T item : this.itemList)
         unioner.union(item.getBounds2D());
      
      return unioner.getUnion();
   }
   
   public float getXScaleLevel()
   {
      return this.xScaleLevel;
   }
   
   public float getYScaleLevel()
   {
      return this.yScaleLevel;
   }

   public void addModListener(ModListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.modListenerVec.contains(listener))
         this.modListenerVec.add(listener);
   }

   public void removeModListener(ModListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.modListenerVec.remove(listener);
   }

   public void modOccured(Object source, ModType type)
   {
      if (type == ModType.Other)
         notifyModListeners(type);
   }
   
   protected void notifyModListeners(ModType type)
   {
      for (ModListener listener : this.modListenerVec)
         listener.modOccured(this, type);
   }
}
