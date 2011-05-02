package noteLab.util.arg;

import noteLab.util.settings.SettingsKeys;
import noteLab.util.settings.SettingsManager;


public class RenderScrollingArg extends Argument
{
   private static final ParamInfo[] PARAM_DESCS = new ParamInfo[1];
   static
   {
      StringBuffer buffer = new StringBuffer();
      buffer.append("Specifies if the contents of pages should be ");
      buffer.append("rendered when the pages are scrolled.  It may be ");
      buffer.append("useful to disable page rendering during scrolling ");
      buffer.append("on slow systems.");
      
      PARAM_DESCS[0] = new ParamInfo("render", buffer.toString());
   }
   
   private static final String DESC = 
                                  "Used to determine if pages should " +
   		                         "be render while they are being scrolled.";
   
   public RenderScrollingArg()
   {
      super(SettingsKeys.RENDER_SCROLLING_KEY, 
            1 ,PARAM_DESCS, DESC, false);
   }
   
   public String encode(boolean render)
   {
      return PREFIX+getIdentifier()+" "+render;
   }
   
   @Override
   public ArgResult decode(String[] args)
   {
      boolean render = Boolean.parseBoolean(args[0]);
      
      SettingsManager.getSharedInstance().setValue(getIdentifier(), render);
      return ArgResult.SHOW_GUI;
   }
}
