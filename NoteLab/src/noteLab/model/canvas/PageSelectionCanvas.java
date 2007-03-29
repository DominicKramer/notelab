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
import noteLab.gui.ToolBarButton;
import noteLab.gui.control.drop.ColorControl;
import noteLab.gui.listener.ValueChangeEvent;
import noteLab.gui.listener.ValueChangeListener;
import noteLab.gui.menu.MenuConstants;
import noteLab.gui.menu.MenuPath;
import noteLab.gui.menu.PathMenuItem;
import noteLab.model.Page;
import noteLab.model.Path;
import noteLab.model.Paper.PaperType;
import noteLab.model.binder.Binder;
import noteLab.model.tool.PageSelector;
import noteLab.util.render.Renderer2D;
import noteLab.util.settings.SettingsChangedEvent;
import noteLab.util.settings.SettingsChangedListener;
import noteLab.util.settings.SettingsKeys;
import noteLab.util.settings.SettingsManager;

public class PageSelectionCanvas extends SubCanvas<PageSelector, Page>
{
   private PageSelector pageSelector;
   private PageSelectionToolBar toolBar;
   
   public PageSelectionCanvas(CompositeCanvas canvas)
   {
      super(canvas);
      
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
      getCompositeCanvas().
         getBinder().
            getCurrentPage().
               setSelectionEnabled(true);
   }
   
   @Override
   public void finish()
   {
      getCompositeCanvas().
         getBinder().
            getCurrentPage().
               setSelectionEnabled(false);
   }
   
   @Override
   public void pathStartedImpl(Path path, boolean newPage)
   {
      if (newPage)
         doRepaint();
   }
   
   @Override
   public void pathFinishedImpl(Path path)
   {
      
   }
   
   @Override
   public void pathChangedImpl(Path path)
   {
   }
   
