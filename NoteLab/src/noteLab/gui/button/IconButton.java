package noteLab.gui.button;

import javax.swing.JButton;

import noteLab.gui.DefinedIcon;

public class IconButton extends JButton implements IconedButton
{
   private DefinedIcon icon;
   
   public IconButton(DefinedIcon icon, int size)
   {
      setDefinedIcon(icon, size);
   }
   
   public DefinedIcon getDefinedIcon()
   {
      return this.icon;
   }

   public void setDefinedIcon(DefinedIcon icon, int size)
   {
      if (icon == null)
         throw new NullPointerException();
      
      this.icon = icon;
      setIcon(this.icon.getIcon(size));
      setActionCommand(this.icon.toString());
   }
}
