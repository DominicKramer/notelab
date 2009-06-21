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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JWindow;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.ToolBarButton;
import noteLab.gui.Tooltips;
import noteLab.gui.button.IconToggleButton;
import noteLab.gui.chooser.NoteLabFileChooser;
import noteLab.gui.chooser.filter.JarnalFileFilter;
import noteLab.gui.chooser.filter.NoteLabFileFilter;
import noteLab.gui.chooser.filter.SupportedFileFilter;
import noteLab.gui.control.drop.ButtonComboButton;
import noteLab.gui.control.drop.ColorControl;
import noteLab.gui.listener.ValueChangeEvent;
import noteLab.gui.listener.ValueChangeListener;
import noteLab.gui.menu.MenuConstants;
import noteLab.gui.menu.MenuPath;
import noteLab.gui.menu.PathMenuItem;
import noteLab.gui.toolbar.CutCopyPasteToolBar;
import noteLab.gui.toolbar.file.OpenFileProcessor;
import noteLab.model.Page;
import noteLab.model.Path;
import noteLab.model.Paper.PaperType;
import noteLab.model.binder.Binder;
import noteLab.model.geom.FloatPoint2D;
import noteLab.model.tool.PageSelector;
import noteLab.util.copy.CopyStateListener;
import noteLab.util.copy.CutCopyPasteReady;
import noteLab.util.geom.RectangleUnioner;
import noteLab.util.geom.Transformable;
import noteLab.util.render.Renderer2D;
import noteLab.util.settings.SettingsChangedEvent;
import noteLab.util.settings.SettingsChangedListener;
import noteLab.util.settings.SettingsKeys;
import noteLab.util.settings.SettingsManager;
import noteLab.util.settings.SettingsUtilities;
import noteLab.util.structure.CopyVector;

public class PageSelectionCanvas extends SubCanvas<PageSelector, Page>
{
   private enum Mode
   {
      Selection, 
      Unselection, 
      SelectDropLocation, 
      SelectImportLocation
   };
   
   private enum Action
   {
      Paste, 
      Open, 
      Drop, 
      DeletePage, 
      ClearPage, 
      ChangeBGColor
   }
   
   private static final float DEFAULT_DROP_LINE_DELTA = 3;
   
   private PageSelector pageSelector;
   private PageSelectionToolBar toolBar;
   
   private DropLine dropLine;
   
   private Vector<Page> selPageVec;
   
   public PageSelectionCanvas(CompositeCanvas canvas)
   {
      super(canvas, false);
      
      this.dropLine = new DropLine(2);
      this.selPageVec = new Vector<Page>();
      this.pageSelector = new PageSelector();
      this.toolBar = new PageSelectionToolBar();
   }
   
   public PageSelectionToolBar getToolBarButton()
   {
      return this.toolBar;
   }
   
   @Override
   public PageSelector getTool()
   {
      return this.pageSelector;
   }
   
   @Override
   public void start()
   {
      for (Page page : this.selPageVec)
         page.setSelectionEnabled(true);
      
      doRepaint();
   }
   
   @Override
   public void finish()
   {
      for (Page page : this.selPageVec)
         page.setSelectionEnabled(false);
      
      doRepaint();
   }
   
   @Override
   public boolean getRenderBinder()
   {
      return true;
   }
   
   private void selectPage(Page page)
   {
      if (page == null)
         throw new NullPointerException();
      
      if (this.selPageVec.contains(page))
         return;
      
      page.setSelectionEnabled(true);
      page.setSelected(true);
      this.selPageVec.add(page);
      
      this.toolBar.syncActionButtons();
      this.toolBar.notifyOfCopyState();
   }
   
   private void selectAllPages()
   {
      for (Page page : getCompositeCanvas().getBinder())
         selectPage(page);
   }
   
   private void unselectPage(Page page)
   {
      if (page == null)
         throw new NullPointerException();
      
      page.setSelectionEnabled(false);
      page.setSelected(false);
      
      this.selPageVec.remove(page);
      
      this.toolBar.syncActionButtons();
      this.toolBar.notifyOfCopyState();
   }
   
   private void unselectAllPages()
   {
      for (Page page : this.selPageVec)
      {
         page.setSelectionEnabled(false);
         page.setSelected(false);
      }
      this.selPageVec.clear();
      
      this.toolBar.syncActionButtons();
      this.toolBar.notifyOfCopyState();
   }
   
