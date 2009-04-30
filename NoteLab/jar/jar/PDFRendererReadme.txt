Version:  Obtained from CVS, Sunday, April 19, 2009 at 2:32 AM 
          central standard time.  Some modifications were 
          made to the code.

Modifications:
--------------
File:  com.sun.pdfview.PDFRenderer
Description:  Modified the 'setupRendering(Graphics2D g)' 
              method so that the Graphics2D object supplied to the 
              method does not have any of its attributes modified.  
              
              The code from CVS set various attributes, like 
              antialiasing, for the Graphics2D object.  Hence 
              if the Graphics2D object was configured and then 
              used with a PDFRenderer, these configurations would 
              be lost.  
              
              This change was necessary to allow NoteLab 
              to render PDF documents in a low quality state 
              while the user was quickly scrolling through the 
              document.  
              
              The 'setup()' method was also modified to reflect 
              these changes.

File:  com.sun.pdfview.font.PDFFontDescriptor
Description:  Added various null pointer safety checks in the 
              constructor.

