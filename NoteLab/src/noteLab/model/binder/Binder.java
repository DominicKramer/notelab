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

package noteLab.model.binder;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import noteLab.gui.listener.RepaintBroadcastCenter;
import noteLab.gui.listener.RepaintListener;
import noteLab.model.Page;
import noteLab.model.Paper.PaperType;
import noteLab.model.geom.FloatPoint2D;
import noteLab.util.CopyReady;
import noteLab.util.geom.Bounded;
import noteLab.util.geom.RectangleUnioner;
import noteLab.util.geom.Transformable;
import noteLab.util.geom.unit.Unit;
import noteLab.util.mod.ModBroadcaster;
import noteLab.util.mod.ModListener;
import noteLab.util.mod.ModType;
import noteLab.util.render.PrinterRenderer2D;
import noteLab.util.render.Renderable;
import noteLab.util.render.Renderer2D;

/**
 * A binder represents a collection of <code>Pages</code>.  This class stores the information 
 * needed by any representation of a binder.  However, subclasses handle how <code>Pages</code> 
 * are arranged in the binder.
 * <br><br>
 * This class is used in that a <code>CompositeCanvas</code> uses a <code>Binder</code> to 
 * hold all of its <code>Pages</code>.
 * 
 * @author Dominic Kramer
 */
