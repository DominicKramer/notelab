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

package noteLab.util.arg;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import noteLab.util.LookAndFeelUtilities;
import noteLab.util.settings.SettingsKeys;
import noteLab.util.settings.SettingsUtilities;

public class LookAndFeelArg extends Argument
{
   private static final ParamInfo[] PARAM_DESCS;
   static
   {
      String prefix = "Specifies that the ";
      String suffix = " look and feel should be used to decorate the " +
                      "graphical elements.";
      
      String[] lafArr = LookAndFeelUtilities.getLookAndFeels();
      PARAM_DESCS = new ParamInfo[lafArr.length];
      
      for (int i=0; i<lafArr.length; i++)
         PARAM_DESCS[i] = new ParamInfo(lafArr[i], 
                                        prefix+lafArr[i]+suffix);
   }
   
   public LookAndFeelArg()
   {
      super(SettingsKeys.LOOK_AND_FEEL_KEY, 1, PARAM_DESCS, 
            "Used to set the look and feel of the graphical elements.", 
            false);
   }
   
   public String encode(String lookAndFeel)
   {
      boolean found = false;
      for (String allowedLook : LookAndFeelUtilities.getLookAndFeels())
      {
         if (allowedLook.equals(lookAndFeel))
         {
            found = true;
            break;
         }
      }
      
      if (!found)
         throw new IllegalArgumentException("ERROR:  The look and feel '"+
                                            lookAndFeel+"' is not a valid " +
                                            "look and feel for this system.");
      
      return PREFIX+getIdentifier()+" "+lookAndFeel;
   }
   
   @Override
   public ArgResult decode(String[] args)
   {
      if (args.length < 1)
      {
         System.out.println("ERROR:  The look and feel could not be " +
                            "changed because "+LookAndFeelArg.class.getName()+
                            " was not properly given the look and feel to " +
                            "load");
         
         return ArgResult.SHOW_GUI;
      }
      
      if (!SettingsUtilities.setLookAndFeel(args[0]))
         System.out.println("WARNING:  The look and feel \""+args[0]+
                            "\" was not loaded because it was not " +
                            "recognized as a look and feel.");
      
      return ArgResult.SHOW_GUI;
   }
   
   public static void main(String[] args)
   {
      LookAndFeelInfo[] infoArr = UIManager.getInstalledLookAndFeels();
      for (LookAndFeelInfo info : infoArr)
        System.out.println(info.getName()+" @ "+info.getClassName());
   }
}
