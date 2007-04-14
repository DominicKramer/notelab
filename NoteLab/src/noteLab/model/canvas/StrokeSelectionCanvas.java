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

package noteLab.model.canvas;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.SlidingPanel;
import noteLab.gui.ToolBarButton;
import noteLab.gui.DecoratedButton.Style;
import noteLab.gui.control.drop.ColorControl;
import noteLab.gui.control.drop.SizeControl;
import noteLab.gui.listener.ValueChangeEvent;
import noteLab.gui.listener.ValueChangeListener;
import noteLab.gui.menu.MenuConstants;
import noteLab.gui.menu.MenuPath;
import noteLab.gui.menu.PathMenuItem;
import noteLab.gui.toolbar.CutCopyPasteToolBar;
import noteLab.model.Page;
import noteLab.model.Path;
import noteLab.model.Stroke;
import noteLab.model.binder.Binder;
import noteLab.model.geom.FloatPoint2D;
import noteLab.model.tool.Pen;
import noteLab.model.tool.StrokeSelector;
import noteLab.util.copy.CopyStateListener;
import noteLab.util.copy.CutCopyPasteReady;
import noteLab.util.geom.RectangleUnioner;
import noteLab.util.geom.unit.MValue;
import noteLab.util.geom.unit.Unit;
import noteLab.util.render.Renderer2D;
import noteLab.util.settings.SettingsUtilities;
import noteLab.util.structure.CopyVector;
import noteLab.util.undoRedo.action.DeletePagedStrokeAction;
import noteLab.util.undoRedo.action.DrawPagedStrokeAction;

public class StrokeSelectionCanvas extends SubCanvas<StrokeSelector, Stroke>
{
   private enum Action
   {
      Deletion, 
      Unselect_All, 
      Select_All, 
      Smooth
   }
   
   private enum Mode
   {
      Single_Selection, 
      Box_Selection, 
      
      Single_Unselection, 
      Box_Unselection,
      
      Move, 
      Scale
   };
   
   private static final float SEL_BOX_LINE_WIDTH = 1;
   private static final Color SEL_BOX_LINE_COLOR = Color.RED;
   
   private StrokeSelector selector;
   private FloatPoint2D prevPoint;
   private SelectionToolBar toolBar;
   private Rectangle2D.Float selRect;
   
   private Rectangle2D.Float initScale;
   private Rectangle2D.Float deltaScale;
   
   private Vector<CopyStateListener> copyListenerVec;
   
   public StrokeSelectionCanvas(CompositeCanvas canvas)
   {
      super(canvas);
      
      this.copyListenerVec = new Vector<CopyStateListener>();
      
      this.selector = new StrokeSelector();
      this.prevPoint = null;
      this.selRect = null;
      this.toolBar = new SelectionToolBar();
      
      this.initScale = new Rectangle2D.Float();
      this.deltaScale = new Rectangle2D.Float();
   }
   
   public SelectionToolBar getToolBarButton()
   {
      return this.toolBar;
   }
   
   @Override
   public void start()
   {
   }
   
   @Override
   public void finish()
   {
   }
   
   @Override
   public void pathStartedImpl(Path path, boolean newPage)
   {
   }
   
   @Override
   public void pathFinishedImpl(Path path)
   {
      Mode curMode = this.toolBar.getCurrentMode();
      if (curMode == Mode.Box_Selection || curMode == Mode.Box_Unselection)
      {
         boolean isSelected = true;
         if (curMode == Mode.Box_Unselection)
            isSelected = false;
         
         Page curPage = getCompositeCanvas().getBinder().getCurrentPage();
         
         for (Stroke stroke : curPage)
            if (this.selRect.contains(stroke.getBounds2D()))
               curPage.setStrokeSelected(stroke, isSelected);
         
         notifyOfCopyState(checkCanCopy());
      }
      
      doRepaint();
      this.prevPoint = null;
      this.selRect = null;
   }
   
