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

package noteLab.util.geom.unit;

/*
 * The sizes below were taken from the document:  
 * /usr/share/doc/packages/hplip/hpijs-2.1.7/hpijs_readme.html
 * found on the SUSE 9 Linux Distribution
                            inches:      postscript units:
  1. Letter                  8.5 x 11          612 x 792
  2. Legal                   8.5 x 14          612 x 1008
  3. Ledger                   11 x 17          792 x 1224
  4. Executive              7.25 x 10.5        522 x 756
  5. A3                    11.69 x 16.53       842 x 1190
  6. A4                     8.27 x 11.69       595 x 842
  7. A5                     5.83 x 8.27        420 x 595
  8. A6                     4.13 x 5.83        297 x 420 
  9. Photo                     4 x 6           288 x 432 
 10. B4                   10.126 x 14.342      729 x 1033 
 11. B5                     7.17 x 10.126      516 x 729
 12. Oufuku-Hagaki          5.83 x 7.87        420 x 567
 13. Hagaki                 3.94 x 5.83        284 x 420
 14. Super B                  13 x 19          936 x 1368
 15. Flsa                    8.5 x 13          612 x 936
 16. Number 10 Envelope     4.12 x 9.5         297 x 684
 17. A2 Envelope            4.37 x 5.75        315 x 414
 18. C6 Envelope            4.49 x 6.38        323 x 459
 19. DL Envelope            4.33 x 8.66        312 x 624
 20. Japanese Envelope #3   4.72 x 9.25        340 x 666
 21. Japanese Envelope #4   3.54 x 8.07        255 x 581
 */
public enum MPaperSize
{
   Letter(8.5, 11), 
   Legal(8.5, 14), 
   Ledger(11, 17), 
   Executive(7.25, 10.5), 
   A3(11.69, 16.53), 
   A4(8.27, 11.69), 
   A5(5.83, 8.27), 
   A64(13, 5.83),  
   Photo(4, 6),  
   B4(10.126, 14.342), 
   B5(7.17, 10.126), 
   Oufuku_Hagaki(5.83, 7.87), 
   Hagaki(3.94, 5.83), 
   Super_B(13, 19), 
   Flsa(8.5, 13),
   Number_10_Envelope(4.12, 9.5), 
   A2_Envelope(4.37, 5.75), 
   C6_Envelope(4.49, 6.38), 
   DL_Envelope(4.33, 8.66), 
   Japanese_Envelope_3(4.72, 9.25), 
   Japanese_Envelope_4(3.54, 8.07), 
   Custom(-1, -1);
   
   private MDimension mDim;
   
   private MPaperSize(double widthInches, double heightInches)
   {
      MValue mWidth = new MValue(widthInches, Unit.INCH);
      MValue mHeight = new MValue(heightInches, Unit.INCH);
      
      this.mDim = new MDimension(mHeight, mWidth);
   }
   
   public MDimension getMDimension()
   {
      return this.mDim.getCopy();
   }
}
