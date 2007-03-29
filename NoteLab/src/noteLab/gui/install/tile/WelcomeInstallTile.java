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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.text.html.HTMLEditorKit;

import noteLab.gui.sequence.ProceedType;
import noteLab.gui.sequence.SequenceTile;
import noteLab.util.InfoCenter;

public class WelcomeInstallTile extends SequenceTile
{
   public WelcomeInstallTile()
   {
      super(null, false, true);
      
      String appName = InfoCenter.getAppName();
      String appVers = InfoCenter.getAppVersion();
      
      StringBuffer buffer = new StringBuffer();
      buffer.append("<font color=\"blue\"><center>Welcome to ");
      buffer.append(appName);
      buffer.append("</center></font><br><br>This installer will guide you ");
      buffer.append("through the installation of ");
      buffer.append(appName);
      buffer.append(" version ");
      buffer.append(appVers);
      buffer.append(".<br><br>For more information visit the ");
      buffer.append(InfoCenter.getAppName());
      buffer.append(" homepage at <br><br><center><font color=\"blue\">");
      buffer.append(InfoCenter.getHomepage());
      buffer.append("</font></center><br>  ");
      buffer.append("Any questions or comments are ");
      buffer.append("greatly appreciated and can be directed to ");
      buffer.append(InfoCenter.getAuthor());
      buffer.append(" at ");
      buffer.append(InfoCenter.getAuthorEmail());
      buffer.append(".<br><br>It is strongly recommended that you uninstall ");
      buffer.append("any previous versions of ");
      buffer.append(appName);
      buffer.append(".");
      
      Color bgColor = getBackground();
      
      JEditorPane htmlPane = new JEditorPane();
      htmlPane.setEditable(false);
      htmlPane.setPreferredSize(new Dimension(500, 300));
      htmlPane.setEditorKit(new HTMLEditorKit());
      htmlPane.setText(buffer.toString());
      htmlPane.setBackground(bgColor);
      
      JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      welcomePanel.add(new JScrollPane(htmlPane));
      
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      add(new JLabel("    "));
      add(new JLabel("    "));
      add(new JLabel("    "));
      add(welcomePanel);
      
      notifyTileProceedChanged(ProceedType.can_proceed);
   }
   
   @Override
   public SequenceTile getNextTile()
   {
     SequenceTile next = super.getNextTile();
     if (next != null)
        return next;
     
     next = new LicenseTile(this);
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
   
   public static void main(String[] args)
   {
      JFrame frame = new JFrame();
      frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      frame.add(new WelcomeInstallTile());
      frame.pack();
      frame.setVisible(true);
   }
}
