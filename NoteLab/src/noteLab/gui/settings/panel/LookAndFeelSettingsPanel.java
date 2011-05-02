package noteLab.gui.settings.panel;

import javax.swing.JComboBox;

import noteLab.gui.settings.panel.base.ManagedSettingsPanel;
import noteLab.gui.settings.state.SettingsSaveCapable;
import noteLab.util.InfoCenter;
import noteLab.util.LookAndFeelUtilities;
import noteLab.util.arg.LookAndFeelArg;
import noteLab.util.settings.SettingsKeys;
import noteLab.util.settings.SettingsUtilities;

public class LookAndFeelSettingsPanel 
                extends ManagedSettingsPanel 
                           implements SettingsSaveCapable
{
   private JComboBox lafBox;
   
   public LookAndFeelSettingsPanel()
   {
      super("Look and Feel ("+InfoCenter.getAppName()+" restart required)", 
            "Sets the look and feel (theme) to use.", 
            SettingsKeys.LOOK_AND_FEEL_KEY, 
            LookAndFeelUtilities.getDefaultLookAndFeel());
      
      this.lafBox = new JComboBox(LookAndFeelUtilities.getLookAndFeels());
      getDisplayPanel().add(this.lafBox);
      
      updateDisplay(SettingsUtilities.getLookAndFeel());
   }
   
   @Override
   public void updateDisplay(Object curVal)
   {
      if (curVal == null)
         throw new NullPointerException();
      
      this.lafBox.setSelectedItem(curVal);
   }
   
   @Override
   public void encode(StringBuffer buffer)
   {
      if (buffer == null)
         throw new NullPointerException();
      
      int length = buffer.length();
      if (length > 0 && buffer.charAt(length-1) != ' ')
         buffer.append(' ');
      
      String encStr = (new LookAndFeelArg()).
                         encode(this.lafBox.getSelectedItem().toString());
      buffer.append(encStr);
   }
   
   @Override
   public String save()
   {
      return "";
   }
}
