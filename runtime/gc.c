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

typedef struct {
    void *vptr; // NULL for an array-instance
    union {
        int size;
        unsigned length;
    } info;
    void *forwarding;
} ObjHeader;

const int HEAD_SZ = sizeof(ObjHeader);

// Lab 4: exercise 11:
// "new" a new object, do necessary initializations, and
// return the pointer (reference).
/*    ----------------
 p--->|  objHeader   |
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
  if (0 >= size) {
    printf("Warning: try to allocate a mem space less than 0.\n");
    size = 1;
  }
  int haveGC = 0;
  while (1) {
    int sz = size + HEAD_SZ;
    long remain = heap.size - (heap.fromFree - heap.from);
    if (remain >= sz) {
      ObjHeader *pObj = (ObjHeader*)heap.fromFree;
      pObj->vptr = vtable;
      pObj->info.size = size;
      pObj->forwarding = NULL;
      heap.fromFree += sz;
      return pObj;
    } else if (!haveGC) {
      Tiger_gc();
      haveGC = 1;
    } else {
      printf("OutOfMemoryError: cannot have enough heap space.\n");
      exit(1);
    }
  }
}

// "new" an array of size "length", do necessary
// initializations. And each array comes with an
// extra "header" storing the array length and other information.
/*    ----------------
 p--->|  objHeader   |
      |--------------|\
      | e_0          | \
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
      ObjHeader *pObj = (ObjHeader*)heap.fromFree;
      pObj->vptr = NULL;
      pObj->info.length = length;
      pObj->forwarding = NULL;
      heap.fromFree += sz;
      return pObj;
    } else if (!haveGC) {
      Tiger_gc();
      haveGC = 1;
    } else {
      printf("OutOfMemoryError: cannot have enough heap space.\n");
      exit(1);
    }
  }
}

//===============================================================//
// The Gimple Garbage Collector

// Lab 4, exercise 12:
// A copying collector based-on Cheney's algorithm.

inline static void swap(void *a, void *b) {
  void *mid = a;
  a = b;
  b = mid;
}

static void* Tiger_gc_forward(void *p) {
  if (p >= (void*)heap.from && p < heap.from + heap.size) {
    void * *forwarding = (void**)((int*)p + 2);
    if (*forwarding >= (void*)heap.to && *forwarding <= heap.to + heap.size) return p;
    int size = HEAD_SZ;

    ObjHeader *header = (ObjHeader*)p;
    if (NULL != header->vptr) { // p is an instance-object
      size += header->info.size;
    } else { // p is an array-object
      size += sizeof(int) * header->info.length;
    }

    forwarding = memcpy(heap.toNext, p, size);
    heap.toNext += size;
    return forwarding;
  } else return p;
}

static void Tiger_gc () {
  int *cur_frame = (int*)prev;
  while (NULL != cur_frame) {
    // fetch the info of a gc frame
    char *arguments_gc_map = *(char**)(cur_frame + 2);
    int *arguments_base_addr = *(int**)(cur_frame + 4);
    int local_ref_cnt = *(cur_frame + 6);

    // forward the formals
    int i;
    for (i = 0; i < strlen(arguments_gc_map); ++i) if ('1' == arguments_gc_map[i]) {
      void * *cur_formal = (void**)(arguments_base_addr + i);
      *cur_formal = Tiger_gc_forward(*cur_formal);
    }

    // forward the locals
    for (i = 0; i < local_ref_cnt; ++i) {
      void * *cur_local = (void**)(cur_frame + 8 + i);
      *cur_local = Tiger_gc_forward(*cur_local);
    }

    // forward the outer environment
    cur_frame = *(void**)cur_frame;
  }

  char *scan = heap.toStart;
  while (scan < heap.toNext) {
    int *p = (int*)scan;
    ObjHeader *header = (ObjHeader*)p;
    if (NULL != header->vptr) {
      char *gc_map = *(char**)(header->vptr);
      int i;
      for (i = 0; i < strlen(gc_map); ++i) if ('1' == gc_map[i]) {
        void * *field = (void**)(p + 3 + i);
        *field = Tiger_gc_forward(*field);
      }
    }
    scan += header->info.size;
  }

  swap(heap.from, heap.to);
  heap.fromFree = heap.toNext;
  heap.toStart = heap.to;
  heap.toNext = heap.to;
}
