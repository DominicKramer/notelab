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

package noteLab.gui.uninstall;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.html.HTMLEditorKit;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.sequence.ProceedType;
import noteLab.gui.sequence.SequenceTile;
import noteLab.util.InfoCenter;
import noteLab.util.percent.PercentChangedListener;

public class WelcomeUninstallTile 
                extends SequenceTile 
                           implements ActionListener, 
                                      PercentChangedListener
{
   private JCheckBox savePrefsBox;
   private JButton uninstallButton;
   private ProgressPanel progressPanel;
   private File installDir;
   
   public WelcomeUninstallTile(File installDir)
   {
      super(null, false, false);
      
      String appName = InfoCenter.getAppName();
      String appVers = InfoCenter.getAppVersion();
      
      StringBuffer buffer = new StringBuffer();
      buffer.append("<b><font color=\"blue\"><center>Thank you for your interest in ");
      buffer.append(appName);
      buffer.append("</center></font></b><br><br>This uninstaller will guide you ");
      buffer.append("through the uninstallation of ");
      buffer.append(appName);
      buffer.append(" version ");
      buffer.append(appVers);
      buffer.append(".<br><br>You can find future releases of ");
      buffer.append(InfoCenter.getAppName());
      buffer.append(" on its homepage at <br><br><font color=\"blue\"><center>");
      buffer.append(InfoCenter.getHomepage());
      buffer.append("</center></font><br>  ");
      buffer.append("Any questions or comments are ");
      buffer.append("greatly appreciated and can be directed to ");
      buffer.append(InfoCenter.getAuthor());
      buffer.append(" at ");
      buffer.append(InfoCenter.getAuthorEmail());
      buffer.append(".<br><br>");
      
      this.installDir = installDir;
      if (this.installDir == null)
      {
         buffer.append("<b>Unfortunately ");
         buffer.append(appName);
         buffer.append("'s installation directory could not be determined.  ");
         buffer.append("As such, ");
         buffer.append(appName);
         buffer.append(" cannot be uninstalled.</b>");
      }
      else
      {
         buffer.append("<b>The directory <br><br><font color=\"blue\"><center>");
         buffer.append(installDir.getAbsolutePath());
         buffer.append("<br><br></center></font>will be deleted during the ");
         buffer.append("installation.</b>");
      }
      
      Color bgColor = getBackground();
      
      JEditorPane htmlPane = new JEditorPane();
      htmlPane.setEditable(false);
      htmlPane.setPreferredSize(new Dimension(500, 300));
      htmlPane.setEditorKit(new HTMLEditorKit());
      htmlPane.setText(buffer.toString());
      htmlPane.setBackground(bgColor);
      
      JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      welcomePanel.add(new JScrollPane(htmlPane));
      
      this.savePrefsBox = new JCheckBox("Save user preferences", true);
      this.savePrefsBox.setEnabled(installDir != null);
      JPanel prefPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      prefPanel.add(this.savePrefsBox);      
      
      JPanel mainPanel = new JPanel(new BorderLayout());
      mainPanel.add(welcomePanel, BorderLayout.CENTER);
      mainPanel.add(prefPanel, BorderLayout.SOUTH);
      
      int size = GuiSettingsConstants.SMALL_BUTTON_SIZE;
      ImageIcon icon = DefinedIcon.about.getIcon(size);
      
      this.uninstallButton = new JButton("Uninstall", icon);
      this.uninstallButton.setEnabled(installDir != null);
      this.uninstallButton.addActionListener(this);
      
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      buttonPanel.add(uninstallButton);
      
      JPanel outerPanel = new JPanel(new BorderLayout());
      outerPanel.add(mainPanel, BorderLayout.CENTER);
      outerPanel.add(buttonPanel, BorderLayout.SOUTH);
      
      this.progressPanel = new ProgressPanel();
      
      JPanel spacerPanel = new JPanel(new BorderLayout());
      spacerPanel.add(outerPanel, BorderLayout.CENTER);
      spacerPanel.add(this.progressPanel, BorderLayout.SOUTH);
      
      setLayout(new BorderLayout());
      add(new JLabel("    "), BorderLayout.NORTH);
      add(spacerPanel, BorderLayout.CENTER);
      add(new JLabel("    "), BorderLayout.SOUTH);
   }
   
   @Override
   public void sequenceCancelled()
   {
   }

   @Override
   public void sequenceCompleted()
   {
   }
   
   public void percentChanged(int percent, String message)
   {
      this.progressPanel.setPercent(percent);
      this.progressPanel.setMessage(message);
   }

   public void actionPerformed(ActionEvent e)
   {
      this.uninstallButton.setEnabled(false);
      
      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            try
            {
               Uninstaller.uninstall(WelcomeUninstallTile.this,
                                     installDir, 
                                     savePrefsBox.isSelected());
               notifyTileProceedChanged(ProceedType.can_proceed);
            }
            catch (Exception exception)
            {
               int size = GuiSettingsConstants.BUTTON_SIZE;
               ImageIcon icon = DefinedIcon.dialog_error.getIcon(size);
               
               String message = "An error has occured during the uninstallation.  " +
                                "The error returned was:  "+
                                exception.getMessage();
               
               JOptionPane.showMessageDialog(new JFrame(), 
                                             message, 
                                             "Error", 
                                             JOptionPane.ERROR_MESSAGE, 
                                             icon);
               
               notifyTileProceedChanged(ProceedType.failed);
            }
         }
      });
   }
   
   private static class ProgressPanel extends JPanel
   {
      private JProgressBar progressBar;
      private JLabel progressLabel;
      
      public ProgressPanel()
      {
         this.progressBar = new JProgressBar(0, 100);
         this.progressBar.setStringPainted(true);
         
         this.progressLabel = new JLabel("  ");
         
         setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
         add(this.progressBar);
         add(this.progressLabel);
      }
      
      public void setPercent(int percent)
      {
         this.progressBar.setValue(percent);
      }
      
      public void setMessage(String message)
      {
         if (message == null)
            throw new NullPointerException();
         
         this.progressLabel.setText(message);
      }
   }
   
   public static void main(String[] args)
   {
      File installDir = null;
      if (args.length > 0)
         installDir = new File(args[0]);
      
      JFrame frame = new JFrame();
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.add(new WelcomeUninstallTile(installDir));
      frame.pack();
      frame.setVisible(true);
   }
}
