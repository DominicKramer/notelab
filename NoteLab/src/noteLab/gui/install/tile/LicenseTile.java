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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.WindowConstants;

import noteLab.gui.file.ViewFilePanel;
import noteLab.gui.help.HelpConstants;
import noteLab.gui.sequence.ProceedType;
import noteLab.gui.sequence.SequenceTile;
import noteLab.util.InfoCenter;

public class LicenseTile extends SequenceTile implements HelpConstants
{
   private ViewFilePanel licensePanel;
   private AcceptPanel acceptPanel;
   
   public LicenseTile(WelcomeInstallTile prevTile)
   {
      super(prevTile, true, true);
      
      this.licensePanel = new ViewFilePanel();
      this.acceptPanel = new AcceptPanel();
      
      this.licensePanel.setText(INFO_PREFIX+"/"+LICENSE_URL, 
                                INFO_PREFIX+"/"+LICENSE_URL, 
                                LICENSE_NAME);
      
      JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      labelPanel.add(new JLabel("Before continuing you must accept "+
                                InfoCenter.getAppName()+"'s license."));
      
      setLayout(new BorderLayout());
      add(labelPanel, BorderLayout.NORTH);
      add(this.licensePanel, BorderLayout.CENTER);
      add(this.acceptPanel, BorderLayout.SOUTH);
   }
   
   @Override
   public SequenceTile getNextTile()
   {
     SequenceTile next = super.getNextTile();
     if (next != null)
        return next;
     
     next = new InstallDirTile(this);
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
   
   private class AcceptPanel extends JPanel implements ActionListener
   {
      private JRadioButton acceptButton;
      private JRadioButton noAcceptButton;
      
      public AcceptPanel()
      {
         this.acceptButton = 
                 new JRadioButton("I accept the terms of the license agreement.");
         this.noAcceptButton = 
                 new JRadioButton("I do not accept the terms of the " +
                                  "license agreement.");
         
         this.acceptButton.addActionListener(this);
         this.noAcceptButton.addActionListener(this);
         
         ButtonGroup group = new ButtonGroup();
         group.add(this.acceptButton);
         group.add(this.noAcceptButton);
         
         this.noAcceptButton.setSelected(true);
         
         JPanel acceptPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         acceptPanel.add(this.acceptButton);
         
         JPanel noAcceptPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
         noAcceptPanel.add(this.noAcceptButton);
         
         setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
         add(acceptPanel);
         add(noAcceptPanel);
      }
      
      public boolean isAccepted()
      {
         return this.acceptButton.isSelected();
      }

      public void actionPerformed(ActionEvent e)
      {
         if (isAccepted())
            notifyTileProceedChanged(ProceedType.can_proceed);
         else
            notifyTileProceedChanged(ProceedType.can_not_proceed);
      }
   }
   
   public static void main(String[] args)
   {
      JFrame frame = new JFrame();
      frame.add(new LicenseTile(new WelcomeInstallTile()));
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.pack();
      frame.setVisible(true);
   }
}
