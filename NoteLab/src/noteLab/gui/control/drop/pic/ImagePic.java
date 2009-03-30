package noteLab.gui.control.drop.pic;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;

import javax.swing.ImageIcon;

import noteLab.gui.DefinedIcon;

public class ImagePic implements ButtonPic, ImageObserver
{
   private static final float IMAGE_PERCENT = 0.8f;
   
   private DefinedIcon icon;
   
   public ImagePic(DefinedIcon icon)
   {
      setIcon(icon);
   }
   
   public DefinedIcon getIcon()
   {
      return this.icon;
   }
   
   public void setIcon(DefinedIcon icon)
   {
      if (icon == null)
         throw new NullPointerException();
      
      this.icon = icon;
   }
   
   public void paintPic(Graphics g, int width, int height)
   {
      int size = (int)(Math.min(width, height)*IMAGE_PERCENT);
      ImageIcon image = this.icon.getIcon(size);
      
      int yOffset = (height-size)/2;
      int xOffset = (width-size)/2;
      
      g.translate(xOffset, yOffset);
      g.drawImage(image.getImage(), 0, 0, this);
      g.translate(-xOffset, -yOffset);
   }

   public boolean imageUpdate(Image img, int infoflags, 
                              int x, int y, 
                              int width, int height)
   {
      return false;
   }
}
