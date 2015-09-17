#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// "new" a new object, do necessary initializations, and
// return the pointer (reference).
/*    -----------------------------------------
      | vptr | v0 | v1 | ...      | v_{size-1}|                           
      -----------------------------------------
      ^      \                                /
      |       \<------------- size --------->/
      |
      p (returned address)
*/
void *Tiger_new (void *vtable, int size) {
  // You should write 4 statements for this function.
  // #1: "malloc" a chunk of memory (be careful of the size):
  if (0 >= size) {
    printf("Warning: try to allocate a mem space less than 0.\n");
    size = 1;
  }
  int sz = sizeof(void*) + size;
  void *p = malloc(sz);
  // #2: clear this chunk of memory (zero off it):
  memset(p, 0, sz);
  // #3: set up the "vptr" pointer to the value of "vtable":
  memcpy(p, &vtable, sizeof(void*));
  // #4: return the pointer
  return p;
}

// "new" an array of size "length", do necessary
// initializations. And each array comes with an
// extra "header" storing the array length.
// This function should return the starting address
// of the array elements, but not the starting address of
// the array chunk. 
/*    ---------------------------------------------
      | length | e0 | e1 | ...      | e_{length-1}|                           
      ---------------------------------------------
               ^
               |
               p (returned address)
*/
void *Tiger_new_array (int length) {
  // You can use the C "malloc" facilities, as above.
  // Your code here:
  int *p = (int*)malloc(sizeof(int) * (length + 1));
  if (NULL == p) return NULL;
  *p = length;
  ++p;
  return p;
}
