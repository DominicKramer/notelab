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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.ToolBarButton;
import noteLab.gui.control.drop.ColorControl;
import noteLab.gui.control.drop.SizeControl;
import noteLab.gui.control.drop.TriControl;
import noteLab.gui.control.drop.pic.PrimitivePic.Style;
import noteLab.gui.listener.SelectionChangeEvent;
import noteLab.gui.listener.SelectionChangeListener;
import noteLab.gui.listener.ValueChangeEvent;
import noteLab.gui.listener.ValueChangeListener;
import noteLab.gui.menu.MenuConstants;
import noteLab.gui.menu.MenuPath;
import noteLab.gui.menu.PathMenuItem;
import noteLab.gui.settings.constants.PenSettingsConstants;
import noteLab.model.Page;
import noteLab.model.Path;
import noteLab.model.Stroke;
import noteLab.model.binder.Binder;
import noteLab.model.geom.FloatPoint2D;
import noteLab.model.tool.Pen;
import noteLab.util.geom.RectangleUnioner;
import noteLab.util.geom.unit.MValue;
import noteLab.util.geom.unit.Unit;
import noteLab.util.render.Renderer2D;
import noteLab.util.settings.SettingsChangedEvent;
import noteLab.util.settings.SettingsChangedListener;
import noteLab.util.settings.SettingsKeys;
import noteLab.util.settings.SettingsManager;
import noteLab.util.settings.SettingsUtilities;
import noteLab.util.undoRedo.action.DeleteStrokeAction;
import noteLab.util.undoRedo.action.DrawStrokeAction;

public class SrokeCanvas extends SubCanvas<Pen, Stroke>
{
   private enum Mode
   {
      Write, 
      Delete
   }
   
   private Pen pen;
   private PenToolBar toolBar;
   private Stroke curStroke;
   
   public SrokeCanvas(CompositeCanvas canvas)
   {
      super(canvas, true);
      
      this.pen = new Pen(canvas.getZoomLevel());
      
      this.curStroke = null;
      this.toolBar = new PenToolBar();
   }
   
   public PenToolBar getToolBarButton()
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
   public void pathStartedImpl(Path path, MouseButton button, boolean newPage)
   {
      //if (button == MouseButton.Button1)
      //   this.toolBar.setCurrentMode(Mode.Write);
      if (button == MouseButton.Button3)
         this.toolBar.setCurrentMode(Mode.Delete);
   }
   
   @Override
   public void pathFinishedImpl(Path path, MouseButton button)
   {
      if (this.toolBar.getCurrentMode() == Mode.Write)
      {
         CompositeCanvas canvas = getCompositeCanvas();
         final Page page = canvas.getBinder().getCurrentPage();
         
         DrawStrokeAction actionDone = 
                     new DrawStrokeAction(canvas, this.curStroke, page);
         DeleteStrokeAction undoAction = 
                     new DeleteStrokeAction(canvas, this.curStroke, page);
         canvas.getUndoRedoManager().actionDone(actionDone, undoAction);
         
         final Stroke rawCurStroke = this.curStroke;
         this.curStroke = null;
         
         new Thread(new Runnable()
         {
            public void run()
            {
               RectangleUnioner unioner = new RectangleUnioner();
               unioner.union(rawCurStroke.getBounds2D());
               
               Path path = rawCurStroke.getPath();
               path.simplify(rawCurStroke.getPen().getWidth());
               path.smooth(SettingsUtilities.getSmoothFactor());
               
               unioner.union(rawCurStroke.getBounds2D());
               
               Rectangle2D bounds = unioner.getUnion();
               
               float x = (float)bounds.getX()+page.getX();
               float y = (float)bounds.getY()+page.getY();
               float width = (float)bounds.getWidth();
               float height = (float)bounds.getHeight();
               float delta = rawCurStroke.getPen().getWidth();
               
               doRepaint(x, y, width, height, delta);
            }
         }).start();
      }
      
      setRenderBinder(true);
      this.toolBar.syncMode();
   }
   
