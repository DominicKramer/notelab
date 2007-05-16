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

package noteLab.gui.install.tile;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import noteLab.gui.DefinedIcon;
import noteLab.gui.GuiSettingsConstants;
import noteLab.gui.chooser.FileProcessor;
import noteLab.gui.chooser.NoteLabFileChooser;
import noteLab.gui.sequence.ProceedType;
import noteLab.gui.sequence.SequenceTile;

public class InstallDirTile extends SequenceTile implements ActionListener
{
   private JTextField urlField;
   private JLabel errorLabel;
   
   private NoteLabFileChooser browseChooser;
   
   public InstallDirTile(LicenseTile prevTile)
   {
      super(prevTile, true, true);
      
      this.urlField = new JTextField(30);
      
      this.errorLabel = new JLabel("     ");
      this.errorLabel.setForeground(Color.RED);
      
      int size = GuiSettingsConstants.SMALL_BUTTON_SIZE;
      ImageIcon icon = DefinedIcon.directory.getIcon(size);
      JButton browseButton = new JButton("Browse", icon);
      browseButton.addActionListener(this);
      
      JPanel browsePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      browsePanel.add(this.urlField);
      browsePanel.add(browseButton);
      
      JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      topPanel.add(new JLabel("Please select the installation directory."));
      
      JPanel errorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      errorPanel.add(this.errorLabel);
      
      JPanel contentPanel = new JPanel(new BorderLayout());
      contentPanel.add(topPanel, BorderLayout.NORTH);
      contentPanel.add(browsePanel, BorderLayout.CENTER);
      contentPanel.add(errorPanel, BorderLayout.SOUTH);
      
      JPanel labelPanel = new JPanel();
      labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));
      labelPanel.add(new JLabel("    "));
      labelPanel.add(new JLabel("    "));
      labelPanel.add(new JLabel("    "));
      labelPanel.add(new JLabel("    "));
      labelPanel.add(new JLabel("    "));
      labelPanel.add(new JLabel("    "));
      labelPanel.add(contentPanel);
      labelPanel.add(new JLabel("    "));
      labelPanel.add(new JLabel("    "));
      labelPanel.add(new JLabel("    "));
      labelPanel.add(new JLabel("    "));
      labelPanel.add(new JLabel("    "));
      labelPanel.add(new JLabel("    "));
      
      setLayout(new FlowLayout(FlowLayout.CENTER));
      add(labelPanel);
      
      FileProcessor processor = new FileProcessor()
      {
         private File file;
         
         public File getLastFileProcessed()
         {
            return this.file;
         }
         
         public void processFile(File file)
         {
            if (file == null)
               return;
            
            this.file = file;
            
            String errorText = null;
            if (!file.canWrite())
               errorText = "The directory choosen cannot be written to.";
            
            urlField.setText(file.getAbsolutePath());
            errorLabel.setText((errorText==null)?"    ":errorText);
            
            if (errorText == null)
               notifyTileProceedChanged(ProceedType.can_proceed);
            else
               notifyTileProceedChanged(ProceedType.can_not_proceed);
         }

         public File getFormattedName(File file)
         {
            return file;
         }
      };
      
      this.browseChooser = new NoteLabFileChooser("Select", 
                                                  false, 
                                                  false, 
                                                  processor);
      this.browseChooser.setMultiSelectionEnabled(false);
      this.browseChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
   }
   
   public File getInstallDirectory()
   {
      return new File(this.urlField.getText());
   }
   
   @Override
   public SequenceTile getNextTile()
   {
     SequenceTile next = super.getNextTile();
     if (next != null)
        return next;
     
     next = new ExtractTile(this);
     super.setNextTile(next);
     return next;
   }
   
   @Override
   public void sequenceCancelled()
   {
   }

   @Override
   public void sequenceCompleted()
   {
   }

   public void actionPerformed(ActionEvent e)
   {
      this.browseChooser.showFileChooser();
   }
   
   public static void main(String[] args)
   {
      JFrame frame = new JFrame();
      frame.add(new InstallDirTile(null));
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.pack();
      frame.setVisible(true);
   }
}
