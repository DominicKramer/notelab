#!/bin/bash
 
 for F in *.svg
 do
    rsvg -w 128 $F ../${F%.svg}.png
 done
