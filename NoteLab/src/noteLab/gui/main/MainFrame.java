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

package noteLab.gui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.ToolBarButton;
import noteLab.gui.Tooltips;
import noteLab.gui.button.IconToggleButton;
import noteLab.gui.fullscreen.FullScreenListener;
import noteLab.gui.fullscreen.FullScreenManager;
import noteLab.gui.help.HelpMenu;
import noteLab.gui.menu.DebugMenu;
import noteLab.gui.menu.DynamicMenuBar;
import noteLab.gui.menu.MenuConstants;
import noteLab.gui.menu.Menued;
import noteLab.gui.menu.PathMenuItem;
import noteLab.gui.toolbar.BinderToolBar;
import noteLab.gui.toolbar.CanvasControlToolBar;
import noteLab.gui.toolbar.UndoRedoToolBar;
import noteLab.gui.toolbar.file.FileToolBar;
import noteLab.model.canvas.CompositeCanvas;
import noteLab.util.InfoCenter;
import noteLab.util.mod.ModListener;
import noteLab.util.mod.ModType;
import noteLab.util.progress.ProgressEvent;
import noteLab.util.progress.ProgressListener;
import noteLab.util.settings.SettingsUtilities;
import noteLab.util.undoRedo.UndoRedoManager;

public class MainFrame extends JFrame implements Menued, 
                                                 ActionListener, 
                                                 ModListener, 
                                                 ProgressListener, 
                                                 FullScreenListener
{
   private static final String TITLE_PREFIX = "  -  ";
   private static final String UNTITLED_NAME = "untitled";
   private static final String MODIFIED_TEXT = "  (Modified)";
   
   private static int NUM_OPEN = 0;
   
   private static final String EXIT = "exit";
   private static final String VIEW_MAIN_TOOLBAR = "View Main Toolbar";
   private static final String VIEW_TOOL_TOOLBAR = "View Tool Toolbar";
   
   private static final DefinedIcon FULLSCREEN_ICON = 
                                       DefinedIcon.resize_stroke;
   
   private String baseTitle;
   
   private CanvasControlToolBar canvasToolbar;
   private JPanel toolbarPanel;
   private BinderToolBar binderToolBar;
   private CompositeCanvas canvas;
   private FileToolBar fileToolBar;
   private Vector<PathMenuItem> menuItemVec;
   private MainFrameCloseListener closeListener;
   private JProgressBar progressBar;
   private JLabel messageLabel;
   
   private JCheckBoxMenuItem viewMainToolbar;
   private JCheckBoxMenuItem viewToolToolbar;
   
   private IconToggleButton fullScreenButton;
   
   private JWindow twinWindow;
   private JPanel mainPanel;
   
   public MainFrame()
   {
      this(new CompositeCanvas(1));
   }
   
   public MainFrame(CompositeCanvas canvas)
   {
      if (canvas == null)
         throw new NullPointerException();
      
      this.twinWindow = new JWindow();
      this.twinWindow.setLayout(new GridLayout(1,1));
      
      FullScreenManager.getSharedInstance().addFullScreenListener(this);
      
      setIconImage(DefinedIcon.feather.
                      getIcon(DefinedIcon.ORIGINAL_SIZE).
                         getImage());
      
      //make the exit item on the file menu
      this.menuItemVec = new Vector<PathMenuItem>(1);
      
      JMenuItem exitItem = 
         new JMenuItem("Quit", DefinedIcon.quit.getIcon(16));
      exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 
                                                     InputEvent.CTRL_DOWN_MASK, 
                                                     true));
      exitItem.setActionCommand(EXIT);
      exitItem.addActionListener(this);
      //exitItem.setToolTipText(Tooltips.EXIT);
      
      this.menuItemVec.add(new PathMenuItem(exitItem, 
                                            MenuConstants.FILE_MENU_PATH));
      
      this.viewMainToolbar = 
         new JCheckBoxMenuItem(VIEW_MAIN_TOOLBAR);
      this.viewMainToolbar.setActionCommand(VIEW_MAIN_TOOLBAR);
      this.viewMainToolbar.addActionListener(this);
      
      this.viewToolToolbar = 
         new JCheckBoxMenuItem(VIEW_TOOL_TOOLBAR);
      this.viewToolToolbar.setActionCommand(VIEW_TOOL_TOOLBAR);
      this.viewToolToolbar.addActionListener(this);
      
      this.menuItemVec.add(new PathMenuItem(this.viewMainToolbar, 
                                            MenuConstants.VIEW_MENU_PATH));
      this.menuItemVec.add(new PathMenuItem(this.viewToolToolbar, 
                                            MenuConstants.VIEW_MENU_PATH));
      
      this.canvas = canvas;
      this.canvas.addModListener(this);
      
      this.fileToolBar = new FileToolBar(this);
      
        UndoRedoManager manager = this.canvas.getUndoRedoManager();
        UndoRedoToolBar undoRedoToolBar = new UndoRedoToolBar(manager);
        undoRedoToolBar.appendTo(this.fileToolBar);
        
        this.binderToolBar = new BinderToolBar(this.canvas);
        this.binderToolBar.appendTo(this.fileToolBar);
      
      this.fullScreenButton = 
            new IconToggleButton(FULLSCREEN_ICON, 
                                 GuiSettingsConstants.BUTTON_SIZE);
      this.fullScreenButton.addActionListener(this);
      this.fullScreenButton.setToolTipText(Tooltips.FULLSCREEN);
      
      this.fileToolBar.add(this.fullScreenButton);
      
      this.canvasToolbar = new CanvasControlToolBar(this.canvas);
      
      this.toolbarPanel = new JPanel(new BorderLayout());
      this.toolbarPanel.add(this.fileToolBar, BorderLayout.NORTH);
      this.toolbarPanel.add(this.canvasToolbar, BorderLayout.CENTER);
      
      this.progressBar = new JProgressBar(0,100);
      this.progressBar.setStringPainted(false);
      
      this.messageLabel = new JLabel("  ");
      JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      messagePanel.add(this.progressBar);
      messagePanel.add(this.messageLabel);
      
      this.mainPanel = new JPanel(new BorderLayout());
      this.mainPanel.add(this.toolbarPanel, BorderLayout.NORTH);
      this.mainPanel.add(new MainPanel(this.canvas), BorderLayout.CENTER);
      this.mainPanel.add(messagePanel, BorderLayout.SOUTH);
      
      // now configure the menu bar
      DynamicMenuBar menuBar = new DynamicMenuBar();
      
      Vector<ToolBarButton> toolBars = this.canvas.getToolBars();
      menuBar.addMenued(this.fileToolBar);
      menuBar.addMenued(this);
      
      for (ToolBarButton toolBar : toolBars)
         menuBar.addMenued(toolBar);
      
      menuBar.addMenued(new HelpMenu());
      
      if (SettingsUtilities.getShowDebugMenu())
         menuBar.addMenued(new DebugMenu(this.canvas));
      
      setJMenuBar(menuBar);
      
      this.closeListener = new MainFrameCloseListener();
      addWindowListener(this.closeListener);
      this.closeListener.windowOpened();
      
      this.baseTitle = TITLE_PREFIX+InfoCenter.getAppName()+" Version "+
                       InfoCenter.getAppVersion();
      
      NUM_OPEN++;
      if (NUM_OPEN > 1)
         this.baseTitle += TITLE_PREFIX+"Window "+NUM_OPEN;
      hasBeenSaved();
      
      setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      
      // Add the contents to the frame and 
      // pack the frame to its best size
      add(this.mainPanel);
      pack();
      
      // Resize the frame if it is bigger than the screen.
      // The delta describes how much smaller the screen is made.
      // This is used so that the frame isn't made the exact size 
      // of the screen.  As a result the user will be able to reach 
      // the frame's corners to resize it.
      int delta = 100;
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      Dimension screenSize = toolkit.getScreenSize();
      int maxHeight = Math.min(getHeight()+delta, screenSize.height-delta);
      int maxWidth = Math.min(getWidth()+delta, screenSize.width-delta);
      
      setSize(maxWidth, maxHeight);
      
      // The settings manager should not notify of changes 
      // every time a new MainFrame is opened since this would 
      // affect previously opened MainFrame ojects.
      // SettingsManager.getSharedInstance().notifyOfChanges();
      
      this.viewMainToolbar.doClick();
      this.viewToolToolbar.doClick();
      
      FullScreenManager.getSharedInstance().revokeFullScreenMode();
   }
   
   public void setMessage(String message, Color color)
   {
      if (message == null || color == null)
         throw new NullPointerException();
      
      this.messageLabel.setForeground(color);
      this.messageLabel.setText(message);
   }
   
   public CompositeCanvas getCompositeCanvas()
   {
      return this.canvas;
   }
   
   public List<PathMenuItem> getPathMenuItems()
   {
      return this.menuItemVec;
   }

   public void actionPerformed(ActionEvent e)
   {
      String cmmd = e.getActionCommand();
      if (cmmd.equals(EXIT))
      {
         this.closeListener.processWindowClosing();
      }
      else if (cmmd.equals(VIEW_MAIN_TOOLBAR))
      {
         boolean show = this.viewMainToolbar.isSelected();
         this.fileToolBar.setVisible(show);
      }
      else if (cmmd.equals(VIEW_TOOL_TOOLBAR))
      {
         boolean show = this.viewToolToolbar.isSelected();
         this.canvasToolbar.setVisible(show);
      }
      else if (cmmd.equals(FULLSCREEN_ICON.toString()))
      {
         GraphicsDevice screen = GraphicsEnvironment.
                                    getLocalGraphicsEnvironment().
                                       getDefaultScreenDevice();
         
         try
         {
            if (this.fullScreenButton.isSelected())
            {
               remove(this.mainPanel);
               this.twinWindow.add(this.mainPanel);
               this.twinWindow.validate();
               screen.setFullScreenWindow(this.twinWindow);
            }
            else
            {
               FullScreenManager.
                  getSharedInstance().
                     revokeFullScreenMode();
               this.twinWindow.setVisible(false);
            }
         }
         catch (Exception exception)
         {
            System.err.print("An error occured while trying to make " +
                             InfoCenter.getAppName());
            if (this.fullScreenButton.isSelected())
               System.err.print(" enter ");
            else
               System.err.print(" leave ");
            System.err.println("fullscreen mode.  The error returned was:");
            System.err.println(exception);
            System.err.print("The system reports that it does ");
            if (!screen.isFullScreenSupported())
               System.err.print("not ");
            System.err.println("support fullscreen mode.");
            System.err.println("Stack trace:");
            exception.printStackTrace();
         }
      }
   }
   
   private class MainFrameCloseListener extends WindowAdapter
   {
      public MainFrameCloseListener()
      {
      }
      
      public void windowOpened()
      {
      }
      
      public void windowClosing(WindowEvent e)
      {
         processWindowClosing();
      }
      
      private void processWindowClosing()
      {
         if (canvas.hasBeenModified())
         {
            int result = 
                  JOptionPane.
                     showConfirmDialog(new JFrame(), "Save changes?", "Save?", 
                                       JOptionPane.YES_NO_CANCEL_OPTION, 
                                       JOptionPane.QUESTION_MESSAGE, 
                                       DefinedIcon.dialog_question.
                                                      getIcon(48));
            
            if (result == JOptionPane.CANCEL_OPTION)
               return;
            
            if (result == JOptionPane.YES_OPTION)
            {
               // The argument 'false' is used so that the "save as" option is 
               // not forced.  That is, if the user has already saved the 
               // current canvas to a file, the user won't be asked to again 
               // select a file.  Instead it will use the current file.
               
               //CHANGED:  This might need to check if the file was actually 
               //          saved.
               fileToolBar.save(false, true);
            }
         }
         
         dispose();
         NUM_OPEN--;
         if (NUM_OPEN == 0)
            System.exit(0);
      }
   }
   
   public void hasBeenSaved()
   {
      File savedFile = this.canvas.getFile();
      String title = (savedFile != null)?
                        (savedFile.getName()+this.baseTitle):
                           (UNTITLED_NAME+this.baseTitle);
      setTitle(title);
      this.canvas.setHasBeenModified(false);
   }

   public void modOccured(Object source, ModType type)
   {
      String curTitle = getTitle();
      if (!curTitle.endsWith(MODIFIED_TEXT))
         curTitle += MODIFIED_TEXT;
      
      setTitle(curTitle);
   }
   
   public void progressOccured(ProgressEvent event)
   {
      if (event == null)
         throw new NullPointerException();
      
      if (event.isIndeterminate())
         this.progressBar.setIndeterminate(true);
      else
      {
         this.progressBar.setIndeterminate(false);
         this.progressBar.setValue(event.getPercent());
      }
      
      if (event.isComplete())
      {
         this.progressBar.setIndeterminate(false);
         this.progressBar.setValue(100);
      }
   }

   public void fullScreenRevoked()
   {
      this.fullScreenButton.setSelected(false);
      
      this.twinWindow.remove(this.mainPanel);
      add(this.mainPanel);
      validate();
      repaint();
   }
}
