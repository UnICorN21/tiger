#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "control.h"

extern void Tiger_heap_init (int);

int main (int argc, char **argv) {
  // initialize the Java heap
  Tiger_heap_init (Control_heapSize);

  // enter Java code...
  Tiger_main ();
}

