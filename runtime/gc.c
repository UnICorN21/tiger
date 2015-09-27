#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// The Gimple Garbage Collector.
static void Tiger_gc();

//===============================================================//
// The Java Heap data structure.

/*   
      ----------------------------------------------------
      |                        |                         |
      ----------------------------------------------------
      ^\                      /^
      | \<~~~~~~~ size ~~~~~>/ |
    from                       to
 */
struct JavaHeap {
  int size;         // in bytes, note that this if for semi-heap size
  char *from;       // the "from" space pointer
  char *fromFree;   // the next "free" space in the from space
  char *to;         // the "to" space pointer
  char *toStart;    // "start" address in the "to" space
  char *toNext;     // "next" free space pointer in the to space
};

// The Java heap, which is initialized by the following
// "heap_init" function.
struct JavaHeap heap;

// Lab 4, exercise 10:
// Given the heap size (in bytes), allocate a Java heap
// in the C heap, initialize the relevant fields.
void Tiger_heap_init (int heapSize) {
  // You should write 7 statement here:
  // #1: allocate a chunk of memory of size "heapSize" using "malloc"
  if (0 >= heapSize) {
    printf("Warning: try to create a heap space less than 0.\n");
    heapSize = 1;
  }
  char *heapChuck = (char*)malloc(heapSize);
  // #2: initialize the "size" field, note that "size" field
  // is for semi-heap, but "heapSize" is for the whole heap.
  heap.size = heapSize >> 1;
  // #3: initialize the "from" field (with what value?)
  heap.from = heapChuck;
  // #4: initialize the "fromFree" field (with what value?)
  heap.fromFree = heapChuck;
  // #5: initialize the "to" field (with what value?)
  heap.to = (char*)(heapChuck + heap.size);
  // #6: initizlize the "toStart" field with NULL;
  heap.toStart = heap.to;
  // #7: initialize the "toNext" field with NULL;
  heap.toNext = heap.to;
  return;
}

// The "prev" pointer, pointing to the top frame on the GC stack. 
// (see part A of Lab 4)
void *prev = 0;

//===============================================================//
// Object Model And allocation

union ObjInfo {
  void *vptr;
  unsigned length;
};

const int HEAD_SZ = sizeof(union ObjInfo) + sizeof(void*);

// Lab 4: exercise 11:
// "new" a new object, do necessary initializations, and
// return the pointer (reference).
/*    ----------------
      |  forwarding  |
      |--------------|
 p--->|   objInfo    |
      |--------------|\
      | v_0          | \
      |--------------|  s
      | ...          |  i
      |--------------|  z
      | v_{size-1}   | /e
      ----------------/
*/
// Try to allocate an object in the "from" space of the Java
// heap. Read Tiger book chapter 13.3 for details on the
// allocation.
// There are two cases to consider:
//   1. If the "from" space has enough space to hold this object, then
//      allocation succeeds, return the apropriate address (look at
//      the above figure, be careful);
//   2. if there is no enough space left in the "from" space, then
//      you should call the function "Tiger_gc()" to collect garbages.
//      and after the collection, there are still two sub-cases:
//        a: if there is enough space, you can do allocations just as case 1; 
//        b: if there is still no enough space, you can just issue
//           an error message ("OutOfMemory") and exit.
//           (However, a production compiler will try to expand
//           the Java heap.)
void *Tiger_new (void *vtable, int size) {
  // Your code here:
  if (0 >= size) {
    printf("Warning: try to allocate a mem space less than 0.\n");
    size = 1;
  }
  int haveGC = 0;
  while (1) {
    int sz = size + HEAD_SZ;
    long remain = heap.size - (heap.fromFree - heap.from);
    if (remain >= sz) {
      int * *pObj = (int**)malloc(sz);
      *pObj++ = NULL;
      ((union ObjInfo*)pObj)->vptr = vtable;
      return pObj;
    } else if (!haveGC) {
      Tiger_gc();
      haveGC = 1;
    } else {
      printf("OutOfMemoryError: cannot have enough heap space.");
      exit(1);
    }
  }
}

// "new" an array of size "length", do necessary
// initializations. And each array comes with an
// extra "header" storing the array length and other information.
/*    ----------------
      |  forwarding  |
      |--------------|
      |   objInfo    |
      |--------------|\
p---->| e_0          | \      
      |--------------|  s
      | ...          |  i
      |--------------|  z
      | e_{length-1} | /e
      ----------------/
*/
// Try to allocate an array object in the "from" space of the Java
// heap. Read Tiger book chapter 13.3 for details on the
// allocation.
// There are two cases to consider:
//   1. If the "from" space has enough space to hold this array object, then
//      allocation succeeds, return the apropriate address (look at
//      the above figure, be careful);
//   2. if there is no enough space left in the "from" space, then
//      you should call the function "Tiger_gc()" to collect garbages.
//      and after the collection, there are still two sub-cases:
//        a: if there is enough space, you can do allocations just as case 1; 
//        b: if there is still no enough space, you can just issue
//           an error message ("OutOfMemory") and exit.
//           (However, a production compiler will try to expand
//           the Java heap.)
void *Tiger_new_array (int length) {
  if (0 >= length) {
    printf("Warning: try to create an array with illegal length.\n");
    length = 1;
  }
  int haveGC = 0;
  while (1) {
    int sz = sizeof(int) * length + HEAD_SZ;
    long remain = heap.size - (heap.fromFree - heap.from);
    if (remain >= sz) {
      int * *pObj = (int**)malloc(sz);
      *pObj++ = NULL;
      ((union ObjInfo*)pObj)->length = length;
      return ++pObj;
    } else if (!haveGC) {
      Tiger_gc();
      haveGC = 1;
    } else {
      printf("OutOfMemoryError: cannot have enough heap space.");
      exit(1);
    }
  }
}

//===============================================================//
// The Gimple Garbage Collector

// Lab 4, exercise 12:
// A copying collector based-on Cheney's algorithm.
static void Tiger_gc () {
  // Your code here:

}