public abstract class Binder implements Renderable, Bounded, 
                                        RepaintBroadcastCenter, 
                                        ModBroadcaster, 
                                        ModListener, 
                                        Iterable<Page>, 
                                        Transformable, 
                                        Printable, 
                                        Pageable, 
                                        CopyReady<Binder>
{
   /**
    * The format in which this binder's pages are in.  This field 
    * is used when printing this binder.
    */
   private static PageFormat LETTER_SIZE_FORMAT = new PageFormat();
   
   /** The index of this binder's current page. */
   private int curPage;
   
   /** The list of pages that this binder contains. */
   private LinkedList<Page> pageList;
   
   /** The vector of listeners that should be informed when repaints occur. */
   private Vector<RepaintListener> repaintListeners;
   
   /**
    * The vector of listeners that should be informed when a page has been added 
    * or removed from this binder or when this binder's current page has been 
    * modified.
    */
   private Vector<BinderListener> binderListeners;
   
   /** The level, along the x-axis, at which this binder is currently scaled. */
   protected float xScaleLevel;
   
   /** The level, along the y-axis, at which this binder is currently scaled. */
   protected float yScaleLevel;
   
   /**
    * The vector of listeners that should be informed when this binder has been 
    * modified.
    */
   private Vector<ModListener> modListenerVec;
   
   public Binder(float xScaleLevel, float yScaleLevel, Page ...pages)
   {
      if (pages == null)
         throw new NullPointerException();
      
      if (pages.length == 0)
         pages = new Page[] {new Page(PaperType.CollegeRuled, 
                                      xScaleLevel, yScaleLevel, 
                                      Unit.getScreenResolution())};
      
      this.modListenerVec = new Vector<ModListener>();
      
      this.xScaleLevel = xScaleLevel;
      this.yScaleLevel = yScaleLevel;
      
      this.repaintListeners = new Vector<RepaintListener>();
      this.binderListeners = new Vector<BinderListener>();
      
      this.pageList = new LinkedList<Page>();
      for (Page p : pages)
         addPage(p);
      
      this.curPage = -1;
      setCurrentPage(0);
   }
   
//----------------=[ Methods used for modifying the pages ]=------------------//
   /**
    * Implemented so an object of this class can be used in a 
    * <code>foreach</code> loop.
    */
   public Iterator<Page> iterator()
   {
      return this.pageList.iterator();
   }
   
   /** Adds a deep copy of the current page to the end of the binder. */
   public void copyPage()
   {
      addPage(getCurrentPage().getCopy());
   }
   
   /** Adds a new page to the end of the binder. */
   public void addNewPage()
   {
      addPage(new Page(getCurrentPage()));
   }
   
   /**
    * Adds the given page to the edge of the binder.
    * 
    * @param page The page to add to the end of the binder.
    */
   public void addPage(Page page)
   {
      if (page == null)
         throw new NullPointerException();
      
      this.pageList.add(page);
      
      page.addModListener(this);
      
      setCurrentPage(getNumberOfPages()-1);
      doLayout();
      
      notifyModListeners(ModType.Other);
      
      for (BinderListener listener : this.binderListeners)
         listener.pageAdded(this, page);
   }
   
   /**
    * Removes the current page from the binder.  If the page is the last page in the binder 
    * a new page is added to the binder so that the binder is not empty.
    */
   public void removeCurrentPage()
   {
      removePage(getCurrentPage());
   }
   
   /**
    * Removes the given page from the binder if it is actually a page in the binder.  If the 
    * page is the last page in the binder a new page is added to the binder so that the binder 
    * is not empty.
    * 
    * @param page The page to remove from the binder.
    */
   public void removePage(Page page)
   {
      if (page == null)
         throw new NullPointerException();
      
      if (getNumberOfPages() < 2)
         return;
      
      Page curPage = getCurrentPage();
      curPage.setSelected(false);
      
      int newIndex = this.pageList.indexOf(page);
      if (newIndex > 0)
         newIndex--;
      
      page.removeModListener(this);
      this.pageList.remove(page);
      this.curPage = newIndex;
      getCurrentPage().setSelected(true);
      repaint();
      doLayout();
      
      notifyModListeners(ModType.Other);
      
      for (BinderListener listener : this.binderListeners)
      {
         listener.pageRemoved(this, page);
         listener.currentPageChanged(this);
      }
   }
   
   /**
    * Used to get the page at the specified index.  Indices start with <code>0</code> and 
    * not <code>1</code>.
    * 
    * @param index The index of the page to retrieve.
    * 
    * @return The page at the given index or <code>null</code> if the index is invalid.
    */
   private Page getPageAt(int index)
   {
      if (index < 0 || index >= getNumberOfPages())
         return null;
      
      return this.pageList.get(index);
   }
   
   /**
    * Used to get the binder's current page.
    * 
    * @return The binder's current page.
    */
   public Page getCurrentPage()
   {
      return getPageAt(curPage);
   }
   
   /**
    * Used to set the binder's current page to the page with the specified index.
    * 
    * @param index The index of the new current page.  If the index is invalid nothing 
    *              is done.
    */
   public void setCurrentPage(final int index)
   {
      if (index < 0 || index >= getNumberOfPages())
         throw new NullPointerException();
      
      if (this.curPage == index)
         return;
      
      if (this.curPage == -1)
         this.curPage = 0;
      
      Page oldCurPage = getCurrentPage();
      boolean selEnabled = false;
      if (oldCurPage != null)
      {
         oldCurPage.setSelected(false);
         selEnabled = oldCurPage.getSelectionEnabled();
         oldCurPage.setSelectionEnabled(false);
      }
      
      this.curPage = index;
      
      Page curPage = getCurrentPage();
      curPage.setSelected(true);
      curPage.setSelectionEnabled(selEnabled);
      
      notifyModListeners(ModType.Other);
      
      for (BinderListener listener : this.binderListeners)
         listener.currentPageChanged(this);
   }
   
   /**
    * Sets the binder's current page to the page which contains the given point.  The point is 
    * relative to the <code>CompositeCanvas</code> which contains this <code>Binder</code>.
    * 
    * @param point The point in the <code>CompositeCanvas</code> which holds this binder.
    * 
    * @return <code>true</code> if the point is valid and the current page has been set or 
    *         <code>false</code> otherwise.
    */
   public boolean setCurrentPage(FloatPoint2D point)
   {
      for (int i=0; i<getNumberOfPages(); i++)
      {
         if (getPageAt(i).contains(point))
         {
            boolean changed = (i != this.curPage);
            setCurrentPage(i);
            return changed;
         }
      }
      
      return false;
   }
   
   public boolean canFlipForward()
   {
      return (this.curPage+1) < getNumberOfPages();
   }
   
   public void flipForward()
   {
      if (canFlipForward())
         setCurrentPage(this.curPage+1);
   }
   
   public boolean canFlipBack()
   {
      return (this.curPage-1) >= 0;
   }
   
   public void flipBack()
   {
      if (canFlipBack())
         setCurrentPage(this.curPage-1);
   }
//--------------=[ End methods used for modifying the pages ]=----------------//
   
   
//--------------------=[ Methods used for repainting ]=-----------------------//
   public void addRepaintListener(RepaintListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.repaintListeners.contains(listener))
         this.repaintListeners.add(listener);
   }

   public void removeRepaintListener(RepaintListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      this.repaintListeners.remove(listener);
   }
   
   public void repaint(float x, float y, float width, float height)
   {
      for (RepaintListener listener : this.repaintListeners)
         listener.repaint(x, y, width, height);
   }
   
   public void repaint()
   {
      for (RepaintListener listener : this.repaintListeners)
         listener.repaint();
   }
   
   public void show(float x, float y, float width, float height)
   {
      for (RepaintListener listener : this.repaintListeners)
         listener.show(x, y, width, height);
   }
   
   public void show(Page page)
   {
      if (page == null)
         throw new NullPointerException();
      
      show(page.getX(), page.getY(), 
           page.getWidth(), page.getHeight());
   }
   
   public void showCurrent()
   {
      show(getCurrentPage());
   }
