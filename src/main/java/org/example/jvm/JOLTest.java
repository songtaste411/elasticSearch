package org.example.jvm;

import org.example.Main;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.vm.VM;

public class JOLTest {
    public static void main(String[] args) {
        JOLTest master = new JOLTest();
        System.out.println("====加锁前====");
        System.out.println(ClassLayout.parseInstance(master).toPrintable());
        System.out.println("====加锁后====");
        synchronized (master) {
            System.out.println(ClassLayout.parseInstance(master).toPrintable());
        }
    }



}
