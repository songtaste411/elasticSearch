package org.example.jvm.classloading;

/**
 * 被动使用类字段演示一：
 * 通过子类引用父类的静态字段，不会导致子类初始化
 **/
public class SuperClass {

    static {
        System.out.println("SuperClass init!");
    }

    public static int value = 123;
}

 class SubClass extends SuperClass {

    static {
        System.out.println("SubClass init!");
    }
}

/**
 * 非主动使用类字段演示
 **/
 class NotInitialization {
    public static void main(String[] args) {
        SuperClass[] sca = new SuperClass[10];
//        for (SuperClass superClass:
//             sca) {
//            System.out.println(superClass.value);
//        }
    }


}