   private void doPaintDirtyRectangle()
   {
      if (this.selPageVec.isEmpty())
         return;
      
      RectangleUnioner unioner = new RectangleUnioner();
      for (Page page : this.selPageVec)
         unioner.union(page.getBounds2D());
      
      Rectangle2D.Float union = unioner.getUnion();
      
      doRepaint((float)union.getX(), 
                (float)union.getY(), 
                (float)union.getWidth(), 
                (float)union.getHeight(), 1);
   }
   
   @Override
   public void pathStartedImpl(Path path, MouseButton button, boolean newPage)
   {
      pathChangedImpl(path, button);
   }
   
   @Override
   public void pathFinishedImpl(Path path, MouseButton button)
   {
      
   }
   
   @Override
   public void pathChangedImpl(Path path, MouseButton button)
   {
      Mode mode = this.toolBar.getCurrentMode();
      
      if (mode.equals(Mode.SelectDropLocation) || 
          mode.equals(Mode.SelectImportLocation))
      {
         FloatPoint2D lastPt = path.getLast();
         CompositeCanvas canvas = getCompositeCanvas();
         Page page = canvas.getBinder().getPageAt(lastPt);
         if (page == null)
            return;
         
         Rectangle2D.Float bounds = page.getBounds2D();
         float averageH = (float)(bounds.getY()+bounds.getHeight()/2.0);
         
         float y = lastPt.getY();
         
         boolean isAbove = y <= averageH;
         float newY = (isAbove)?(page.getMinY()):(page.getMaxY());
         float newX = page.getX();
         float delta = (isAbove)?
                          (-DEFAULT_DROP_LINE_DELTA):
                             (DEFAULT_DROP_LINE_DELTA+1);
         delta *= canvas.getZoomLevel();
         this.dropLine.setLocation(newX, newX+page.getWidth(), newY+delta, 
                                   page, isAbove);
         if (mode.equals(Mode.SelectDropLocation))
            this.toolBar.showPasteMenu();
         else
            this.toolBar.showImportMenu();
      }
      else
      {
         FloatPoint2D lastPt = path.getLast();
         Page page = getCompositeCanvas().getBinder().getPageAt(lastPt);
         if (page == null)
            return;
         
         if (this.toolBar.getCurrentMode().equals(Mode.Selection))
            selectPage(page);
         else if (this.toolBar.getCurrentMode().equals(Mode.Unselection))
            unselectPage(page);
         
         float x = page.getX();
         float y = page.getY();
         float w = page.getWidth();
         float h = page.getHeight();
         doRepaint(x, y, w, h, 1);
      }
   }
   
   public void renderInto(Renderer2D overlayDisplay, Renderer2D mG2d)
   {
      Mode curMode = this.toolBar.getCurrentMode();
      if (curMode.equals(Mode.SelectDropLocation) || 
          curMode.equals(Mode.SelectImportLocation))
      {
         mG2d.setColor(Color.RED);
         mG2d.setLineWidth(this.dropLine.getWidth());
         mG2d.drawLine(this.dropLine.getLeftPoint(), this.dropLine.getRightPoint());
      }
   }
   
   @Override
   public void zoomBy(float val)
   {
      this.dropLine.scaleBy(val, val);
   }

   @Override
   public void zoomTo(float val)
   {
      this.dropLine.scaleTo(val, val);
   }
   
   @Override
   public void resizeTo(float val)
   {
      this.dropLine.resizeTo(val, val);
   }
   
   private class DropLine implements Transformable
   {
      private FloatPoint2D leftPt;
      private FloatPoint2D rightPt;
      private float width;
      private boolean dropAbove;
      private Page page;
      
      private DropLine(float width)
      {
         this.width = width;
         this.dropAbove = true;
         this.page = null;
         
         float scaleLevel = getCompositeCanvas().getZoomLevel();
         this.leftPt = new FloatPoint2D(0, 0, scaleLevel, scaleLevel);
         this.rightPt = new FloatPoint2D(0, 0, scaleLevel, scaleLevel);
      }
      
      public float getWidth()
      {
         return this.width;
      }
      
      public FloatPoint2D getLeftPoint()
      {
         return this.leftPt;
      }
      
