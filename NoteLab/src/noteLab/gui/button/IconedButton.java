package noteLab.gui.button;

import java.awt.event.ActionListener;

import noteLab.gui.DefinedIcon;

public interface IconedButton
{
   public DefinedIcon getDefinedIcon();
   public void setDefinedIcon(DefinedIcon icon, int size);
   
   public void addActionListener(ActionListener listener);
   
   public String getActionCommand();
}