   public void renderInto(Renderer2D mG2d)
   {
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
   
   private class PageSelectionToolBar 
                    extends ToolBarButton 
                               implements ActionListener, 
                                          SettingsChangedListener, 
                                          SettingsKeys, 
                                          ValueChangeListener
                                             <Color, ColorControl>, 
                                          GuiSettingsConstants
   {
      private static final String PLAIN_PAPER = "Plain";
      private static final String GRAPH_PAPER = "Graph";
      private static final String WIDE_RULED_PAPER = "WideRuled";
      private static final String COLLEGE_RULED_PAPER = "CollegeRuled";
      
      private static final String BG_COLOR = "background color";
      private static final String COPY_PAGE = "CopyPage";
      private static final String DELETE_PAGE = "DeletePage";
      private static final String CLEAR_PAGE = "ClearPage";
      
      private JToggleButton plainButton;
      private JToggleButton graphButton;
      private JToggleButton collegeButton;
      private JToggleButton wideButton;
      
      private ColorControl bgColorButton;
      private JButton copyPageButton;
      private JButton delPageButton;
      private JButton clearPageButton;
      
      private Vector<PathMenuItem> menuItemVec;
      
      public PageSelectionToolBar()
      {
         super(DefinedIcon.select_page);
         
         ButtonGroup pageTypeGroup = new ButtonGroup();
         
         //construct the buttons that control the page type
         this.plainButton = 
            new JToggleButton(DefinedIcon.page.getIcon(BUTTON_SIZE));
         this.plainButton.setActionCommand(PLAIN_PAPER);
         this.plainButton.addActionListener(this);
         pageTypeGroup.add(this.plainButton);
         
         this.graphButton = 
            new JToggleButton(DefinedIcon.graph.getIcon(BUTTON_SIZE));
         this.graphButton.setActionCommand(GRAPH_PAPER);
         this.graphButton.addActionListener(this);
         pageTypeGroup.add(this.graphButton);
         
         this.collegeButton = 
            new JToggleButton(DefinedIcon.college_rule.getIcon(BUTTON_SIZE));
         this.collegeButton.setActionCommand(COLLEGE_RULED_PAPER);
         this.collegeButton.addActionListener(this);
         pageTypeGroup.add(this.collegeButton);
         
         this.wideButton = 
            new JToggleButton(DefinedIcon.wide_rule.getIcon(BUTTON_SIZE));
         this.wideButton.setActionCommand(WIDE_RULED_PAPER);
         this.wideButton.addActionListener(this);
         pageTypeGroup.add(this.wideButton);
         
         //construct the cut/copy/paste buttons
         this.copyPageButton = 
            new JButton(DefinedIcon.copy_page.getIcon(BUTTON_SIZE));
         this.copyPageButton.setActionCommand(COPY_PAGE);
         this.copyPageButton.addActionListener(this);
         
         this.delPageButton = 
            new JButton(DefinedIcon.delete_page.getIcon(BUTTON_SIZE));
         this.delPageButton.setActionCommand(DELETE_PAGE);
         this.delPageButton.addActionListener(this);
         
         Page curPage = getCompositeCanvas().getBinder().getCurrentPage();
         this.bgColorButton = 
            new ColorControl(curPage.getPaper().getBackgroundColor(), 
                             BUTTON_SIZE+8);
         JButton colorButton = this.bgColorButton.getDecoratedButton();
         colorButton.setActionCommand(BG_COLOR);
         colorButton.addActionListener(this);
         this.bgColorButton.addValueChangeListener(this);
         
         this.clearPageButton = 
            new JButton(DefinedIcon.delete_stroke.getIcon(BUTTON_SIZE));
         this.clearPageButton.setActionCommand(CLEAR_PAGE);
         this.clearPageButton.addActionListener(this);
         
         //add the components to the toolbar
         JToolBar panel = getToolBar();
         panel.add(this.plainButton);
         panel.add(this.graphButton);
         panel.add(this.collegeButton);
         panel.add(this.wideButton);
         panel.addSeparator();
         panel.add(this.bgColorButton);
         panel.addSeparator();
         panel.add(this.copyPageButton);
         panel.add(this.delPageButton);
         panel.add(this.clearPageButton);
         
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
         plainItem.setActionCommand(PLAIN_PAPER);
         plainItem.addActionListener(menuListener);
         typeGroup.add(plainItem);
         
         JCheckBoxMenuItem graphItem = 
               new JCheckBoxMenuItem("Graph", DefinedIcon.graph.getIcon(16));
         graphItem.setActionCommand(GRAPH_PAPER);
         graphItem.addActionListener(menuListener);
         typeGroup.add(graphItem);
         
         JCheckBoxMenuItem collegeItem = 
               new JCheckBoxMenuItem("College", 
                                     DefinedIcon.college_rule.getIcon(16));
         collegeItem.setActionCommand(COLLEGE_RULED_PAPER);
         collegeItem.addActionListener(menuListener);
         typeGroup.add(collegeItem);
         
         JCheckBoxMenuItem wideItem = 
               new JCheckBoxMenuItem("Wide", 
                                     DefinedIcon.wide_rule.getIcon(16));
         wideItem.setActionCommand(WIDE_RULED_PAPER);
         wideItem.addActionListener(menuListener);
         typeGroup.add(wideItem);
         
         //JMenuItem copyItem = new JMenuItem("Copy");
         //copyItem.setActionCommand(COPY_PAGE);
         //copyItem.addActionListener(menuListener);
         
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
      
      public void actionPerformed(ActionEvent e)
      {
         CompositeCanvas canvas = getCompositeCanvas();
         Binder binder = canvas.getBinder();
         Page selPage = binder.getCurrentPage();
         if (selPage == null)
            return;
         
         String cmmd = e.getActionCommand();
         if (cmmd.equals(BG_COLOR))
            setColor();
         else if (cmmd.equals(COPY_PAGE))
            binder.copyPage();
         else if (cmmd.equals(DELETE_PAGE))
            binder.removeCurrentPage();
         else if (cmmd.equals(PLAIN_PAPER))
            selPage.setPaperType(PaperType.Plain);
         else if (cmmd.equals(GRAPH_PAPER))
            selPage.setPaperType(PaperType.Graph);
         else if (cmmd.equals(COLLEGE_RULED_PAPER))
            selPage.setPaperType(PaperType.CollegeRuled);
         else if (cmmd.equals(WIDE_RULED_PAPER))
            selPage.setPaperType(PaperType.WideRuled);
         else if (cmmd.equals(CLEAR_PAGE))
            selPage.clear();
            
         doRepaint();
      }
      
      private void setColor()
      {
         CompositeCanvas canvas = getCompositeCanvas();
         Binder binder = canvas.getBinder();
         Page selPage = binder.getCurrentPage();
         if (selPage == null)
            return;
         
         Color color = this.bgColorButton.getControlValue();
         selPage.getPaper().setBackgroundColor(color);
         canvas.doRepaint();
      }
      
      private class MenuListener implements ActionListener
      {
         public void actionPerformed(ActionEvent event)
         {
            doClick();
            String cmmd = event.getActionCommand();
            if (cmmd.equals(COPY_PAGE))
               copyPageButton.doClick();
            else if (cmmd.equals(PLAIN_PAPER))
               plainButton.doClick();
            else if (cmmd.equals(GRAPH_PAPER))
               graphButton.doClick();
            else if (cmmd.equals(COLLEGE_RULED_PAPER))
               collegeButton.doClick();
            else if (cmmd.equals(WIDE_RULED_PAPER))
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
      }
   }
}