      public FloatPoint2D getRightPoint()
      {
         return this.rightPt;
      }
      
      public boolean dropAbove()
      {
         return this.dropAbove;
      }
      
      public Page getDropPage()
      {
         return this.page;
      }
      
      public void setLocation(float leftX, float rightX, float y, 
                              Page dropPage, boolean dropAbove)
      {
         float oldLeft = this.leftPt.getX();
         float oldRight = this.rightPt.getX();
         float oldY = this.leftPt.getY();
         
         this.leftPt.translateTo(leftX, y);
         this.rightPt.translateTo(rightX, y);
         
         this.page = dropPage;
         this.dropAbove = dropAbove;
         
         doPaint(oldLeft, oldRight, oldY);
         doPaint(leftX, rightX, y);
      }
      
      private void doPaint()
      {
         float leftX = this.leftPt.getX();
         float rightX = this.rightPt.getX();
         float y = this.leftPt.getY();
         
         doRepaint(leftX, y-this.width, rightX-leftX, 2*this.width, this.width);
      }
      
      private void doPaint(float leftX, float rightX, float y)
      {
         doRepaint(leftX, y-this.width, rightX-leftX, 2*this.width, this.width);
      }

      public void resizeTo(float x, float y)
      {
         this.leftPt.resizeTo(x, y);
         this.rightPt.resizeTo(x, y);
      }

      public void scaleBy(float x, float y)
      {
         this.leftPt.scaleBy(x, y);
         this.rightPt.scaleBy(x, y);
      }

      public void scaleTo(float x, float y)
      {
         this.leftPt.scaleTo(x, y);
         this.rightPt.scaleTo(x, y);
      }

      public void translateBy(float x, float y)
      {
         this.leftPt.translateBy(x, y);
         this.rightPt.translateBy(x, y);
      }

      public void translateTo(float x, float y)
      {
         this.leftPt.translateTo(x, y);
         this.rightPt.translateTo(x, y);
      }
   }
   
