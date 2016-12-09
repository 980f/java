I just have to unlaod this so I quit thinking about it.

Have a single PoolManager that contains all ObjectPools.
A class that we wish to pool has New() functions which ask the global PoolManager service to give it one of (whatever.class).
If the pool manager doesn't have a pool fot htat class it makes one.

class ObjectPool extends Vector(){
  Object nextFree(){
    return remove(last());
  }
  release(object obj){
    obj.onRelease();
    add(obj);
  }
}

class PoolManager {
  static hashTable ocean;

  static ObjectPool getpool(Class classofObjectDesired){
    ObjectPool pool= ocean.get(classofObjectDesired.hashcode());
    if(pool==null){
      pool = new ObjectPool(); //probably just a vector.
      ocean.put(classofObjectDesired.hashcode(),pool);
    }
    return pool;
  }

  static Object getMeOne(Class classofObjectDesired){
    ObjectPool pool= ocean.getpool(classofObjectDesired.hashcode());
    Object obj=pool.nextFree();
    if(obj==null){
      //here we can deal with detecting a run-away allocaiton.
      obj=classofObjectDesired.Make(); //each poolable class must have a public Object static Make() function.
    }
    obj.onAcquired(); // a convenience, jhave a funcoitn that is called when object removed from pool
    return obj;
  }

  static void released(Object released){
    ObjectPool pool= ocean.getpool(release.getClass().hashcode());
    pool.released(released);
  }

}



========
interface isPoolable

onRelease()
onAcquired()




Andy.
512-791-3529