   @Override
   public void pathChangedImpl(Path path)
   {
      FloatPoint2D lastPt = path.getLast();
      Binder binder = getCompositeCanvas().getBinder();
      Page curPage = binder.getCurrentPage();
      Vector<Stroke> strokesAtPt = curPage.getStrokesAt(lastPt);
      
      Mode curMode = this.toolBar.getCurrentMode();
      
      if (curMode == Mode.Single_Selection)
      {
         for (Stroke stroke : strokesAtPt)
         {
            curPage.setStrokeSelected(stroke, true);
            paintStroke(stroke);
         }
         
         notifyOfCopyState(true);
      }
      else if (curMode == Mode.Single_Unselection)
      {
         for (Stroke stroke : strokesAtPt)
         {
            curPage.setStrokeSelected(stroke, false);
            paintStroke(stroke);
         }
         
         notifyOfCopyState(checkCanCopy());
      }
      else if (curMode == Mode.Box_Selection || 
               curMode == Mode.Box_Unselection)
      {
         FloatPoint2D first = path.getFirst();
         Page page = getCompositeCanvas().getBinder().getCurrentPage();
         
         // define the coordinates for the new box
         float boxX = Math.min(first.getX(), lastPt.getX());
         float boxY = Math.min(first.getY(), lastPt.getY());
         float boxW = Math.abs(first.getX() - lastPt.getX());
         float boxH = Math.abs(first.getY() - lastPt.getY());
         
         if (this.selRect == null)
         {
            this.selRect = new Rectangle2D.Float(boxH, boxY, boxW, boxH);
            doRepaint(page.getX() + boxX, page.getY() + boxY, 
                      boxW, boxH, 
                      SEL_BOX_LINE_WIDTH);
         }
         else
         {
            // find the coordinates of the region of the 
            // screen that should be repainted
            float repaintX = Math.min(this.selRect.x, boxX);
            float repaintY = Math.min(this.selRect.y, boxY);
            float repaintW = Math.max(this.selRect.width, boxW);
            float repaintH = Math.max(this.selRect.height, boxH);
            
            this.selRect.setRect(boxX, boxY, boxW, boxH);
            doRepaint(page.getX() + repaintX, page.getY() + repaintY, 
                      repaintW, repaintH, 
                      SEL_BOX_LINE_WIDTH);
         }
         
         // we notify the CopyStateListeners of whether or not things 
         // can be copied during the execution of the pathFinishedImpl() 
         // method
      }
      else if (curMode == Mode.Move)
      {
         if (this.prevPoint != null)
         {
            float xDiff = lastPt.getX()-this.prevPoint.getX();
            float yDiff = lastPt.getY()-this.prevPoint.getY();
            
            for (int i=0; i<curPage.getNumSelectedStrokes(); i++)
               moveStroke(curPage.getSelectedStrokeAt(i), xDiff, yDiff);
         }
      }
      else if (curMode == Mode.Scale)
      {
         if (this.prevPoint        != null && 
             this.initScale.width  != 0    && 
             this.initScale.height != 0)
         {
            float xDiff = lastPt.getX()-this.prevPoint.getX();
            float yDiff = lastPt.getY()-this.prevPoint.getY();
            
            this.deltaScale.width  += xDiff;
            this.deltaScale.height += yDiff;
            
            this.deltaScale.x += xDiff;
            this.deltaScale.y += yDiff;
            
            float xScale = (this.initScale.width+this.deltaScale.width)/
                            this.initScale.width;
            float yScale = (this.initScale.height+this.deltaScale.height)/
                            this.initScale.height;
            
            RectangleUnioner unioner = new RectangleUnioner();
            
            Stroke stroke;
            Pen pen;
            float maxWidth = 0;
            for (int i=0; i<curPage.getNumSelectedStrokes(); i++)
            {
               stroke = curPage.getSelectedStrokeAt(i);
               
               pen = stroke.getPen();
               maxWidth = Math.max(maxWidth, pen.getWidth());
               
               unioner.union(stroke.getBounds2D());
               stroke.scaleTo(xScale, yScale);
               
               unioner.union(stroke.getBounds2D());
               maxWidth = Math.max(maxWidth, pen.getWidth());
            }
            
            Rectangle2D.Float union = unioner.getUnion();
            Page page = binder.getCurrentPage();
            doRepaint((float)union.getX()+page.getX(), 
                      (float)union.getY()+page.getY(),
                      (float)union.getWidth(), 
                      (float)union.getHeight(), 
                      maxWidth);
         }
      }
      
      this.prevPoint = lastPt;
   }
   