   private class PageSelectionToolBar 
                    extends ToolBarButton 
                               implements ActionListener, 
                                          SettingsChangedListener, 
                                          SettingsKeys, 
                                          ValueChangeListener
                                             <Color, ColorControl>, 
                                          GuiSettingsConstants, 
                                          CutCopyPasteReady<CopyVector<Page>>
   {
      private final String CANCEL_PASTE = "CANCEL_PASTE";
      private final String CANCEL_IMPORT = "CANCEL_IMPORT";
      
      private ButtonComboButton typeCombo;
      private IconToggleButton plainButton;
      private IconToggleButton graphButton;
      private IconToggleButton collegeButton;
      private IconToggleButton wideButton;
      
      private JToggleButton selButton;
      private JButton selAllButton;
      
      private JToggleButton unSelButton;
      private JButton unSelAllButton;
      
      private ColorControl bgColorButton;
      private JButton delPageButton;
      private JButton clearPageButton;
      
      private JButton openButton;
      private JButton dropButton;
      private JTextField fileField;
      
      private Vector<PathMenuItem> menuItemVec;
      
      private Mode curMode;
      
      private Vector<CopyStateListener> copyListenerVec;
      
      private JPopupMenu pasteMenu;
      private CopyVector<Page> pasteVec;
      
      private JPopupMenu importMenu;
      private CompositeCanvas importCanvas;
      
      private CutCopyPasteToolBar<CopyVector<Page>> cutToolbar;
      
      public PageSelectionToolBar()
      {
         super(DefinedIcon.select_page);
         
         setToolTipText(Tooltips.PAGE_EDITING_MODE);
         
         int smallSize = GuiSettingsConstants.SMALL_BUTTON_SIZE;
         
         JMenuItem pasteItem = new JMenuItem("Paste", DefinedIcon.paste.getIcon(smallSize));
         pasteItem.setActionCommand(Action.Paste.toString());
         pasteItem.addActionListener(this);
         pasteItem.setToolTipText(Tooltips.PAGE_PASTE);
         
         JMenuItem cancelPaste = new JMenuItem("Cancel", DefinedIcon.close.getIcon(smallSize));
         cancelPaste.setActionCommand(CANCEL_PASTE);
         cancelPaste.addActionListener(this);
         cancelPaste.setToolTipText(Tooltips.PAGE_CANCEL_PASTE);
         
         this.pasteMenu = new JPopupMenu();
         this.pasteMenu.add(pasteItem);
         this.pasteMenu.add(cancelPaste);
         
         JMenuItem importItem = new JMenuItem("Import", DefinedIcon.jump.getIcon(smallSize));
         importItem.setActionCommand(Action.Drop.toString());
         importItem.addActionListener(this);
         importItem.setToolTipText(Tooltips.PAGE_OPEN_IMPORT);
         
         JMenuItem cancelImport = new JMenuItem("Cancel", DefinedIcon.close.getIcon(smallSize));
         cancelImport.setActionCommand(CANCEL_IMPORT);
         cancelImport.addActionListener(this);
         cancelImport.setToolTipText(Tooltips.PAGE_CANCEL_IMPORT);
         
         this.importMenu = new JPopupMenu();
         this.importMenu.add(importItem);
         this.importMenu.add(cancelImport);
         
         this.importCanvas = null;
         
         this.copyListenerVec = new Vector<CopyStateListener>();
         this.pasteVec = new CopyVector<Page>(0);
         
         int size = BUTTON_SIZE;
         
         ButtonGroup selGroup = new ButtonGroup();
         
         this.selButton = new JToggleButton(DefinedIcon.select_page.getIcon(size));
         this.selButton.setActionCommand(Mode.Selection.toString());
         this.selButton.addActionListener(this);
         this.selButton.setToolTipText(Tooltips.PAGE_SELECT);
         selGroup.add(this.selButton);
         
         this.unSelButton = new JToggleButton(DefinedIcon.unselect_page.getIcon(size));
         this.unSelButton.setActionCommand(Mode.Unselection.toString());
         this.unSelButton.addActionListener(this);
         this.unSelButton.setToolTipText(Tooltips.PAGE_UNSELECT);
         selGroup.add(this.unSelButton);
         
         this.selAllButton = new JButton(DefinedIcon.select_all_page.getIcon(size));
         this.selAllButton.setActionCommand(DefinedIcon.select_all_page.toString());
         this.selAllButton.addActionListener(this);
         this.selAllButton.setToolTipText(Tooltips.PAGE_SELECT_ALL);
         
         this.unSelAllButton = new JButton(DefinedIcon.unselect_all_page.getIcon(size));
         this.unSelAllButton.setActionCommand(DefinedIcon.unselect_all_page.toString());
         this.unSelAllButton.addActionListener(this);
         this.unSelAllButton.setToolTipText(Tooltips.PAGE_UNSELECT_ALL);
         
         //construct the buttons that control the page type
         this.plainButton = 
            new IconToggleButton(DefinedIcon.page, BUTTON_SIZE);
         this.plainButton.setActionCommand(PaperType.Plain.toString());
         this.plainButton.addActionListener(this);
         this.plainButton.setToolTipText(Tooltips.TYPE_PLAIN_PAPER);
         
         this.graphButton = 
            new IconToggleButton(DefinedIcon.graph, BUTTON_SIZE);
         this.graphButton.setActionCommand(PaperType.Graph.toString());
         this.graphButton.addActionListener(this);
         this.graphButton.setToolTipText(Tooltips.TYPE_GRAPH_PAPER);
         
         this.collegeButton = 
            new IconToggleButton(DefinedIcon.college_rule, BUTTON_SIZE);
         this.collegeButton.setActionCommand(PaperType.CollegeRuled.toString());
         this.collegeButton.addActionListener(this);
         this.collegeButton.setToolTipText(Tooltips.TYPE_COLLEGE_RULED_PAPER);
         
         this.wideButton = 
            new IconToggleButton(DefinedIcon.wide_rule, BUTTON_SIZE);
         this.wideButton.setActionCommand(PaperType.WideRuled.toString());
         this.wideButton.addActionListener(this);
         this.wideButton.setToolTipText(Tooltips.TYPE_WIDE_RULED_PAPER);
         
         this.typeCombo = new ButtonComboButton(DefinedIcon.page);
         this.typeCombo.addActionListener(this);
         this.typeCombo.setToolTipText(Tooltips.PAGE_TYPE);
         this.typeCombo.registerButton(this.plainButton);
         this.typeCombo.registerButton(this.graphButton);
         this.typeCombo.registerButton(this.collegeButton);
         this.typeCombo.registerButton(this.wideButton);
         
         JWindow typePopup = this.typeCombo.getPopupWindow();
         typePopup.setLayout(new FlowLayout(FlowLayout.LEFT));
         typePopup.add(this.plainButton);
         typePopup.add(this.graphButton);
         typePopup.add(this.collegeButton);
         typePopup.add(this.wideButton);
         
         this.delPageButton = 
            new JButton(DefinedIcon.delete_page.getIcon(BUTTON_SIZE));
         this.delPageButton.setActionCommand(Action.DeletePage.toString());
         this.delPageButton.addActionListener(this);
         this.delPageButton.setToolTipText(Tooltips.PAGE_DELETE);
         
         Color defaultColor = SettingsUtilities.getPaperColor();
         
         Page curPage = getCompositeCanvas().getBinder().getCurrentPage();
         this.bgColorButton = new ColorControl(defaultColor);
         this.bgColorButton.setActionCommand(Action.ChangeBGColor.toString());
         this.bgColorButton.addActionListener(this);
         this.bgColorButton.addValueChangeListener(this);
         this.bgColorButton.setToolTipText(Tooltips.PAGE_COLOR);
         
         this.clearPageButton = 
            new JButton(DefinedIcon.delete_stroke.getIcon(BUTTON_SIZE));
         this.clearPageButton.setActionCommand(Action.ClearPage.toString());
         this.clearPageButton.addActionListener(this);
         this.clearPageButton.setToolTipText(Tooltips.PAGE_DELETE_STROKES);
         
         JToolBar toolbar = getToolBar();
         toolbar.add(this.selButton);
         toolbar.add(this.selAllButton);
         toolbar.addSeparator();
         toolbar.add(this.unSelButton);
         toolbar.add(this.unSelAllButton);
         toolbar.addSeparator();
         toolbar.add(typeCombo);
         toolbar.addSeparator();
         toolbar.add(this.bgColorButton);
         
         this.cutToolbar = 
                  new CutCopyPasteToolBar<CopyVector<Page>>(this);
         this.cutToolbar.setCopyIcon(DefinedIcon.copy_page);
         this.cutToolbar.setCutToolTipText(Tooltips.PAGE_CUT);
         this.cutToolbar.setCopyToolTipText(Tooltips.PAGE_COPY);
         this.cutToolbar.setPasteToolTipText(Tooltips.PAGE_PASTE);
         
         this.openButton = new JButton(DefinedIcon.directory.getIcon(SMALL_BUTTON_SIZE));
         this.openButton.setActionCommand(Action.Open.toString());
         this.openButton.addActionListener(this);
         this.openButton.setToolTipText(Tooltips.PAGE_OPEN_IMPORT);
         
         this.fileField = new JTextField(10);
         this.fileField.setEditable(false);
         
         this.dropButton = new JButton(DefinedIcon.jump.getIcon(SMALL_BUTTON_SIZE));
         this.dropButton.setActionCommand(Mode.SelectImportLocation.toString());
         this.dropButton.addActionListener(this);
         this.dropButton.setEnabled(false);
         this.dropButton.setToolTipText(Tooltips.PAGE_DROP_IMPORT);
         
         toolbar.add(this.delPageButton);
         toolbar.add(this.clearPageButton);
         toolbar.addSeparator();
         this.cutToolbar.appendTo(toolbar);
         
         toolbar.add(this.openButton);
         toolbar.add(this.fileField);
         toolbar.add(this.dropButton);
         
         //set up the menu items
         this.menuItemVec = new Vector<PathMenuItem>();
         
         //set up the path
         MenuPath selPagePath = new MenuPath(MenuConstants.SELECT_MENU_PATH, 
                                             "Page", 
                                             DefinedIcon.college_rule.
                                                            getIcon(16));
         
         //make the menu items
         MenuListener menuListener = new MenuListener();
         
         ButtonGroup typeGroup = new ButtonGroup();
         
         JCheckBoxMenuItem plainItem = 
               new JCheckBoxMenuItem("Plain", DefinedIcon.page.getIcon(16));
         plainItem.setActionCommand(PaperType.Plain.toString());
         plainItem.addActionListener(menuListener);
         plainItem.setToolTipText(Tooltips.TYPE_PLAIN_PAPER);
         typeGroup.add(plainItem);
         
         JCheckBoxMenuItem graphItem = 
               new JCheckBoxMenuItem("Graph", DefinedIcon.graph.getIcon(16));
         graphItem.setActionCommand(PaperType.Graph.toString());
         graphItem.addActionListener(menuListener);
         graphItem.setToolTipText(Tooltips.TYPE_GRAPH_PAPER);
         typeGroup.add(graphItem);
         
         JCheckBoxMenuItem collegeItem = 
               new JCheckBoxMenuItem("College", 
                                     DefinedIcon.college_rule.getIcon(16));
         collegeItem.setActionCommand(PaperType.CollegeRuled.toString());
         collegeItem.addActionListener(menuListener);
         collegeItem.setToolTipText(Tooltips.TYPE_COLLEGE_RULED_PAPER);
         typeGroup.add(collegeItem);
         
         JCheckBoxMenuItem wideItem = 
               new JCheckBoxMenuItem("Wide", 
                                     DefinedIcon.wide_rule.getIcon(16));
         wideItem.setActionCommand(PaperType.WideRuled.toString());
         wideItem.addActionListener(menuListener);
         wideItem.setToolTipText(Tooltips.TYPE_WIDE_RULED_PAPER);
         typeGroup.add(wideItem);
         
         //add the menu items to the Vector
         this.menuItemVec.add(new PathMenuItem(new JLabel(" Type"), 
                                               selPagePath));
         this.menuItemVec.add(new PathMenuItem(new JSeparator(), 
                                               selPagePath));
         
         this.menuItemVec.add(new PathMenuItem(plainItem, selPagePath));
         this.menuItemVec.add(new PathMenuItem(graphItem, selPagePath));
         this.menuItemVec.add(new PathMenuItem(collegeItem, selPagePath));
         this.menuItemVec.add(new PathMenuItem(wideItem, selPagePath));
         
         PaperType type = curPage.getPaper().getPaperType();
         if (type == PaperType.Plain)
         {
            this.plainButton.doClick();
            plainItem.setSelected(true);
         }
         else if (type == PaperType.Graph)
         {
            this.graphButton.doClick();
            graphItem.setSelected(true);
         }
         else if (type == PaperType.CollegeRuled)
         {
            this.collegeButton.doClick();
            collegeItem.setSelected(true);
         }
         else if (type == PaperType.WideRuled)
         {
            this.wideButton.doClick();
            wideItem.setSelected(true);
         }
         
         this.selButton.doClick();
         syncActionButtons();
         
         SettingsManager.getSharedInstance().addSettingsListener(this);
      }
      
      public void start()
      {
         //do nothing when the toolbar is started.  
         //that way, when the toolbar is restarted 
         //it is in the state that it was left in.
      }

      @Override
      public void finish()
      {
         this.bgColorButton.setPopupVisible(false);
         this.typeCombo.setPopupVisible(false);
      }

      @Override
      public SubCanvas getCanvas()
      {
         return PageSelectionCanvas.this;
      }

      public List<PathMenuItem> getPathMenuItems()
      {
         return this.menuItemVec;
      }
      
      public Mode getCurrentMode()
      {
         return this.curMode;
      }
      
      private void syncActionButtons()
      {
         boolean canEdit = !selPageVec.isEmpty();
         
         this.plainButton.setEnabled(canEdit);
         this.graphButton.setEnabled(canEdit);
         this.collegeButton.setEnabled(canEdit);
         this.wideButton.setEnabled(canEdit);
         
         this.bgColorButton.setEnabled(canEdit);
         this.delPageButton.setEnabled(canEdit);
         this.clearPageButton.setEnabled(canEdit);
         
         this.bgColorButton.setEnabled(canEdit);
      }
      
      public void actionPerformed(ActionEvent e)
      {
         String cmmd = e.getActionCommand();
         
         Mode[] modes = Mode.values();
         for (Mode m : modes)
         {
            if (cmmd.equals(m.toString()))
            {
               this.curMode = m;
               break;
            }
         }
         
         if (cmmd.equals(Mode.Selection.toString()) || 
             cmmd.equals(Mode.Unselection.toString()))
            return;
         
         if (cmmd.equals(DefinedIcon.select_all_page.toString()))
         {
            selectAllPages();
         }
         else if (cmmd.equals(DefinedIcon.unselect_all_page.toString()))
         {
            unselectAllPages();
            doRepaint();
         }
         else if (cmmd.equals(Action.ChangeBGColor.toString()))
         {
            setColor();
         }
         else if (cmmd.equals(CANCEL_PASTE))
         {
            this.curMode = Mode.Selection;
            this.cutToolbar.setPasteIcon(DefinedIcon.paste);
            dropLine.doPaint();
         }
         else if (cmmd.equals(Action.Paste.toString()))
         {
            if (this.pasteVec == null)
               return;
            
            dropPages(this.pasteVec);
         }
         else if (cmmd.equals(CANCEL_IMPORT))
         {
            this.curMode = Mode.Selection;
            dropLine.doPaint();
         }
         else if (cmmd.equals(Action.Drop.toString()))
         {
            if (this.importCanvas == null)
               return;
            
            dropPages(this.importCanvas.getBinder());
         }
         else if (cmmd.equals(Action.Open.toString()))
         {
            ImportProcessor processor = new ImportProcessor();
            NoteLabFileChooser openChooser = 
                                  new NoteLabFileChooser("Open", true, false, processor);
            openChooser.setAcceptAllFileFilterUsed(false);
            openChooser.addChoosableFileFilter(new JarnalFileFilter());
            openChooser.addChoosableFileFilter(new NoteLabFileFilter());
            openChooser.addChoosableFileFilter(new SupportedFileFilter());
            openChooser.showFileChooser();
         }
         else if (cmmd.equals(Mode.SelectImportLocation.toString()))
         {
            curMode = Mode.SelectImportLocation;
         }
         else if (cmmd.equals(Action.DeletePage.toString()))
         {
            deleteSelectedPages();
         }
         else if (cmmd.equals(Action.ClearPage.toString()))
         {
            for (Page page : selPageVec)
               page.clear();
         }
         else if (cmmd.equals(PaperType.Plain.toString()))
         {
            for (Page page : selPageVec)
               page.setPaperType(PaperType.Plain);
         }
         else if (cmmd.equals(PaperType.Graph.toString()))
         {
            for (Page page : selPageVec)
               page.setPaperType(PaperType.Graph);
         }
         else if (cmmd.equals(PaperType.CollegeRuled.toString()))
         {
            for (Page page : selPageVec)
               page.setPaperType(PaperType.CollegeRuled);
         }
         else if (cmmd.equals(PaperType.WideRuled.toString()))
         {
            for (Page page : selPageVec)
               page.setPaperType(PaperType.WideRuled);
         }
         
         doPaintDirtyRectangle();
      }
      
      private void dropPages(Iterable<Page> pageVec)
      {
         Page dropPage = dropLine.getDropPage();
         CompositeCanvas canvas = getCompositeCanvas();
         Binder binder = canvas.getBinder();
         float zoomLevel = canvas.getZoomLevel();
         
         if (dropLine.dropAbove())
         {
            Page basePage = dropPage;
            Stack<Page> pageStack = new Stack<Page>();
            for (Page page : pageVec)
               pageStack.push(page);
            
            for (Page curPage : pageStack)
            {
               curPage = curPage.getCopy();
               curPage.scaleTo(zoomLevel, zoomLevel);
               binder.addPageBefore(basePage, curPage);
               basePage = curPage;
            }
         }
         else
         {
            Page basePage = dropPage;
            for (Page curPage : pageVec)
            {
               curPage = curPage.getCopy();
               curPage.scaleTo(zoomLevel, zoomLevel);
               binder.addPageAfter(basePage, curPage);
               basePage = curPage;
            }
         }
         
         this.curMode = Mode.Selection;
         this.cutToolbar.setPasteIcon(DefinedIcon.paste);
         doRepaint();
      }
      
      private void showPasteMenu()
      {
         FloatPoint2D rightPoint = dropLine.getRightPoint();
         float x = rightPoint.getX();
         float y = rightPoint.getY();
         
         JComponent panel = getCompositeCanvas().getDisplayPanel();
         if (panel == null)
            return;
         
         this.pasteMenu.show(panel, (int)x, (int)y);
      }
      
      private void showImportMenu()
      {
         FloatPoint2D rightPoint = dropLine.getRightPoint();
         float x = rightPoint.getX();
         float y = rightPoint.getY();
         
         JComponent panel = getCompositeCanvas().getDisplayPanel();
         if (panel == null)
            return;
         
         this.importMenu.show(panel, (int)x, (int)y);
      }
      
      private void deleteSelectedPages()
      {
         Binder binder = getCompositeCanvas().getBinder();
         for (Page page : selPageVec)
         {
            binder.removePage(page);
            page.setSelected(false);
         }
         
         doPaintDirtyRectangle();
         selPageVec.clear();
         syncActionButtons();
      }
      
      private void setColor()
      {
         Color color = this.bgColorButton.getControlValue();
            for (Page page : selPageVec)
               page.getPaper().setBackgroundColor(color);
      }
      
      private class MenuListener implements ActionListener
      {
         public void actionPerformed(ActionEvent event)
         {
            doClick();
            String cmmd = event.getActionCommand();
            if (cmmd.equals(PaperType.Plain.toString()))
               plainButton.doClick();
            else if (cmmd.equals(PaperType.Graph.toString()))
               graphButton.doClick();
            else if (cmmd.equals(PaperType.CollegeRuled.toString()))
               collegeButton.doClick();
            else if (cmmd.equals(PaperType.WideRuled.toString()))
               wideButton.doClick();
         }
      }

      public void settingsChanged(SettingsChangedEvent event)
      {
         String key = event.getKey();
         if (key.equals(PAPER_TYPE_KEY))
         {
            PaperType type = (PaperType)event.getNewValue();
            if (type.equals(PaperType.Plain))
               plainButton.doClick();
            else if (type.equals(PaperType.Graph))
               graphButton.doClick();
            else if (type.equals(PaperType.CollegeRuled))
               collegeButton.doClick();
            else if (type.equals(PaperType.WideRuled))
               wideButton.doClick();
         }
         else if (key.equals(PAPER_COLOR_KEY))
         {
            Object ob = event.getNewValue();
            if ( (ob == null) || !(ob instanceof Color) )
               return;
            
            bgColorButton.setControlValue((Color)ob);
            bgColorButton.doClick();
         }
      }

      public void valueChanged(ValueChangeEvent<Color, ColorControl> event)
      {
         setColor();
         doPaintDirtyRectangle();
      }

      public CopyVector<Page> copy()
      {
         CopyVector<Page> selPages = new CopyVector<Page>(selPageVec.size());
         for (Page page : selPageVec)
            selPages.add(page.getCopy());
         
         return selPages;
      }

      public CopyVector<Page> cut()
      {
         CopyVector<Page> copy = copy();
         deleteSelectedPages();
         return copy;
      }

      public void paste(CopyVector<Page> item)
      {
         this.curMode = Mode.SelectDropLocation;
         this.pasteVec = item;
         this.cutToolbar.setPasteIcon(DefinedIcon.paste_down);
      }

      public void addCopyStateListener(CopyStateListener listener)
      {
         if (listener == null)
            throw new NullPointerException();
         
         if (!this.copyListenerVec.contains(listener))
            this.copyListenerVec.add(listener);
      }

      public void removeCopyStateListener(CopyStateListener listener)
      {
         if (listener == null)
            throw new NullPointerException();
         
         this.copyListenerVec.remove(listener);
      }
      
      private void notifyOfCopyState()
      {
         boolean canCopy = !selPageVec.isEmpty();
         for (CopyStateListener listener : this.copyListenerVec)
            listener.copyStateChanged(canCopy);
      }
      
      private class ImportProcessor extends OpenFileProcessor
      {
         @Override
         protected void processCanvasLoaded(CompositeCanvas canvas)
         {
            curMode = Mode.SelectImportLocation;
            importCanvas = canvas;
            
            CompositeCanvas realCanvas = getCompositeCanvas();
            realCanvas.setCurrentCanvas(realCanvas.getPageCanvas());
            
            File file = getLastFileProcessed();
            String name = "";
            if (file != null)
               name = file.getAbsolutePath();
            
            fileField.setText("Loaded:  \""+name+"\"");
            dropButton.setEnabled(true);
         }
      }
   }
}
