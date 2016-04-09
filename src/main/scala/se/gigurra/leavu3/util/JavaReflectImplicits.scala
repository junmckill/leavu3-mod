package se.gigurra.leavu3.util

/**
  * Created by kjolh on 4/3/2016.
  */
trait JavaReflectImplicits {

  implicit class RichClass(cls: Class[_]) {

    def reflectField(name: String, o: Object = null): Object = {
      val field = cls.getDeclaredField(name)
      field.setAccessible(true)
      field.get(o)
    }

    def reflectGetter(name: String, o: Object = null): Object = {
      val method = cls.getDeclaredMethod(name)
      method.setAccessible(true)
      method.invoke(o)
    }

    def reflectInvokeStatic(name: String, args: Object*): Object = {
      reflectInvoke(name, null, args:_*)
    }

    def reflectInvoke(name: String, o: Object, args: Object*): Object = {
      val method = cls.getDeclaredMethods.find(_.getName == name).getOrElse(throw new RuntimeException(s"Class $cls has no method named $name"))
      method.setAccessible(true)
      method.invoke(o, args:_*)
    }
  }

  implicit class RichObject(o: Object) {

    def reflectField(name: String):Object = {
      o.getClass.reflectField(name, o)
    }

    def reflectGetter(name: String): Object = {
      o.getClass.reflectGetter(name, o)
    }

    def reflectInvoke(name: String, args: Object*): Object = {
      o.getClass.reflectInvoke(name, o, args:_*)
    }
  }

}
