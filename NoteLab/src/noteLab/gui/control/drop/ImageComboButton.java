package noteLab.gui.control.drop;

import noteLab.gui.DefinedIcon;
import noteLab.gui.control.drop.pic.ImagePic;

public class ImageComboButton extends ComboButton<DefinedIcon, ImageComboButton>
{
   private DefinedIcon prevIcon;
   
   public ImageComboButton(DefinedIcon icon)
   {
      super(new ImagePic(icon));
      
      this.prevIcon = icon;
   }
   
   @Override
   public ImagePic getButtonPic()
   {
      return (ImagePic)super.getButtonPic();
   }
   
   @Override
   public DefinedIcon getPreviousValue()
   {
      return this.prevIcon;
   }

   public DefinedIcon getControlValue()
   {
      return getButtonPic().getIcon();
   }

   public void setControlValue(DefinedIcon icon)
   {
      this.prevIcon = getControlValue();
      
      getButtonPic().setIcon(icon);
   }
}
