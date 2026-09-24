package com.caeliusconsulting.jobqueuesim.learnings;

public class StringMethodsDemo {

    public static void main(String[] args) {

        String str = "Hello Java";

        System.out.println(str.length());          // 10
        System.out.println(str.charAt(1));         // e
        System.out.println(str.toUpperCase());     // HELLO JAVA
        System.out.println(str.toLowerCase());     // hello java
        System.out.println(str.substring(6));      // Java
        System.out.println(str.contains("Java"));  // true
        System.out.println(str.startsWith("Hello"));// true
        System.out.println(str.endsWith("Java"));  // true
        System.out.println(str.indexOf("Java"));   // 6
        System.out.println(str.replace("Java", "World")); // Hello World
        System.out.println(str.equals("Hello Java"));     // true
        System.out.println(str.isEmpty());         // false
        System.out.println(str.trim());            // removes leading/trailing spaces
    }
}