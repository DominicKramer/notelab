Version:  Obtained from CVS, Saturday, September 26, 2009 at 12:49 AM 
          central standard time.  Some modifications were 
          made to the code.

The following two commands will check out the current source code for 
pdf-renderer from its CVS server (where 'username' is your username): 

  cvs -d :pserver:username@cvs.dev.java.net:/cvs login
  cvs -d :pserver:username@cvs.dev.java.net:/cvs checkout pdf-renderer 

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

