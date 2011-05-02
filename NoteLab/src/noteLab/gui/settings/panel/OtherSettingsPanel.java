package noteLab.gui.settings.panel;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import noteLab.gui.settings.state.SettingsSaveCapable;
import noteLab.gui.settings.state.SettingsStateCapable;

public class OtherSettingsPanel 
                extends JPanel 
                           implements SettingsStateCapable, 
                                      SettingsSaveCapable
{
   private VMSettingsPanel vmPanel;
   private LookAndFeelSettingsPanel lafPanel;
   
   public OtherSettingsPanel()
   {
      this.vmPanel = new VMSettingsPanel();
      this.vmPanel.setBorder(new TitledBorder("Memory Settings"));
      
      this.lafPanel = new LookAndFeelSettingsPanel();
      this.lafPanel.setBorder(new TitledBorder("Look and Feel Settings"));
      
      JPanel innerPanel = new JPanel();
      innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
      innerPanel.add(this.lafPanel);
      innerPanel.add(this.vmPanel);
      
      add(innerPanel);
   }
   
   @Override
   public void encode(StringBuffer buffer)
   {
      if (buffer == null)
         throw new NullPointerException();
      
      this.vmPanel.encode(buffer);
      this.lafPanel.encode(buffer);
   }

   @Override
   public String save()
   {
      return this.vmPanel.save() + this.lafPanel.save();
   }

   @Override
   public void revertToSaved()
   {
      this.vmPanel.revertToSaved();
      this.lafPanel.revertToSaved();
   }

   @Override
   public void restoreDefaults()
   {
      this.vmPanel.restoreDefaults();
      this.lafPanel.restoreDefaults();
   }

   @Override
   public void apply()
   {
      this.vmPanel.apply();
      this.lafPanel.apply();
   }
}