   private boolean checkCanCopy()
   {
      boolean canCopy = false;
      for (Page p : getCompositeCanvas().getBinder())
      {
         if (p.getNumSelectedStrokes() > 0)
         {
            canCopy = true;
            break;
         }
      }
      
      return canCopy;
   }
   
   private void notifyOfCopyState(boolean canCopy)
   {
      for (CopyStateListener listener : this.copyListenerVec)
         listener.copyStateChanged(canCopy);
   }
   
   private Rectangle2D.Float getLineBounds(FloatPoint2D pt1, 
                                           FloatPoint2D pt2, 
                                           float lineWidth)
   {
      Page curPage = getCompositeCanvas().getBinder().getCurrentPage();
      
      float x = Math.min(pt1.getX()+curPage.getX(), pt2.getX()+curPage.getX());
      float y = Math.min(pt1.getY()+curPage.getY(), pt2.getY()+curPage.getY());
      float width = Math.abs(pt1.getX()-pt2.getX());
      float height = Math.abs(pt1.getY()-pt2.getY());

      return new Rectangle2D.Float(x-lineWidth, y-lineWidth, 
                                   width+2*lineWidth, height+2*lineWidth);
   }
   
   private void paintStroke(Stroke stroke)
   {
      float penWidth = stroke.getPen().getWidth();
      
      Path path = stroke.getPath();
      FloatPoint2D pt1, pt2;
      Rectangle2D.Float lineBounds;
      
      for (int i=0; i<path.getNumItems()-1; i++)
      {
         pt1 = path.getItemAt(i);
         pt2 = path.getItemAt(i+1);
         
         lineBounds = getLineBounds(pt1, pt2, penWidth);
         doRepaint(lineBounds.x, lineBounds.y, 
                   lineBounds.width, lineBounds.height, penWidth);
      }
   }
   
   private void moveStroke(Stroke stroke, 
                           float xDiff, float yDiff)
   {
      float penWidth = stroke.getPen().getWidth();
      doMoveAndPaint(stroke.getBounds2D(), xDiff, yDiff, penWidth);
      stroke.translateBy(xDiff, yDiff);
   }
   
   private void doMoveAndPaint(Rectangle2D bounds,  
                               float xDiff, float yDiff, float delta)
   {
      Page curPage = getCompositeCanvas().getBinder().getCurrentPage();
      
      float x = curPage.getX() + (float)bounds.getX();
      float y = curPage.getY() + (float)bounds.getY();
      float width = (float)bounds.getWidth();
      float height = (float)bounds.getHeight();
      
      if (xDiff > 0) // if positive stretch the box left
      {
        width += xDiff;
      }
      else // if negative stretch the box right
      {
         // Note:  xDiff is negative or 0
         
         // move the box left
         x += xDiff;
         // and stretch it right
         width -= 2*xDiff;
         
         // account for the pen's width
         width += delta;
      }
      
      if (yDiff > 0) // if positive stretch the box down
         height += yDiff;
      else // if negative stretch the box down
      {
         // Note:  yDiff is negative or 0
         
         // move the box up
         y += yDiff;
         // and stretch it down
         height -= 2*yDiff;
         
         // account for the pen's width
         height += delta;
      }
      
      doRepaint(x,  y,  width,  height, 0);
   }
   
   @Override
   public StrokeSelector getTool()
   {
      return this.selector;
   }

   public void renderInto(Renderer2D mG2d)
   {
      Mode curMode = this.toolBar.getCurrentMode();
      if (curMode == Mode.Box_Selection || curMode == Mode.Box_Unselection)
      {
         if (!isPathInProgress() || this.selRect == null)
            return;
         
         // find the current page
         Page curPage = getCompositeCanvas().getBinder().getCurrentPage();
         
         // translate to the top left corner of the current page
         mG2d.translate(curPage.getX(), curPage.getY());
         
         // render the box
         mG2d.setLineWidth(SEL_BOX_LINE_WIDTH);
         mG2d.setColor(SEL_BOX_LINE_COLOR);
         mG2d.drawRectangle( (float)this.selRect.getX(), 
                             (float)this.selRect.getY(), 
                             (float)this.selRect.getWidth(), 
                             (float)this.selRect.getHeight() );
         
         // translate back to the origin
         mG2d.translate(-curPage.getX(), -curPage.getY());
      }
   }
   
   @Override
   public void zoomBy(float val)
   {
   }

