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

package noteLab.model;

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import noteLab.model.Paper.PaperType;
import noteLab.model.geom.FloatPoint2D;
import noteLab.model.geom.TransformRectangle2D;
import noteLab.util.CopyReady;
import noteLab.util.Selectable;
import noteLab.util.UnitScaleDependent;
import noteLab.util.geom.Bounded;
import noteLab.util.geom.unit.Unit;
import noteLab.util.mod.ModListener;
import noteLab.util.mod.ModType;
import noteLab.util.render.Renderable;
import noteLab.util.render.Renderer2D;

public class Page extends TransformRectangle2D 
                     implements Renderable, CopyReady<Page>, 
                                Selectable, Bounded, 
                                ModListener, Iterable<Stroke>, 
                                UnitScaleDependent
{
   private Paper paper;
   private LinkedList<Stroke> strokeList;
   
   private Vector<Stroke> selStrokeVec;
   private Vector<Stroke> unSelStrokeVec;
   
   public Page(Page page)
   {
      this( (float)page.getWidth(), (float)page.getHeight(), 
             page.getPaper().getPaperType(), 
             page.getXScaleLevel(), page.getYScaleLevel(), 
             page.getPaper().getScreenResolution(), 
             page.getPaper().getUnitScaleFactor());
      
      Color color = page.getPaper().getBackgroundColor();
      this.paper.setBackgroundColor(new Color(color.getRed(), 
                                              color.getGreen(), 
                                              color.getBlue()));
   }
   
   public Page(PaperType paperType, 
               float xScaleLevel, float yScaleLevel, 
               int screenRes, float unitScaleLevel)
   {
      this( Unit.getValue(8.5f, Unit.INCH, Unit.PIXEL, screenRes, unitScaleLevel), 
            Unit.getValue(11f, Unit.INCH, Unit.PIXEL, screenRes, unitScaleLevel), 
            paperType, xScaleLevel, yScaleLevel, screenRes, unitScaleLevel);
   }
   
   public Page(float width, float height, PaperType paperType, 
               float xScaleLevel, float yScaleLevel, 
               int screenRes, float unitScaleLevel)
   {
      super(0, 0, width, height, xScaleLevel, yScaleLevel);
      
      this.paper = new Paper(paperType, width, height, 
                             xScaleLevel, yScaleLevel, 
                             screenRes, unitScaleLevel);
      
      this.strokeList = new LinkedList<Stroke>();
      this.selStrokeVec = new Vector<Stroke>();
      this.unSelStrokeVec = new Vector<Stroke>();
      
      setPaperType(paperType);
      setSelected(false);
   }
   
   public boolean isSelected()
   {
      return this.paper.isSelected();
   }
   
   public void setSelected(boolean isSelected)
   {
      this.paper.setSelected(isSelected);
      notifyModListeners(ModType.Other);
   }
   
   public boolean getSelectionEnabled()
   {
      return this.paper.getSelectionEnabled();
   }
   
   public void setSelectionEnabled(boolean enabled)
   {
      this.paper.setSelectionEnabled(enabled);
   }
   
   public void setAllStrokeSelected(boolean isSelected)
   {
      for (Stroke stroke : this)
         setStrokeSelected(stroke, isSelected);
   }
   
   public void setStrokeSelected(Stroke stroke, boolean isSelected)
   {
      if (stroke == null)
         throw new NullPointerException();
      
      stroke.setSelected(isSelected);
      
      if (isSelected)
      {
         // remove the stroke from the list of unselected strokes
         this.unSelStrokeVec.remove(stroke);
         
         // add the stroke to the list of selected strokes
         if (!this.selStrokeVec.contains(stroke))
            this.selStrokeVec.add(stroke);
      }
      else
      {
         // remove the stroke from the list of selected strokes
         this.selStrokeVec.remove(stroke);
         
         // add the stroke to the list of unselected strokes
         if (!this.unSelStrokeVec.contains(stroke))
            this.unSelStrokeVec.add(stroke);
      }
   }
   
   public void clear()
   {
      int size = this.strokeList.size();
      for (int i=size-1; i>=0; i--)
         removeStroke(this.strokeList.get(i));
   }
   
   public void removeStroke(Stroke stroke)
   {
      if (stroke == null)
         throw new NullPointerException();
      
      stroke.removeModListener(this);
      this.strokeList.remove(stroke);
      
      if (stroke.isSelected())
         this.selStrokeVec.remove(stroke);
      else
         this.unSelStrokeVec.remove(stroke);
      
      notifyModListeners(ModType.Other);
   }
   
   public void addStroke(Stroke stroke)
   {
      if (stroke == null)
         throw new NullPointerException();
      
      stroke.addModListener(this);
      
      this.strokeList.add(stroke);
      if (stroke.isSelected())
         this.selStrokeVec.add(stroke);
      else
         this.unSelStrokeVec.add(stroke);
      
      notifyModListeners(ModType.Other);
   }
   
   public void renderInto(Renderer2D mG2d)
   {
      if (mG2d == null)
         throw new NullPointerException();
      
      mG2d.beginGroup(this, this.paper.getPaperType().name(), 
                      getXScaleLevel(), getYScaleLevel());
      mG2d.tryRenderBoundingBox(this);
      this.paper.renderInto(mG2d);
      
      for (Stroke stroke : this.strokeList)
         if (mG2d.isInClipRegion(stroke))
            stroke.renderInto(mG2d);
      
      mG2d.endGroup(this);
   }
   
   public Paper getPaper()
   {
      return this.paper;
   }
   
   public void setPaperType(PaperType paper)
   {
      if (paper == null)
         throw new NullPointerException();
      
      this.paper.setPaperType(paper);
      notifyModListeners(ModType.Other);
   }
   
   public Page getCopy()
   {
      Page copy = new Page(this.paper.getWidth(), 
                           this.paper.getHeight(), 
                           this.paper.getPaperType(), 
                           getXScaleLevel(), 
                           getYScaleLevel(), 
                           this.paper.getScreenResolution(), 
                           this.paper.getUnitScaleFactor());
      Color bgColor = this.paper.getBackgroundColor();
      copy.paper.setBackgroundColor(new Color(bgColor.getRed(), 
                                              bgColor.getGreen(), 
                                              bgColor.getBlue()));
      
      for (Stroke stroke : this.strokeList)
         copy.strokeList.add(stroke.getCopy());
      
      return copy;
   }
   
   public Vector<Stroke> getStrokesAt(FloatPoint2D point)
   {
      if (point == null)
         throw new NullPointerException();
      
      float ptX = point.getX();
      float ptY = point.getY();
      
      // make sure the point is inside this page
      if ( ptX < 0 || ptY < 0 || ptX > getWidth() || ptY > getHeight())
         return new Vector<Stroke>(0);
      
      Vector<Stroke> strokeVec = new Vector<Stroke>();
      for (Stroke stroke : this.strokeList)
         if (stroke.containsPoint(point))
            strokeVec.add(stroke);
      
      return strokeVec;
   }
   
   public FloatPoint2D clipPoint(float x, float y)
   {
      float clipX = x;
      float clipY = y;
      
      float xMin = (float)this.paper.getMinX();
      float xMax = (float)this.paper.getMaxX();
      
      float yMin = (float)this.paper.getMinY();
      float yMax = (float)this.paper.getMaxY();
      
      if (clipX < xMin)
         clipX = xMin;
      else if (clipX > xMax)
         clipX = xMax;
      
      if (clipY < yMin)
         clipY = yMin;
      else if (clipY > yMax)
         clipY = yMax;
      
      return new FloatPoint2D(clipX, clipY, 
                              getXScaleLevel(), getYScaleLevel());
   }
   
   @Override
   public String toString()
   {
      StringBuffer buffer = new StringBuffer("Page\n[\n  Dimension='");
        buffer.append("width=");
        buffer.append(this.paper.getWidth());
        buffer.append(" px, height=");
        buffer.append(this.paper.getHeight());
        buffer.append(" px");
        buffer.append("'\n");
        buffer.append("  Strokes:  \n");
        for (Stroke stroke : this.strokeList)
        {
           buffer.append("    ");
           buffer.append(stroke);
           buffer.append("\n");
        }
        buffer.append("]");
        
      return buffer.toString();
   }

   @Override
   public void scaleBy(float x, float y)
   {
      super.scaleBy(x, y);
      
      this.paper.scaleBy(x, y);
      for (Stroke stroke : this.strokeList)
         stroke.scaleBy(x, y);
   }

   @Override
   public void scaleTo(float x, float y)
   {
      super.scaleTo(x, y);
      
      this.paper.scaleTo(x, y);
      for (Stroke stroke : this.strokeList)
         stroke.scaleTo(x, y);
   }
   
   @Override
   public void resizeTo(float x, float y)
   {
      super.resizeTo(x, y);
      
      this.paper.resizeTo(x, y);
      for (Stroke stroke : this.strokeList)
         stroke.resizeTo(x, y);
   }

   @Override
   public void translateBy(float x, float y)
   {
      super.translateBy(x, y);
      
      this.paper.translateBy(x, y);
      for (Stroke stroke : this.strokeList)
         stroke.translateBy(x, y);
   }

   @Override
   public void translateTo(float x, float y)
   {
      super.translateTo(x, y);
      
      this.paper.translateTo(x, y);
      for (Stroke stroke : this.strokeList)
         stroke.translateTo(x, y);
   }
   
   @Override
   public void setX(float x)
   {
      super.setX(x);
      // don't set the paper's x value to this because 
      // the paper's x value = 0 since it is relative to 
      // the top left corner of this page
   }
   
   @Override
   public void setY(float y)
   {
      super.setY(y);
      // don't set the paper's y value to this because 
      // the paper's y value = 0 since it is relative to 
      // the top left corner of this page
   }
   
   @Override
   public void setWidth(float width)
   {
      super.setWidth(width);
      this.paper.setWidth(width);
   }
   
   @Override
   public void setHeight(float height)
   {
      super.setHeight(height);
      this.paper.setHeight(height);
   }
   
   public void modOccured(Object source, ModType type)
   {
      if (type == ModType.Other)
         notifyModListeners(type);
   }
   
   public Iterator<Stroke> iterator()
   {
      return this.strokeList.iterator();
   }
   
   public Vector<Stroke> getSelectedStrokesCopy()
   {
      Vector<Stroke> copy = new Vector<Stroke>(this.selStrokeVec.size());
      for (Stroke stroke : this.selStrokeVec)
         copy.add(stroke);
      
      return copy;
   }
   
   public Vector<Stroke> getUnselectedStrokesCopy()
   {
      Vector<Stroke> copy = new Vector<Stroke>(this.unSelStrokeVec.size());
      for (Stroke stroke : this.unSelStrokeVec)
         copy.add(stroke);
      
      return copy;
   }
   
   public int getNumStrokes()
   {
      return this.strokeList.size();
   }
   
   public int getNumUnselectedStrokes()
   {
      return this.unSelStrokeVec.size();
   }
   
   public Stroke getSelectedStrokeAt(int index)
   {
      return this.selStrokeVec.elementAt(index);
   }
   
   public int getNumSelectedStrokes()
   {
      return this.selStrokeVec.size();
   }
   
   public Stroke getUnSelectedStrokeAt(int index)
   {
      return this.unSelStrokeVec.elementAt(index);
   }

   public float getUnitScaleFactor()
   {
      return this.paper.getUnitScaleFactor();
   }

   public void setUnitScaleFactor(float unitScaleFactor)
   {
      this.paper.setUnitScaleFactor(unitScaleFactor);
   }
}
