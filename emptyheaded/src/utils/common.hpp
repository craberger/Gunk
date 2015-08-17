#ifndef COMMON_H
#define COMMON_H

#include <x86intrin.h>
#include <unordered_map>
#include <ctime>
#include <sys/time.h>
#include <sys/resource.h>
#include <stdio.h>
#include <iostream>
#include <fstream>
#include <vector>
#include <iterator>
#include <algorithm>  // for std::find
#include <cstring>
#include <sys/mman.h>
#include <fcntl.h>    /* For O_RDWR */
#include <unistd.h>   /* For open(), creat() */
#include <math.h>
#include <unistd.h>
#include <tuple>
#include <cstdarg>
#include <set>
#include "assert.h"

//static size_t ADDRESS_BITS_PER_BLOCK = 7;
//static size_t BLOCK_SIZE = 128;
//static double BITSET_THRESHOLD = 1.0 / 16.0;

// Experts only! Proceed with caution!
//#define ENABLE_PCM
//#define ENABLE_PRINT_THREAD_TIMES
//#define ENABLE_ATOMIC_UNION

//Needed for parallelization, prevents false sharing of cache lines
#define PADDING 300
#define MAX_THREADS 512
#define VECTORIZE 1

//CONSTANTS THAT SHOULD NOT CHANGE
#define SHORTS_PER_REG 8
#define INTS_PER_REG 4
#define BYTES_PER_REG 16
#define BYTES_PER_CACHELINE 64


#ifndef NUM_THREADS_IN
static size_t NUM_THREADS = 52;
#else
static size_t NUM_THREADS = NUM_THREADS_IN; 
#endif

namespace common{
  static size_t bitset_length = 0;
#ifndef OPTIMIZER_OVERHEAD
  static double bitset_req = (1.0/256.0);
#else
  static double bitset_req = 2.0;
#endif
}

struct aggregate{};
struct materialize{};

namespace type{
  enum file : uint8_t{
    csv = 0,
    tsv = 1,
    binary = 2
  };

  enum primitive: uint8_t{
    BOOL = 0,
    UINT32 = 1,
    UINT64 = 2,
    STRING = 3
  };

  enum layout: uint8_t {
    RANGE_BITSET = 0,
    UINTEGER = 1,
    HYBRID = 2,
    BLOCK_BITSET = 3,
    BLOCK = 4,
    NOT_VALID = 8

  };

}

#endif
