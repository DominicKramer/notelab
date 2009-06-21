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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.JWindow;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.ToolBarButton;
import noteLab.gui.Tooltips;
import noteLab.gui.button.IconButton;
import noteLab.gui.button.IconToggleButton;
import noteLab.gui.control.drop.ButtonComboButton;
import noteLab.gui.control.drop.ColorControl;
import noteLab.gui.control.drop.SizeControl;
import noteLab.gui.control.drop.pic.PrimitivePic.Style;
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
      Scale;
      
      private Mode getInverse()
      {
         switch (this)
         {
            case Single_Selection:
                 return Single_Unselection;
            case Box_Selection:
                 return Box_Unselection;
            case Single_Unselection:
                 return Single_Selection;
            case Box_Unselection:
                 return Box_Selection;
            default:
                 return this;
         }
      }
   };
   
   private static final float SEL_BOX_LINE_WIDTH = 1;
   private static final Color SEL_BOX_LINE_COLOR = Color.RED;
   
   private StrokeSelector selector;
   private FloatPoint2D prevPoint;
   private SelectionToolBar toolBar;
   private Rectangle2D.Float selRect;
   
   private Rectangle2D.Float initScale;
   
   private Vector<CopyStateListener> copyListenerVec;
   
   // 'null' if there is no item to copy and 
   // non-null if there is.
   private CopyVector<Stroke> copiedItem;
   
   public StrokeSelectionCanvas(CompositeCanvas canvas)
   {
      super(canvas, true);
      
      this.copyListenerVec = new Vector<CopyStateListener>();
      
      this.selector = new StrokeSelector();
      this.prevPoint = null;
      this.selRect = null;
      this.toolBar = new SelectionToolBar();
      this.copiedItem = null;
      
      this.initScale = new Rectangle2D.Float();
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
   public boolean getRenderBinder()
   {
      return true;
   }
   
   @Override
   public void pathStartedImpl(Path path, MouseButton button, boolean newPage)
   {
   }
   
   @Override
   public void pathFinishedImpl(Path path, MouseButton button)
   {
      Mode curMode = this.toolBar.getCurrentMode();
      
      // If the right mouse button is being used, 
      // use the opposite action.  This feature allows 
      // the user to temporarily switch to the opposite 
      // action without having to return to the toolbar.
      if (button == MouseButton.Button3)
         curMode = curMode.getInverse();
      
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
   public void pathChangedImpl(Path path, MouseButton button)
   {
      FloatPoint2D lastPt = path.getLast();
      Binder binder = getCompositeCanvas().getBinder();
      Page curPage = binder.getCurrentPage();
      
      if (this.copiedItem != null)
      {
         RectangleUnioner unioner = new RectangleUnioner();
         float maxWidth = 0;
         
         if (this.copiedItem.isEmpty())
            return;
         
         float scaleLevel = getCompositeCanvas().getZoomLevel();
         
         Rectangle2D.Float bounds;
         float minX = Float.MAX_VALUE;
         float minY = Float.MAX_VALUE;
         for (Stroke stroke : this.copiedItem)
         {
            // scale the strokes if the canvas has been scaled
            stroke.scaleTo(scaleLevel, scaleLevel);
            
            bounds = stroke.getBounds2D();
            minX = (float)Math.min(minX, bounds.getMinX());
            minY = (float)Math.min(minY, bounds.getMinY());
         }
         
         float xDiff = lastPt.getX()-minX;
         float yDiff = lastPt.getY()-minY;
         
         for (Stroke stroke : this.copiedItem)
         {
            // move the stroke to its new position
            stroke.translateBy(xDiff, yDiff);
            
            // the strokes have already been scaled above
            
            // add the stroke to the page
            curPage.addStroke(stroke);
            curPage.setStrokeSelected(stroke, true);
            
            // find the region of the canvas to repaint
            // by getting the stroke's new bounds
            // (the new bounds are needed since the stroke 
            // has been translated).
            unioner.union(stroke.getBounds2D());
            maxWidth = Math.max(maxWidth, stroke.getPen().getWidth());
         }
         
         Rectangle2D.Float union = unioner.getUnion();
         doRepaint( curPage.getX()+(float)union.getX(), 
                    curPage.getY()+(float)union.getY(), 
                    (float)union.getWidth(), (float)union.getHeight(), 
                    maxWidth);
         
         this.copiedItem = null;
         this.toolBar.moveButton.doClick();
         return;
      }
      
      Vector<Stroke> strokesAtPt = curPage.getStrokesAt(lastPt);
      Mode curMode = this.toolBar.getCurrentMode();
      
      // If the right mouse button is being used, 
      // use the opposite action.  This feature allows 
      // the user to temporarily switch to the opposite 
      // action without having to return to the toolbar.
      if (button == MouseButton.Button3)
         curMode = curMode.getInverse();
      
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
            float newWidth  = this.initScale.width+lastPt.getX()-this.prevPoint.getX();
            float newHeight = this.initScale.height+lastPt.getY()-this.prevPoint.getY();
            
            float xScale = newWidth/this.initScale.width;
            float yScale = newHeight/this.initScale.height;
            
            this.initScale.width = newWidth;
            this.initScale.height = newHeight;
            
            float initX = this.initScale.x;
            float initY = this.initScale.y;
            
            RectangleUnioner unioner = new RectangleUnioner();
            
            Stroke stroke;
            Pen pen;
            float curWidth;
            float maxWidth = 0;
            for (int i=0; i<curPage.getNumSelectedStrokes(); i++)
            {
               stroke = curPage.getSelectedStrokeAt(i);
               pen = stroke.getPen();
               curWidth = pen.getWidth();
               
               maxWidth = Math.max(maxWidth, curWidth);
               
               stroke.translateBy(-initX, -initY);
               unioner.union(stroke.getBounds2D());
               
               stroke.resizeTo(xScale, yScale);
               unioner.union(stroke.getBounds2D());
               
               stroke.translateBy(initX, initY);
               pen.setRawWidth(curWidth);
            }
            
            Rectangle2D.Float union = unioner.getUnion();
            Page page = binder.getCurrentPage();
            doRepaint((float)union.getX()+page.getX()+initX, 
                      (float)union.getY()+page.getY()+initY,
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

   public void renderInto(Renderer2D overlayDisplay, Renderer2D mG2d)
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
      
      private ButtonComboButton selCombo;
      
      private IconToggleButton selButton;
      private IconToggleButton unselButton;
      
      private IconToggleButton selBoxButton;
      private IconToggleButton unselBoxButton;
      
      private IconButton selAllButton;
      private IconButton unselAllButton;
      
      private IconButton removeButton;
      private IconButton combButton;
      private IconToggleButton moveButton;
      private IconToggleButton scaleButton;
      
      private SizeControl sizeControl;
      private ColorControl colorControl;
      
      public SelectionToolBar()
      {
         super(DefinedIcon.select_stroke);
         
         setToolTipText(Tooltips.STROKE_EDITING_MODE);
         
         this.selButton = 
                 new IconToggleButton(DefinedIcon.select, BUTTON_SIZE);
         this.selButton.setActionCommand(Mode.Single_Selection.toString());
         this.selButton.addActionListener(this);
         this.selButton.setToolTipText(Tooltips.STROKE_SELECT);
         
         this.unselButton = 
                 new IconToggleButton(DefinedIcon.unselect, BUTTON_SIZE);
         this.unselButton.setActionCommand(Mode.Single_Unselection.toString());
         this.unselButton.addActionListener(this);
         this.unselButton.setToolTipText(Tooltips.STROKE_UNSELECT);
         
         this.selBoxButton = 
                 new IconToggleButton(DefinedIcon.box_select, BUTTON_SIZE);
         this.selBoxButton.setActionCommand(Mode.Box_Selection.toString());
         this.selBoxButton.addActionListener(this);
         this.selBoxButton.setToolTipText(Tooltips.REGION_SELECT);
         
         this.unselBoxButton = 
                 new IconToggleButton(DefinedIcon.box_unselect, BUTTON_SIZE);
         this.unselBoxButton.setActionCommand(Mode.Box_Unselection.toString());
         this.unselBoxButton.addActionListener(this);
         this.unselBoxButton.setToolTipText(Tooltips.REGION_UNSELECT);
         
         this.removeButton = 
                 new IconButton(DefinedIcon.delete_stroke, BUTTON_SIZE);
         this.removeButton.setActionCommand(Action.Deletion.toString());
         this.removeButton.setToolTipText(Tooltips.STROKE_DELETE);
         // Don't directly listen to this button.  Instead, listen to 
         // the drop down button that contains this button.  That way if 
         // the user selects this button from the drop down list, strokes 
         // won't be deleted.  However, when he/she selects the drop down 
         // button, strokes will then be deleted.
         // However, we still must set the button's action command since 
         // the drop down button sends this action command.
         // this.removeButton.addActionListener(this);
         
         this.combButton = 
                 new IconButton(DefinedIcon.paintbrush, BUTTON_SIZE);
         this.combButton.setActionCommand(Action.Smooth.toString());
         this.combButton.setToolTipText(Tooltips.STROKE_SMOOTH);
         // Don't directly listen to this button.  Instead, listen to 
         // the drop down button that contains this button.  That way if 
         // the user selects this button from the drop down list, strokes 
         // won't be smoothed.  However, when he/she selects the drop down 
         // button, smoothed will then be deleted.
         // However, we still must set the button's action command since 
         // the drop down button sends this action command.
         // this.combButton.addActionListener(this);
         
         this.moveButton = 
                 new IconToggleButton(DefinedIcon.move_stroke, BUTTON_SIZE);
         this.moveButton.addActionListener(this);
         this.moveButton.setActionCommand(Mode.Move.toString());
         this.moveButton.setToolTipText(Tooltips.STROKE_MOVE);
         
         this.scaleButton = 
                 new IconToggleButton(DefinedIcon.resize_stroke, BUTTON_SIZE);
         this.scaleButton.addActionListener(this);
         this.scaleButton.setActionCommand(Mode.Scale.toString());
         this.scaleButton.setToolTipText(Tooltips.STROKE_RESIZE);
         
         this.selAllButton = 
                 new IconButton(DefinedIcon.select_all, BUTTON_SIZE);
         this.selAllButton.addActionListener(this);
         this.selAllButton.setActionCommand(Action.Select_All.toString());
         this.selAllButton.setToolTipText(Tooltips.STROKE_SELECT_ALL);
         
         this.unselAllButton = 
                 new IconButton(DefinedIcon.unselect_all, BUTTON_SIZE);
         this.unselAllButton.addActionListener(this);
         this.unselAllButton.setActionCommand(Action.Unselect_All.toString());
         this.unselAllButton.setToolTipText(Tooltips.STROKE_UNSELECT_ALL);
         
         this.sizeControl = 
                        new SizeControl("Size", 1.2, 0, 20, 0.2, 
                                        Unit.PIXEL, Style.Circle, false, 
                                        Color.BLACK, 1);
         SizeListener sizeListener = new SizeListener();
         this.sizeControl.addValueChangeListener(sizeListener);
         this.sizeControl.addActionListener(sizeListener);
         this.sizeControl.setToolTipText(Tooltips.STROKE_SIZE);
         
         this.colorControl = new ColorControl(Color.BLACK);
         
         ColorListener colorListener = new ColorListener();
         this.colorControl.addValueChangeListener(colorListener);
         this.colorControl.addActionListener(colorListener);
         this.colorControl.setToolTipText(Tooltips.STROKE_COLOR);
         
         ButtonGroup selUnselGroup = new ButtonGroup();
         selUnselGroup.add(this.selButton);
         selUnselGroup.add(this.unselButton);
         selUnselGroup.add(this.selBoxButton);
         selUnselGroup.add(this.unselBoxButton);
         selUnselGroup.add(this.moveButton);
         selUnselGroup.add(this.scaleButton);
         
         this.selCombo = new ButtonComboButton(DefinedIcon.select);
         this.selCombo.setToolTipText(Tooltips.PAGE_ACTIVE_EDITING_TOOL);
         this.selCombo.addActionListener(this);
         this.selCombo.registerButton(this.selButton);
         this.selCombo.registerButton(this.selBoxButton);
         this.selCombo.registerButton(this.selAllButton);
         this.selCombo.registerButton(this.unselButton);
         this.selCombo.registerButton(this.unselBoxButton);
         this.selCombo.registerButton(this.unselAllButton);
         this.selCombo.registerButton(this.moveButton);
         this.selCombo.registerButton(this.scaleButton);
         this.selCombo.registerButton(this.removeButton);
         this.selCombo.registerButton(this.combButton);
         
         JPanel singlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
         singlePanel.add(this.selButton);
         singlePanel.add(this.unselButton);
         
         JPanel boxPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
         boxPanel.add(this.selBoxButton);
         boxPanel.add(this.unselBoxButton);
         
         JPanel allPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
         allPanel.add(this.selAllButton);
         allPanel.add(this.unselAllButton);
         
         JPanel transPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
         transPanel.add(this.moveButton);
         transPanel.add(this.scaleButton);
         
         JPanel editPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
         editPanel.add(this.removeButton);
         editPanel.add(this.combButton);
         
         JWindow selPopup = this.selCombo.getPopupWindow();
         selPopup.setLayout(new GridLayout(5, 1));
         selPopup.add(singlePanel);
         selPopup.add(boxPanel);
         selPopup.add(allPanel);
         selPopup.add(transPanel);
         selPopup.add(editPanel);
         
         JToolBar toolbar = getToolBar();
         
         toolbar.add(this.selCombo);
         toolbar.addSeparator();
         
         toolbar.add(this.colorControl);
         toolbar.addSeparator();
         toolbar.add(this.sizeControl);
         
         CutCopyPasteToolBar<CopyVector<Stroke>> copyToolbar = 
                               new CutCopyPasteToolBar(this);
         copyToolbar.setCutToolTipText(Tooltips.STROKE_CUT);
         copyToolbar.setCopyToolTipText(Tooltips.STROKE_COPY);
         copyToolbar.setPasteToolTipText(Tooltips.STROKE_PASTE);
         copyToolbar.appendTo(toolbar);
         
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
         selItem.setToolTipText(Tooltips.STROKE_SELECT);
         group.add(selItem);
         
         JCheckBoxMenuItem deSelItem = 
               new JCheckBoxMenuItem("Deselect", 
                                     DefinedIcon.unselect.getIcon(16));
         deSelItem.setActionCommand(Mode.Single_Unselection.toString());
         deSelItem.addActionListener(listener);
         deSelItem.setToolTipText(Tooltips.STROKE_UNSELECT);
         group.add(deSelItem);
         
         JCheckBoxMenuItem deleteItem = 
               new JCheckBoxMenuItem("Delete", 
                                     DefinedIcon.delete_stroke.getIcon(16));
         deleteItem.setActionCommand(Action.Deletion.toString());
         deleteItem.addActionListener(listener);
         deleteItem.setToolTipText(Tooltips.STROKE_DELETE);
         group.add(deleteItem);
         
         JCheckBoxMenuItem moveItem = 
               new JCheckBoxMenuItem("Move", 
                                     DefinedIcon.move_stroke.getIcon(16));
         moveItem.setActionCommand(Mode.Move.toString());
         moveItem.addActionListener(listener);
         moveItem.setToolTipText(Tooltips.STROKE_MOVE);
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
         // Don't select any buttons since the previous button that 
         // was used should still be active.  It is annoying if the 
         // select button is always selected by default.
         // this.selButton.doClick();
      }
      
      public void finish()
      {
         for (Page page : getCompositeCanvas().getBinder())
            page.setAllStrokeSelected(false);
         
         this.selCombo.setPopupVisible(false);
         this.sizeControl.setPopupVisible(false);
         this.colorControl.setPopupVisible(false);
         
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
            
            // Get the number that describes how to smooth the stroke.
            // The larger the number the more the stroke is smoothed.
            // This number is what the user has selected in the 
            // settings dialog.
            int smoothFactor = SettingsUtilities.getSmoothFactor();
            
            // If the smooth factor is 0 or negative, set it to 1.  
            // That is if the user has selected in the settings 
            // dialog not to smooth strokes, this button should 
            // still slightly smooth strokes when pressed.
            if (smoothFactor <= 0)
               smoothFactor = 1;
            
            Stroke curStroke;
            float maxWidth = 0;
            Path path;
            for (Page page : canvas.getBinder())
            {
               for (int i=0; i<page.getNumSelectedStrokes(); i++)
               {
                  curStroke = page.getSelectedStrokeAt(i);
                  if (curStroke == null)
                     continue;
                  
                  // add the stroke's current bounding box to the dirty region
                  dirtyUnioner.union(curStroke.getBounds2D());
                  
                  path = curStroke.getPath();
                  path.smooth(smoothFactor);
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
            deleteStrokes(getSelectedStrokeTable());
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
         
         if (selStrokes.isEmpty())
            return null;
         
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
         
         if (selStrokes.isEmpty())
            return null;
         
         return selStrokes;
      }
      
      // Called by the CutCopyPasteToolbar when the paste 
      // button is pressed.
      public void paste(CopyVector<Stroke> item)
      {
         if (item == null)
            throw new NullPointerException();
         
         copiedItem = item;
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