   @Override
   public void zoomTo(float val)
   {
   }
   
   @Override
   public void resizeTo(float val)
   {
   }
   
   public class SelectionToolBar 
                   extends ToolBarButton 
                              implements ActionListener, 
                                         GuiSettingsConstants, 
                                         CutCopyPasteReady<CopyVector<Stroke>>
   {
      private Mode mode;
      private Vector<PathMenuItem> menuItemVec;
      
      private JToggleButton selButton;
      private JToggleButton unselButton;
      
      private JToggleButton selBoxButton;
      private JToggleButton unselBoxButton;
      
      private JButton selAllButton;
      private JButton unselAllButton;
      
      private JButton removeButton;
      private JButton combButton;
      private JToggleButton moveButton;
      private JToggleButton scaleButton;
      
      private SizeControl sizeControl;
      private ColorControl colorControl;
      
      public SelectionToolBar()
      {
         super(DefinedIcon.select_stroke);
         
         this.selButton = 
            new JToggleButton(DefinedIcon.select.getIcon(BUTTON_SIZE));
         this.selButton.setActionCommand(Mode.Single_Selection.toString());
         this.selButton.addActionListener(this);
         
         this.unselButton = 
            new JToggleButton(DefinedIcon.unselect.getIcon(BUTTON_SIZE));
         this.unselButton.setActionCommand(Mode.Single_Unselection.toString());
         this.unselButton.addActionListener(this);
         
         this.selBoxButton = 
            new JToggleButton(DefinedIcon.box_select.getIcon(BUTTON_SIZE));
         this.selBoxButton.setActionCommand(Mode.Box_Selection.toString());
         this.selBoxButton.addActionListener(this);
         
         this.unselBoxButton = 
            new JToggleButton(DefinedIcon.box_unselect.getIcon(BUTTON_SIZE));
         this.unselBoxButton.setActionCommand(Mode.Box_Unselection.toString());
         this.unselBoxButton.addActionListener(this);
         
         this.removeButton = 
            new JButton(DefinedIcon.delete_stroke.getIcon(BUTTON_SIZE));
         this.removeButton.addActionListener(this);
         this.removeButton.setActionCommand(Action.Deletion.toString());
         
         this.combButton = 
            new JButton(DefinedIcon.paintbrush.getIcon(BUTTON_SIZE));
         this.combButton.addActionListener(this);
         this.combButton.setActionCommand(Action.Smooth.toString());
         
         this.moveButton = 
            new JToggleButton(DefinedIcon.move_stroke.getIcon(BUTTON_SIZE));
         this.moveButton.addActionListener(this);
         this.moveButton.setActionCommand(Mode.Move.toString());
         
         this.scaleButton = 
            new JToggleButton(DefinedIcon.resize_stroke.getIcon(BUTTON_SIZE));
         this.scaleButton.addActionListener(this);
         this.scaleButton.setActionCommand(Mode.Scale.toString());
         
         this.selAllButton = 
            new JButton(DefinedIcon.select_all.getIcon(BUTTON_SIZE));
         this.selAllButton.addActionListener(this);
         this.selAllButton.setActionCommand(Action.Select_All.toString());
         
         this.unselAllButton = 
            new JButton(DefinedIcon.unselect_all.getIcon(BUTTON_SIZE));
         this.unselAllButton.addActionListener(this);
         this.unselAllButton.setActionCommand(Action.Unselect_All.toString());
         
         this.sizeControl = 
                        new SizeControl("Size", 1.2, 0, 20, 0.2, 
                                        Unit.PIXEL, Style.Circle, false, 
                                        Color.BLACK, BUTTON_SIZE+BUTTON_SIZE/4, 1);
         SizeListener sizeListener = new SizeListener();
         this.sizeControl.addValueChangeListener(sizeListener);
         this.sizeControl.getDecoratedButton().
                             addActionListener(sizeListener);
         
         this.colorControl = new ColorControl(Color.BLACK, BUTTON_SIZE+BUTTON_SIZE/4);
         
         ColorListener colorListener = new ColorListener();
         this.colorControl.addValueChangeListener(colorListener);
         this.colorControl.getDecoratedButton().
                              addActionListener(colorListener);
         
         ButtonGroup selUnselGroup = new ButtonGroup();
         selUnselGroup.add(this.selButton);
         selUnselGroup.add(this.unselButton);
         selUnselGroup.add(this.selBoxButton);
         selUnselGroup.add(this.unselBoxButton);
         selUnselGroup.add(this.moveButton);
         selUnselGroup.add(this.scaleButton);
         
         JToolBar selToolBar = new JToolBar();
         selToolBar.add(this.selButton);
         selToolBar.add(this.selBoxButton);
         selToolBar.add(this.selAllButton);
         selToolBar.addSeparator();
         
         selToolBar.add(this.unselButton);
         selToolBar.add(this.unselBoxButton);
         selToolBar.add(this.unselAllButton);
         
         JToolBar editToolBar = new JToolBar();
         editToolBar.add(this.removeButton);
         editToolBar.add(this.combButton);
         editToolBar.add(this.moveButton);
         editToolBar.add(this.scaleButton);
         editToolBar.addSeparator();
         
         editToolBar.add(this.colorControl);
         editToolBar.addSeparator();
         editToolBar.add(this.sizeControl);
         
         CutCopyPasteToolBar<CopyVector<Stroke>> copyToolbar = 
                                                    new CutCopyPasteToolBar(this);
         copyToolbar.setFloatable(true);
         
         SlidingPanel slidePanel = new SlidingPanel();
         slidePanel.append(selToolBar);
         slidePanel.append(editToolBar);
         slidePanel.append(copyToolbar);
         
         JToolBar toolBarPanel = getToolBar();
         toolBarPanel.add(slidePanel);
         
         // set up the menus
         this.menuItemVec = new Vector<PathMenuItem>();
         
         // create the menu items
         MenuListener listener = new MenuListener();
         
         ButtonGroup group = new ButtonGroup();
         
         JCheckBoxMenuItem selItem = 
               new JCheckBoxMenuItem("Select", 
                                     DefinedIcon.select.getIcon(16));
         selItem.setActionCommand(Mode.Single_Selection.toString());
         selItem.addActionListener(listener);
         group.add(selItem);
         
         JCheckBoxMenuItem deSelItem = 
               new JCheckBoxMenuItem("Deselect", 
                                     DefinedIcon.unselect.getIcon(16));
         deSelItem.setActionCommand(Mode.Single_Unselection.toString());
         deSelItem.addActionListener(listener);
         group.add(deSelItem);
         
         JCheckBoxMenuItem deleteItem = 
               new JCheckBoxMenuItem("Delete", 
                                     DefinedIcon.delete_stroke.getIcon(16));
         deleteItem.setActionCommand(Action.Deletion.toString());
         deleteItem.addActionListener(listener);
         group.add(deleteItem);
         
         JCheckBoxMenuItem moveItem = 
               new JCheckBoxMenuItem("Move", 
                                     DefinedIcon.move_stroke.getIcon(16));
         moveItem.setActionCommand(Mode.Move.toString());
         moveItem.addActionListener(listener);
         group.add(moveItem);
         
         //set up the path
         MenuPath selPath = new MenuPath(MenuConstants.SELECT_MENU_PATH, 
                                         "Stroke Editor", 
                                         DefinedIcon.copy_stroke.
                                                        getIcon(16));
         
         this.menuItemVec.add(new PathMenuItem(new JLabel(" Mode"), selPath));
         this.menuItemVec.add(new PathMenuItem(new JSeparator(), selPath));
         
         this.menuItemVec.add(new PathMenuItem(selItem, selPath));
         this.menuItemVec.add(new PathMenuItem(deSelItem, selPath));
         this.menuItemVec.add(new PathMenuItem(new JSeparator(), selPath));
         this.menuItemVec.add(new PathMenuItem(deleteItem, selPath));
         this.menuItemVec.add(new PathMenuItem(moveItem, selPath));
         
         this.selButton.doClick();
         selItem.setSelected(true);
      }
      
      public Mode getCurrentMode()
      {
         return this.mode;
      }
      
      @Override
      public SubCanvas getCanvas()
      {
         return StrokeSelectionCanvas.this;
      }
      
      public void start()
      {
         this.selButton.doClick();
      }
      
      public void finish()
      {
         for (Page page : getCompositeCanvas().getBinder())
            page.setAllStrokeSelected(false);
         
         doRepaint();
      }

      public List<PathMenuItem> getPathMenuItems()
      {
         return this.menuItemVec;
      }

      public void actionPerformed(ActionEvent e)
      {
         String cmmd = e.getActionCommand();
         
         Mode[] modes = Mode.values();
         for (Mode m : modes)
         {
            if (cmmd.equals(m.toString()))
            {
               this.mode = m;
               break;
            }
         }
         
         if (cmmd.equals(Action.Smooth.toString()))
         {
            CompositeCanvas canvas = getCompositeCanvas();
            
            // the region of the session that needs to be repainted 
            // because the strokes in that region have been combed
            RectangleUnioner dirtyUnioner = new RectangleUnioner();
            
            Stroke curStroke;
            float maxWidth = 0;
            for (Page page : canvas.getBinder())
            {
               for (int i=0; i<page.getNumSelectedStrokes(); i++)
               {
                  curStroke = page.getSelectedStrokeAt(i);
                  if (curStroke == null)
                     continue;
                  
                  // add the stroke's current bounding box to the dirty region
                  dirtyUnioner.union(curStroke.getBounds2D());
                  
                  curStroke.getPath().smooth(SettingsUtilities.getSmoothFactor());
                  maxWidth = Math.max(maxWidth, curStroke.getPen().getWidth());
                  
                  // add the stroke's new bounding box to the dirty region
                  dirtyUnioner.union(curStroke.getBounds2D());
               }
            }
            
            // repaint only the dirty region
            Rectangle2D dirtyRect = dirtyUnioner.getUnion();
            
            // The dirty region is the region relative to the current page.
            // Thus we have to shift the region as if the origin is at 
            // the top left corner of the current page
            Binder binder = getCompositeCanvas().getBinder();
            Page curPage = binder.getCurrentPage();
            
            float x = (float)(dirtyRect.getX()+curPage.getX());
            float y = (float)(dirtyRect.getY()+curPage.getY());
            
            // repaint the dirty region
            doRepaint( x, 
                       y, 
                      (float)dirtyRect.getWidth(), 
                      (float)dirtyRect.getHeight(), 
                      maxWidth);
         }
         else if (cmmd.equals(Action.Deletion.toString()))
         {
            // record the strokes that are deleted for the 
            // undo/redo manager
            Hashtable<Page, Vector<Stroke>> deletedStrokeTable = 
                               new Hashtable<Page, Vector<Stroke>>();
            
            CompositeCanvas canvas = getCompositeCanvas();
            
            // the region of the session that needs to be repainted 
            // because the strokes in that region have been deleted
            RectangleUnioner dirtyUnioner = new RectangleUnioner();
            
            for (Page page : canvas.getBinder())
            {
               Vector<Stroke> selStrokeVec = page.getSelectedStrokesCopy();
               deletedStrokeTable.put(page, selStrokeVec);
               
               Stroke selStroke;
               for (int i=selStrokeVec.size()-1; i>=0; i--)
               {
                  selStroke = selStrokeVec.elementAt(i);
                  page.removeStroke(selStroke);
                  
                  // add the stroke's bounding box to the dirty region
                  dirtyUnioner.union(selStroke.getBounds2D());
               }
            }
            
            DeletePagedStrokeAction actionDone = 
               new DeletePagedStrokeAction(canvas, deletedStrokeTable);
            DrawPagedStrokeAction undoAction = 
               new DrawPagedStrokeAction(canvas, deletedStrokeTable);
            canvas.getUndoRedoManager().actionDone(actionDone, undoAction);
            
            // repaint only the dirty region
            Rectangle2D dirtyRect = dirtyUnioner.getUnion();
            
            // The dirty region is the region relative to the current page.
            // Thus we have to shift the region as if the origin is at 
            // the top left corner of the current page
            Binder binder = getCompositeCanvas().getBinder();
            Page curPage = binder.getCurrentPage();
            
            float x = (float)(dirtyRect.getX()+curPage.getX());
            float y = (float)(dirtyRect.getY()+curPage.getY());
            
            // repaint the dirty region
            doRepaint( x, 
                       y, 
                      (float)dirtyRect.getWidth(), 
                      (float)dirtyRect.getHeight(), 0);
            
            notifyOfCopyState(checkCanCopy());
         }
         else if (cmmd.equals(Mode.Scale.toString()))
         {
            RectangleUnioner unioner = new RectangleUnioner();
            Page curPage = getCompositeCanvas().getBinder().getCurrentPage();
            for (int i=0; i<curPage.getNumSelectedStrokes(); i++)
               unioner.union(curPage.getSelectedStrokeAt(i).getBounds2D());
            
            Rectangle2D union = unioner.getUnion();
            
            initScale.setRect(union);
            deltaScale.setRect(0, 0, 0, 0);
         }
         else if (cmmd.equals(Action.Select_All.toString()))
         {
            getCompositeCanvas().getBinder().setAllStrokeSelected(true);
            // CHANGED:  Notice the whole screen is repainted here
            doRepaint();
            
            notifyOfCopyState(checkCanCopy());
         }
         else if (cmmd.equals(Action.Unselect_All.toString()))
         {
            getCompositeCanvas().getBinder().setAllStrokeSelected(false);
            // CHANGED:  Notice the whole screen is repainted here
            doRepaint();
            
            notifyOfCopyState(checkCanCopy());
         }
      }
      
      private class MenuListener implements ActionListener
      {
         public void actionPerformed(ActionEvent event)
         {
            doClick();
            
            String cmmd = event.getActionCommand();
            if (cmmd.equals(Mode.Single_Selection.toString()))
               selButton.doClick();
            else if (cmmd.equals(Mode.Single_Unselection.toString()))
               unselButton.doClick();
            else if (cmmd.equals(Action.Deletion.toString()))
               removeButton.doClick();
            else if (cmmd.equals(Mode.Move.toString()))
               moveButton.doClick();
         }
      }
      
      private class SizeListener 
                       implements ValueChangeListener<MValue, SizeControl>, 
                                  ActionListener
      {
         private void updateSize()
         {
            float value = (float)sizeControl.getControlValue().
                                                getValue(Unit.PIXEL);
            
            // the region of the session that needs to be repainted 
            // because the strokes in that region have been resized
            RectangleUnioner dirtyUnioner = new RectangleUnioner();
            
            Binder binder = getCompositeCanvas().getBinder();
            for (Page page : binder)
            {
               Vector<Stroke> selStrokeVec = page.getSelectedStrokesCopy();
               Stroke selStroke;
               for (int i=selStrokeVec.size()-1; i>=0; i--)
               {
                  selStroke = selStrokeVec.elementAt(i);
                  selStroke.getPen().setWidth(value);
                  
                  // add the stroke's bounding box to the dirty region
                  dirtyUnioner.union(selStroke.getBounds2D());
               }
            }
            
            // repaint only the dirty region
            Rectangle2D dirtyRect = dirtyUnioner.getUnion();
            Page page = binder.getCurrentPage();
            doRepaint((float)dirtyRect.getX()+page.getX(), 
                      (float)dirtyRect.getY()+page.getY(), 
                      (float)dirtyRect.getWidth(), 
                      (float)dirtyRect.getHeight(), 0);
         }
         
         public void valueChanged(ValueChangeEvent<MValue, 
                                                   SizeControl> event)
         {
            updateSize();
         }

         public void actionPerformed(ActionEvent e)
         {
            updateSize();
         }
      }
      
      private class ColorListener 
                       implements ValueChangeListener<Color, ColorControl>, 
                                  ActionListener
      {
         private void updateColor()
         {
            Color color = colorControl.getControlValue();
            
            // the region of the session that needs to be repainted 
            // because the strokes in that region have been repainted
            RectangleUnioner dirtyUnioner = new RectangleUnioner();
            
            Binder binder = getCompositeCanvas().getBinder();
            for (Page page : binder)
            {
               Vector<Stroke> selStrokeVec = page.getSelectedStrokesCopy();
               Stroke selStroke;
               for (int i=selStrokeVec.size()-1; i>=0; i--)
               {
                  selStroke = selStrokeVec.elementAt(i);
                  selStroke.getPen().setColor(color);
                  
                  // add the stroke's bounding box to the dirty region
                  dirtyUnioner.union(selStroke.getBounds2D());
               }
            }
            
            // repaint only the dirty region
            Rectangle2D dirtyRect = dirtyUnioner.getUnion();
            Page page = binder.getCurrentPage();
            doRepaint((float)dirtyRect.getX()+page.getX(), 
                      (float)dirtyRect.getY()+page.getY(), 
                      (float)dirtyRect.getWidth(), 
                      (float)dirtyRect.getHeight(), 0);
         }
         
         public void valueChanged(ValueChangeEvent<Color, ColorControl> event)
         {
            updateColor();
         }

         public void actionPerformed(ActionEvent e)
         {
            updateColor();
         }
      }

      public CopyVector<Stroke> copy()
      {
         RectangleUnioner unioner = new RectangleUnioner();
         float maxWidth = 0;
         Rectangle2D.Float curBounds;
         
         CopyVector<Stroke> selStrokes = new CopyVector<Stroke>();
         Binder binder = getCompositeCanvas().getBinder();
         Stroke curStroke;
         for (Page p : binder)
         {
            for (int i=0; i<p.getNumSelectedStrokes(); i++)
            {
               curStroke = p.getSelectedStrokeAt(i);
               selStrokes.add(curStroke.getCopy());
               
               curBounds = curStroke.getBounds2D();
               maxWidth = Math.max(maxWidth, curStroke.getPen().getWidth());
               unioner.union(
                          new Rectangle2D.Float(p.getX()+(float)curBounds.getX(), 
                                                p.getY()+(float)curBounds.getY(),
                                                (float)curBounds.getWidth(),
                                                (float)curBounds.getHeight()));
            }
         }
         
         Rectangle2D.Float union = unioner.getUnion();
         doRepaint((float)union.getX(), 
                   (float)union.getY(), 
                   (float)union.getWidth(), 
                   (float)union.getHeight(), 
                   maxWidth);
         
         binder.setAllStrokeSelected(false);
         
         return selStrokes;
      }

      public CopyVector<Stroke> cut()
      {
         RectangleUnioner dirtyRegion = new RectangleUnioner();
         
         CopyVector<Stroke> selStrokes = new CopyVector<Stroke>();
         CompositeCanvas canvas = getCompositeCanvas();
         Binder binder = canvas.getBinder();
         Stroke curStroke;
         Rectangle2D.Float bounds;
         float maxWidth = 0;
         for (Page p : binder)
         {
            for (int i=p.getNumSelectedStrokes()-1; i>=0; i--)
            {
               curStroke = p.getSelectedStrokeAt(i);
               selStrokes.add(curStroke);
               bounds = curStroke.getBounds2D();
               dirtyRegion.union(
                              new Rectangle2D.Float((float)(p.getX()+bounds.getX()), 
                                                    (float)(p.getY()+bounds.getY()), 
                                                    (float)bounds.getWidth(), 
                                                    (float)bounds.getHeight()));
               p.removeStroke(curStroke);
               
               maxWidth = Math.max(maxWidth, curStroke.getPen().getWidth());
            }
         }
         
         Rectangle2D.Float union = dirtyRegion.getUnion();
         doRepaint((float)union.getX(), 
                   (float)union.getY(), 
                   (float)union.getWidth(), 
                   (float)union.getHeight(), 
                   (float)maxWidth);
         
         return selStrokes;
      }

      public void paste(CopyVector<Stroke> item)
      {
         if (item == null)
            throw new NullPointerException();
         
         Page curPage = getCompositeCanvas().getBinder().getCurrentPage();
         RectangleUnioner unioner = new RectangleUnioner();
         float maxWidth = 0;
         
         for (Stroke stroke : item)
         {
            curPage.addStroke(stroke);
            curPage.setStrokeSelected(stroke, true);
            unioner.union(stroke.getBounds2D());
            maxWidth = Math.max(maxWidth, stroke.getPen().getWidth());
         }
         
         Rectangle2D.Float union = unioner.getUnion();
         doRepaint( curPage.getX()+(float)union.getX(), 
                    curPage.getY()+(float)union.getY(), 
                    (float)union.getWidth(), (float)union.getHeight(), 
                    maxWidth);
      }

      public void addCopyStateListener(CopyStateListener listener)
      {
         if (listener == null)
            throw new NullPointerException();
         
         if (!copyListenerVec.contains(listener))
            copyListenerVec.add(listener);
      }

      public void removeCopyStateListener(CopyStateListener listener)
      {
         if (listener == null)
            throw new NullPointerException();
         
         copyListenerVec.remove(listener);
      }
   }
}