   @Override
   public void pathChangedImpl(Path path, MouseButton button)
   {
      Mode curMode = this.toolBar.getCurrentMode();
      Binder binder = getCompositeCanvas().getBinder();
      
      if (curMode == Mode.Write)
      {
         if (this.curStroke == null)
         {
            this.curStroke = new Stroke(this.pen.getCopy(), path);
            setRenderBinder(false);
            binder.getCurrentPage().addStroke(this.curStroke);
         }
         
         int numItems = path.getNumItems();
         if (numItems < 2)
         {
            float delta = 1+this.pen.getWidth();
            FloatPoint2D pt = path.getFirst();
            Page page = binder.getCurrentPage();
            doRepaint(pt.getX()+page.getX(), 
                      pt.getY()+page.getY(), 0, 0, delta);
            return;
         }
         
         FloatPoint2D pt3 = path.getItemAt(numItems-2);
         FloatPoint2D pt4 = path.getItemAt(numItems-1);
         
         doRepaintLine(pt3, pt4, binder);
      }
      else if (curMode == Mode.Delete)
      {
         Page curPage = binder.getCurrentPage();
         Vector<Stroke> strokesAtPt = curPage.getStrokesAt(path.getLast());
         if (strokesAtPt.size() == 0)
            return;
         
         Hashtable<Page, Vector<Stroke>> strokeTable = 
                            new Hashtable<Page, Vector<Stroke>>(1);
         strokeTable.put(curPage, strokesAtPt);
         
         deleteStrokes(strokeTable);
      }
   }
   
   private void doRepaintLine(FloatPoint2D pt1, FloatPoint2D pt2, 
                              Binder binder)
   {
      float x = Math.min(pt1.getX(), pt2.getX());
      float y = Math.min(pt1.getY(), pt2.getY());
      float width = Math.abs(pt1.getX()-pt2.getX());
      float height = Math.abs(pt1.getY()-pt2.getY());
      
      Page page = binder.getCurrentPage();
      if (page != null)
      {
         x += page.getX();
         y += page.getY();
      }
      
      float delta = 1+this.pen.getWidth();
      doRepaint( x, y, width, height, delta );
   }
   
   public void renderInto(Renderer2D mG2d)
   {
      /*
      if (mG2d == null)
         throw new NullPointerException();
      
      if (this.curStroke == null)
         return;
      
      Path path = this.curStroke.getPath();
      int numPts = path.getNumItems();
      
      if (numPts < 2)
         return;
      
      FloatPoint2D pt1 = path.getItemAt(numPts-2).getCopy();
      FloatPoint2D pt2 = path.getItemAt(numPts-1).getCopy();
      
      Binder binder = getCompositeCanvas().getBinder();
      float xOffset = binder.getX();
      float yOffset = binder.getY();
      
      pt1.translateBy(xOffset, yOffset);
      pt2.translateBy(xOffset, yOffset);
      
      mG2d.drawLine(pt1, pt2);
      */
      
      if (mG2d == null)
         throw new NullPointerException();
      
      if (this.curStroke == null)
         return;
      
      Binder binder = getCompositeCanvas().getBinder();
      Page page = binder.getCurrentPage();
      
      mG2d.translate(page.getX(), page.getY());
      this.curStroke.renderInto(mG2d);
      mG2d.translate(-page.getX(), -page.getY());
   }
   
   public Pen getTool()
   {
      return this.pen;
   }
   