//------------------=[ End methods used for repainting ]=---------------------//
   
   
//----------------=[ Methods for getting the geometry ]=----------------------//
   public FloatPoint2D clipPoint(float x, float y)
   {
      return getCurrentPage().clipPoint(x, y);
   }
   
   public Rectangle2D.Float getBounds2D()
   {
      RectangleUnioner unioner = new RectangleUnioner();
      for (Page p : this.pageList)
         unioner.union(p.getBounds2D());
      
      return unioner.getUnion();
   }
   
   public float getX()
   {
      return (float)getBounds2D().getX();
   }
   
   public float getY()
   {
      return (float)getBounds2D().getY();
   }
   
   public float getWidth()
   {
      return getBounds2D().width;
   }
   
   public float getHeight()
   {
      return getBounds2D().height;
   }
//-------------=[ End methods for getting the geometry ]=---------------------//
   
   public void renderInto(Renderer2D mG2d)
   {
      if (mG2d == null)
         throw new NullPointerException();
      
      int screenRes = Unit.getScreenResolution();
      if (this.pageList.size() > 0)
         screenRes = getPageAt(0).getPaper().getScreenResolution();
      
      mG2d.beginGroup(this, ""+screenRes, this.xScaleLevel, this.yScaleLevel);
      
      float pageX;
      float pageY;
      for (Page p : this.pageList)
      {
         if (!mG2d.isInClipRegion(p))
            continue;
         
         pageX = p.getX();
         pageY = p.getY();
         
         // translate the origin to the top left corner 
         // of the page
         mG2d.translate(pageX, pageY);
         
         // render the page
         p.renderInto(mG2d);
         
         // translate back to the origin
         mG2d.translate(-pageX, -pageY);
      }
      
      mG2d.endGroup(this);
   }
   
   public void doLayout()
   {
      doLayoutImpl();
      notifyModListeners(ModType.Other);
   }
   
//-----------------------=[ Abstract methods ]=-------------------------------//
   public abstract void doLayoutImpl();
//---------------------=[ End abstract methods ]=-----------------------------//

   public void scaleBy(float x, float y)
   {
      this.xScaleLevel *= x;
      this.yScaleLevel *= y;
      
      for (Page page : this.pageList)
         page.scaleBy(x, y);
      
      notifyModListeners(ModType.ScaleBy);
   }
   
   // Like the scaleTo method except the new size is stored as 
   // the size of the object as if it were scaled to a scale 
   // level of 1
   public void resizeTo(float x, float y)
   {
      this.xScaleLevel = 1;
      this.yScaleLevel = 1;
      
      for (Page page : this.pageList)
         page.resizeTo(x, y);
      
      notifyModListeners(ModType.ScaleTo);
   }
   
   public void scaleTo(float x, float y)
   {
      this.xScaleLevel = x;
      this.yScaleLevel = y;
      
      for (Page page : this.pageList)
         page.scaleTo(x, y);
      
      notifyModListeners(ModType.ScaleTo);
   }

   public void translateBy(float x, float y)
   {
      for (Page page : this.pageList)
         page.translateBy(x, y);
      
      notifyModListeners(ModType.TranslateBy);
   }

   public void translateTo(float x, float y)
   {
      for (Page page : this.pageList)
         page.translateTo(x, y);
      
      notifyModListeners(ModType.TranslateTo);
   }
   
   /**
    * Used to add a listener that is notified when this binder is 
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
    * notified when this binder is modified.
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
    * Invoked when a modification to one of this binder's pages 
    * has occured.
    * 
    * @param source The page that was modified.
    * @param type The type of modification.
    */
   public void modOccured(Object source, ModType type)
   {
      if (type == ModType.Other)
         notifyModListeners(type);
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
   
   public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) 
                       throws PrinterException
   {
      if (pageIndex < 0 || pageIndex >= getNumberOfPages())
         return Printable.NO_SUCH_PAGE;
      
      Page page = this.pageList.get(pageIndex).getCopy();
      PrinterRenderer2D printRenderer = 
         new PrinterRenderer2D(graphics, pageFormat, 
                               page.getWidth(), page.getHeight());
      page.renderInto(printRenderer);
      
      return Printable.PAGE_EXISTS;
   }

   public int getNumberOfPages()
   {
      return this.pageList.size();
   }

   public PageFormat getPageFormat(int pageIndex) 
                        throws IndexOutOfBoundsException
   {
      if (pageIndex < 0 || pageIndex >= getNumberOfPages())
         throw new IndexOutOfBoundsException();
      
      return LETTER_SIZE_FORMAT;
   }

   public Printable getPrintable(int pageIndex) 
                       throws IndexOutOfBoundsException
   {
      return this;
   }
   
   public void addBinderListener(BinderListener listener)
   {
      if (listener == null)
         throw new NullPointerException();
      
      if (!this.binderListeners.contains(listener))
         this.binderListeners.add(listener);
   }
   
   public void setAllStrokeSelected(boolean isSelected)
   {
      for (Page p : this)
         p.setAllStrokeSelected(isSelected);
   }
}