   public class PenToolBar 
                   extends ToolBarButton 
                              implements SelectionChangeListener, 
                                         ValueChangeListener, 
                                         ActionListener, 
                                         SettingsChangedListener, 
                                         SettingsKeys, 
                                         PenSettingsConstants, 
                                         GuiSettingsConstants
   {
      private JToggleButton writeButton;
      private JToggleButton deleteButton;
      
      private TriControl<Color, ColorControl> colorControl;
      private TriControl<MValue, SizeControl> sizeControl;
      private List<PathMenuItem> menuItemVec;
      private Mode curMode;
      
      public PenToolBar()
      {
         super(DefinedIcon.compose);
         
         this.writeButton = new JToggleButton(DefinedIcon.pencil.getIcon(BUTTON_SIZE));
         this.writeButton.setActionCommand(Mode.Write.toString());
         this.writeButton.addActionListener(this);
         
         this.deleteButton = new JToggleButton(DefinedIcon.remove.getIcon(BUTTON_SIZE));
         this.deleteButton.setActionCommand(Mode.Delete.toString());
         this.deleteButton.addActionListener(this);
         
         ButtonGroup writeDelGroup = new ButtonGroup();
         writeDelGroup.add(this.writeButton);
         writeDelGroup.add(this.deleteButton);
         
         this.curMode = Mode.Write;
         
         ColorControl color1 = new ColorControl(PEN_1_COLOR);
         ColorControl color2 = new ColorControl(PEN_2_COLOR);
         ColorControl color3 = new ColorControl(PEN_3_COLOR);
         this.colorControl = 
            new TriControl<Color, ColorControl>(color1, color2, color3);
         
         SizeControl size1 = 
            new SizeControl("", FINE_SIZE_PX, MIN_SIZE_PX, 
                            MAX_SIZE_PX, STEP_SIZE_PX, 
                            Unit.PIXEL, Style.Circle, true, 
                            Color.BLACK, 1);
         SizeControl size2 = 
            new SizeControl("", MEDIUM_SIZE_PX, MIN_SIZE_PX, 
                            MAX_SIZE_PX, STEP_SIZE_PX, 
                            Unit.PIXEL, Style.Circle, true, 
                            Color.BLACK, 1);
         SizeControl size3 = 
            new SizeControl("", THICK_SIZE_PX, MIN_SIZE_PX, 
                            MAX_SIZE_PX, STEP_SIZE_PX, 
                            Unit.PIXEL, Style.Circle, true, Color.BLACK, 
                            1);
         
         this.sizeControl = 
            new TriControl<MValue, SizeControl>(size1, size2, size3);
         
         
         JToolBar toolPanel = getToolBar();
         toolPanel.setFloatable(false);
         toolPanel.add(this.writeButton);
         toolPanel.add(this.deleteButton);
         toolPanel.addSeparator();
         toolPanel.add(this.sizeControl.getControl1());
         toolPanel.add(this.sizeControl.getControl2());
         toolPanel.add(this.sizeControl.getControl3());
         toolPanel.addSeparator();
         toolPanel.add(this.colorControl.getControl1());
         toolPanel.add(this.colorControl.getControl2());
         toolPanel.add(this.colorControl.getControl3());
         
         this.colorControl.addValueChangeListener(this);
         this.colorControl.addSelectionChangeListener(this);
         
         this.sizeControl.addValueChangeListener(this);
         this.sizeControl.addSelectionChangeListener(this);
         
         // set up the menu items
         this.menuItemVec = new Vector<PathMenuItem>();
         
         //set up the paths for the menu items
         MenuPath strokePath = new MenuPath(MenuConstants.SELECT_MENU_PATH, 
                                            "Pen", 
                                            DefinedIcon.pencil.getIcon(16));
         
         // make the menu items
         ButtonGroup sizeGroup = new ButtonGroup();
         
         JCheckBoxMenuItem size1Item = new JCheckBoxMenuItem(PEN_1_SIZE_KEY);
         size1Item.addActionListener(this);
         sizeGroup.add(size1Item);
         
         JCheckBoxMenuItem size2Item = new JCheckBoxMenuItem(PEN_2_SIZE_KEY);
         size2Item.addActionListener(this);
         sizeGroup.add(size2Item);
         
         JCheckBoxMenuItem size3Item = new JCheckBoxMenuItem(PEN_3_SIZE_KEY);
         size3Item.addActionListener(this);
         sizeGroup.add(size3Item);
         
         ButtonGroup colorGroup = new ButtonGroup();
         
         JCheckBoxMenuItem color1Item = new JCheckBoxMenuItem(PEN_1_COLOR_KEY);
         color1Item.addActionListener(this);
         colorGroup.add(color1Item);
         
         JCheckBoxMenuItem color2Item = new JCheckBoxMenuItem(PEN_2_COLOR_KEY);
         color2Item.addActionListener(this);
         colorGroup.add(color2Item);
         
         JCheckBoxMenuItem color3Item = new JCheckBoxMenuItem(PEN_3_COLOR_KEY);
         color3Item.addActionListener(this);
         colorGroup.add(color3Item);
         
         this.menuItemVec.add(new PathMenuItem(new JLabel(" Size"), 
                                               strokePath));
         this.menuItemVec.add(new PathMenuItem(new JSeparator(), 
                                               strokePath));
         
         this.menuItemVec.add(new PathMenuItem(size1Item, strokePath));
         this.menuItemVec.add(new PathMenuItem(size2Item, strokePath));
         this.menuItemVec.add(new PathMenuItem(size3Item, strokePath));
         
         this.menuItemVec.add(new PathMenuItem(new JSeparator(), 
                                               strokePath));
         
         this.menuItemVec.add(new PathMenuItem(new JLabel(" Type"), 
                                               strokePath));
         this.menuItemVec.add(new PathMenuItem(new JSeparator(), 
                                               strokePath));
         
         this.menuItemVec.add(new PathMenuItem(color1Item, strokePath));
         this.menuItemVec.add(new PathMenuItem(color2Item, strokePath));
         this.menuItemVec.add(new PathMenuItem(color3Item, strokePath));
         
         this.sizeControl.getControl1().doClick();
         size1Item.setSelected(true);
         color1Item.setSelected(true);
         
         this.writeButton.doClick();
         
         updatePen();
         
         float unitScaleLevel = SettingsUtilities.getUnitScaleFactor();
         float zoomLevel = getCompositeCanvas().getZoomLevel();
         
         resizeControlsTo(unitScaleLevel);
         scaleControlsTo(zoomLevel);
         
         SettingsManager.getSharedInstance().addSettingsListener(this);
      }
      
      public Mode getCurrentMode()
      {
         return this.curMode;
      }
      
      public void setCurrentMode(Mode mode)
      {
         if (mode == null)
            throw new NullPointerException();
         
         this.curMode = mode;
      }
      
      private void syncMode()
      {
         if (this.deleteButton.isSelected())
            setCurrentMode(Mode.Delete);
         else if (this.writeButton.isSelected())
            setCurrentMode(Mode.Write);
      }
      
      public List<PathMenuItem> getPathMenuItems()
      {
         return this.menuItemVec;
      }
      
      private void updatePen()
      {
         float width = (float)this.sizeControl.getControlValue().getValue(Unit.PIXEL);
         float unitScaleLevel = SettingsUtilities.getUnitScaleFactor();
         float zoomLevel = getCompositeCanvas().getZoomLevel();
         
         width *= unitScaleLevel*zoomLevel;
         
         Color color = this.colorControl.getControlValue();
         
         pen.setWidth(width);
         pen.setColor(color);
         pen.scaleTo(getCompositeCanvas().getZoomLevel());
         
         getCompositeCanvas().setCursor(pen.getCursor());
      }
      
      public void selectionChanged(SelectionChangeEvent event)
      {
         updatePen();
      }

      public void valueChanged(ValueChangeEvent event)
      {
         updatePen();
      }
      
      @Override
      public SubCanvas getCanvas()
      {
         return SrokeCanvas.this;
      }
      
      public void start()
      {
         //do nothing when the toolbar is started.  
         //that way, when the toolbar is restarted 
         //it is in the state that it was left in.
      }
      
      public void finish()
      {
      }
      
      public void actionPerformed(ActionEvent event)
      {
         processEvent(event.getActionCommand());
      }

      public void settingsChanged(SettingsChangedEvent event)
      {
         String key = event.getKey();
         Object val = event.getNewValue();
         
         if (key.equals(PEN_1_SIZE_KEY))
            this.sizeControl.getControl1().setControlValue((MValue)val);
         else if (key.equals(PEN_2_SIZE_KEY))
            this.sizeControl.getControl2().setControlValue((MValue)val);
         else if (key.equals(PEN_3_SIZE_KEY))
            this.sizeControl.getControl3().setControlValue((MValue)val);
         else if (key.equals(PEN_1_COLOR_KEY))
            this.colorControl.getControl1().setControlValue((Color)val);
         else if (key.equals(PEN_2_COLOR_KEY))
            this.colorControl.getControl2().setControlValue((Color)val);
         else if (key.equals(PEN_3_COLOR_KEY))
            this.colorControl.getControl3().setControlValue((Color)val);
         else if (key.equals(SettingsKeys.UNIT_SCALE_FACTOR))
         {
            float newFactor = ((Number)val).floatValue();
            resizeTo(newFactor);
         }
         
         processEvent(event.getKey());
      }
      
      private void processEvent(String cmmd)
      {
         doClick();
         if (cmmd.equals(PEN_1_SIZE_KEY))
            this.sizeControl.getControl1().doClick();
         else if (cmmd.equals(PEN_2_SIZE_KEY))
            this.sizeControl.getControl2().doClick();
         else if (cmmd.equals(PEN_3_SIZE_KEY))
            this.sizeControl.getControl3().doClick();
         else if (cmmd.equals(PEN_1_COLOR_KEY))
            this.colorControl.getControl1().doClick();
         else if (cmmd.equals(PEN_2_COLOR_KEY))
            this.colorControl.getControl2().doClick();
         else if (cmmd.equals(PEN_3_COLOR_KEY))
            this.colorControl.getControl3().doClick();
         else
         {
            for (Mode mode : Mode.values())
            {
               if (cmmd.equals(mode.toString()))
               {
                  this.curMode = mode;
                  break;
               }
            }
         }
      }
      
      private void resizeControlsTo(float factor)
      {
         pen.resizeTo(factor);
         getCompositeCanvas().setCursor(pen.getCursor());
         
         resizeSizeControlTo(this.sizeControl.getControl1(), factor);
         resizeSizeControlTo(this.sizeControl.getControl2(), factor);
         resizeSizeControlTo(this.sizeControl.getControl3(), factor);
      }
      
      private void scaleControlsTo(float factor)
      {
         pen.scaleTo(factor);
         getCompositeCanvas().setCursor(pen.getCursor());
         
         scaleSizeControlTo(this.sizeControl.getControl1(), factor);
         scaleSizeControlTo(this.sizeControl.getControl2(), factor);
         scaleSizeControlTo(this.sizeControl.getControl3(), factor);
      }
      
      private void scaleControlsBy(float factor)
      {
         pen.scaleBy(factor);
         getCompositeCanvas().setCursor(pen.getCursor());
         
         scaleSizeControlBy(this.sizeControl.getControl1(), factor);
         scaleSizeControlBy(this.sizeControl.getControl2(), factor);
         scaleSizeControlBy(this.sizeControl.getControl3(), factor);
      }
      
      private void resizeSizeControlTo(SizeControl control, float factor)
      {
         if (control == null)
            throw new NullPointerException();
         
         control.getButtonPic().resizeTo(factor);
         control.repaint();
      }
      
      private void scaleSizeControlTo(SizeControl control, float factor)
      {
         if (control == null)
            throw new NullPointerException();
         
         control.getButtonPic().scaleTo(factor);
         control.repaint();
      }
      
      private void scaleSizeControlBy(SizeControl control, float factor)
      {
         if (control == null)
            throw new NullPointerException();
         
         control.getButtonPic().scaleBy(factor);
         control.repaint();
      }
   }
   
   @Override
   public void zoomBy(float val)
   {
      this.toolBar.scaleControlsBy(val);
   }

   @Override
   public void zoomTo(float val)
   {
      this.toolBar.scaleControlsTo(val);
   }
   
   @Override
   public void resizeTo(float val)
   {
      this.toolBar.resizeControlsTo(val);
   }
}
