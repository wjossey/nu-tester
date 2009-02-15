package tester;

import java.lang.reflect.*;
import java.util.*;

/**
 * Copyright 2007, 2008 Viera K. Proulx
 * This program is distributed under the terms of the 
 * GNU Lesser General Public License (LGPL)
 */

/**
 * This class leverages the use of the Java reflection classes
 * to implement comparison for extensional equality
 * between two objects of arbitrary type.
 * 
 * @author Viera K. Proulx
 * @since 3 March 2008     
 *
 */
public class Inspector{
 
  /** the tolerance for comparison of relative difference of inexact numbers */
  protected static double TOLERANCE = 0.001;

  /** current indentation level for pretty-printing */
  protected static String INDENT = "  ";
  
  /** a hashmap of pairs of hashcodes for two objects that are being compared:
   * if the same pair is compared again, the loop of comparisons stops 
   * and produces true
   */
  private HashMap<Integer, Integer> hashmap = new HashMap<Integer, Integer>();

  /** 
   * Constructor: For the given instance get its <code>Class</code> 
   * and fields. 
   */
  public Inspector(){ }
  

  /**------------ THE METHODS USED TO COMPARE TWO OBJECTS -------------*/
  /**
   * <P>Compare the two given objects for extensional equality.<P>
   * <P>Consider inexact numbers (types <code>double</code>, 
   * <code>float</code>, <code>Double</code>, <code>Float</code>) 
   * to be the same if the relative difference
   * is below <code>TOLERANCE</code></P>
   * <P>Use <code>==</code> for all other primitive types 
   * and their wrapper classes.</P>
   * <P>Use the <code>String equals</code> method to compare two
   * objects of the type <code>String</code>.
   * <P>Traverse over <code>Arrays</code>, datasets that implement 
   * <CODE>{@link Traversal Traversal}</CODE> and datasets that implement
   * <code>Iterable</code>interface.</P>
   * <P>For datasets that implement the <code>Map</code> interface compare
   * their sizes, and entry sets for set equality of key-value pairs.</p>
   * <P>For the instances of a class that implements the 
   * <CODE>{@link ISame ISame}</CODE> interface, use its 
   * <CODE>same</CODE> method.</P>
   * <P>Special tests allow the user to provide the method to invoke
   * on the given object with the given argument list</P>
   * <P>Additionally, user may specify that a method invocation must 
   * throw a given <code>Exception</code> with the given message.</P>
   * 
   * @param obj1
   * @param obj2
   * @return true if the two given object are the same
   */
  @SuppressWarnings("unchecked")
public boolean isSame(Object obj1, Object obj2){
    /** make sure both objects are not null */
    if (obj1 == null)
      return obj2 == null; //Returns true iff obj1 & obj2 are both null, otherwise returns false
    if (obj2 == null)
      return false;
    if(obj1 == obj2)
    	return true; //obj1 and obj2 are the same object
    
    /** handle world teachpack colors */
    obj1 = this.adjustIColors(obj1);
    obj2 = this.adjustIColors(obj2);
    
    Reflector r1 = new Reflector(obj1);
    Reflector r2 = new Reflector(obj2);   
    
    boolean sameClass = r1.sampleClass.equals(r2.sampleClass);
    
    if (sameClass){
      
      /** Random objects are all the same */
      if (r1.sampleClass.getName().equals("java.util.Random")){
        return true;
      }

      /** handle String objects separately */
      if (r1.sampleClass.getName().equals("java.lang.String")){
        return obj1.equals(obj2);
      }

      /** handle Color objects separately */
      /** --- compare only the RGB of colors */
     /*if (r1.sampleClass.getName().equals("java.awt.Color")){
        return ((java.awt.Color)obj1).getRGB() ==
               ((java.awt.Color)obj2).getRGB();
      }*/

      /** handle the primitive types separately */
      if (r1.sampleClass.isPrimitive()){
        if (isDouble(r1.sampleClass.getName()))
          return isSameDouble((Double)obj1, (Double)obj2);
        else if (isFloat(r1.sampleClass.getName()))
          return isSameFloat((Float)obj1, (Float)obj2);
        else
          return (obj1.equals(obj2));
      }

      /** handle the wrapper types separately */
      if (isWrapperClass(r1.sampleClass.getName())){
        if (isDouble(r1.sampleClass.getName()))
          return isSameDouble((Double)obj1, (Double)obj2);
        else if (isFloat(r1.sampleClass.getName()))
          return isSameFloat((Float)obj1, (Float)obj2);
        else
          return (obj1.equals(obj2));
      }
      
      /** handle the Canvas class in the draw teachpack */
      if (isOurCanvas(r1.sampleClass.getName()))
        if (isOurCanvas(r2.sampleClass.getName()))
          return (obj1.equals(obj2));
        else
          return false;
      
      /** two Random objects are never compared */
		if (r1.sampleClass.getName().equals("java.util.Random")) {
			return true;
		}

      /** Record the information about the object compared
       * and check whether the current pair has already been tested
       * for equality, or has been viewed before.
       */
      Integer i1 = obj1.hashCode();
      Integer i2 = obj2.hashCode();
      
      Integer i2match = hashmap.get(i1);
     
      if ((i2match != null) && (i2match.equals(i2)))
        return true;
    
      hashmap.put(i1, i2);
      
      if(obj1.getClass().isArray() && obj2.getClass().isArray() &&	
    		  obj1.getClass() == obj2.getClass()){
    	  int length = Array.getLength(obj1);
          if (Array.getLength(obj2) == length){
            for(int i = 0; i < length; i++){
            	if(!isSame(Array.get(obj1, i), Array.get(obj2, i)))
            		return false;
            }
            return true;
          }
          else
            return false;
      }
      
      /** handle Array objects */
      if ((obj1 instanceof Object[]) && (obj2 instanceof Object[])){
        int length = Array.getLength(obj1);
        if (Array.getLength(obj2) == length){
          for(int i = 0; i < length; i++){
            if (!isSame(((Object[])obj1)[i], ((Object[])obj2)[i]))
              return false;
          }
          return true;
        }
        else
          return false;
      }
            
      /** handle ISame objects by delegating to the user-defined method */
      if ((obj1 instanceof ISame) && (obj2 instanceof ISame))
        return ((ISame)obj1).same((ISame)obj2);
        
      
      /** handle Iterable objects */
      if ((obj1 instanceof Iterable) && (obj2 instanceof Iterable))
        return isSameIterable((Iterable<?>)obj1, (Iterable<?>)obj2);
      
      /** handle the Map objects by comparing their size, key-set, and
       * key-value mappings */
      if ((obj1 instanceof Map) && (obj2 instanceof Map))
        return isSameMap((Map)obj1, (Map)obj2); 

      /** now handle the general case */     
      boolean sameValues = true;
      int i = 0;
      try{
        for ( ; i < Array.getLength(r1.sampleDeclaredFields); i++){
          sameValues = sameValues && 
            isSame(r1.sampleDeclaredFields[i].get(obj1),
                   r2.sampleDeclaredFields[i].get(obj2));
        }
      }catch(IllegalAccessException e){
        System.out.println("same comparing " + 
            r1.sampleDeclaredFields[i].getType().getName() + " and " +
            r2.sampleDeclaredFields[i].getType().getName() + 
            "cannot access the field " + i + 
            " message: " + e.getMessage());
        System.out.println("class 1: " + r1.sampleClass.getName());
        System.out.println("class 2: " + r2.sampleClass.getName());
      }
      
      

      return sameValues;
    }
    else
      return false;
  }
  
 
  /**
   * Determine whether the relative difference between two double numbers
   * is below the expected <code>TOLERANCE</code>
   * @param d1 the first inexact number
   * @param d2 the second inexact number
   * @return true is the two numbers are nearly the same 
   */
  protected boolean isSameDouble(double d1, double d2){
    if (d1 - d2 == 0.0)
      return true;
    else{
     return (Math.abs(d1 - d2) / (Math.max (Math.abs(d1), Math.abs(d2))))
             < TOLERANCE;
    }
  }
 
  /**
   * Determine whether the relative difference between two float numbers
   * is below the expected <code>TOLERANCE</code>
   * @param f1 the first inexact number
   * @param f2 the second inexact number
   * @return true is the two numbers are nearly the same 
   */
  protected boolean isSameFloat(float f1, float f2){
    if (f1 - f2 == 0.0)
      return true;
    else{
      Double d1 = ((Float)f1).doubleValue();
      Double d2 = ((Float)f2).doubleValue();
     return (Math.abs(d1 - d2) / (Math.max (Math.abs(d1), Math.abs(d2))))
             < TOLERANCE;
    }
  }

  /**
   * Determine whether two <code>Iterable</code> objects generate the same
   * data elements in the same order.
   * @param obj1 the first <code>Iterable</code> dataset
   * @param obj2 the second <code>Iterable</code> dataset
   * @return true is the two datasets are extensionally equal 
   */
  protected boolean isSameIterable(Iterable<?> obj1, Iterable<?> obj2){
    Iterator<?> it1 = obj1.iterator();
    Iterator<?> it2 = obj2.iterator();
    
    return this.isSameData(it1, it2);
  }
 
  /**
   * Determine whether two <code>Iterator</code>s generate the same data
   * in the same order.
   * @param it1 the first <code>Iterator</code>
   * @param it2 the second <code>Iterator</code>
   * @return true is both datasets containd the same data elements 
   * (in the same order)
   */
  protected boolean isSameData(Iterator<?> it1, Iterator<?> it2){
    /** if the first dataset is empty, the second one has to be too */
    if (!it1.hasNext()){
      return !it2.hasNext();
    }
    /** the first dataset is nonempty - make sure the second one is too... */
    else if(!it2.hasNext())
      return false;
    /** now both have data - compare the next pair of data and recur */
    else{
      return this.isSame(it1.next(), it2.next()) &&
             this.isSameData(it1, it2);
    }
  }


  /**
   * Determine whether two <code>Map</code> objects have the same 
   * key-value sets of data
   * @param obj1 the first <code>Map</code> dataset
   * @param obj2 the second <code>Map</code> dataset
   * @return true is the two maps are extensionally equal 
   */
  protected <K,V> boolean isSameMap(Map<K,V> obj1, Map<K,V> obj2){
    // make sure both maps have the same size keyset
    if (obj1.size() != obj2.size())
      return false;
    
    // the key sets for the two maps have the same size - pick one
    Set<K> set1 = obj1.keySet();
        
    for (Object key : set1){
      // make sure each key is in both key sets
      if (!obj2.containsKey(key))
        return false;
      
      // now compare the corresponding values
      if (!this.isSame(obj1.get(key), obj2.get(key)))
          return false;   
    }
    
    // all tests passed
    return true;
  }
  
  /**------- THE METHODS USED TO DETERMINE THE TYPES OF OBJECTS ----------*/ 
  /**
   * Does the class with the given name represent inexact numbers?
   * @param name the name of the class in question
   * @return true if this is double, float, or Double or Float
   */
  protected boolean isDouble(String name){
    return 
    name.equals("double") ||
    name.equals("java.lang.Double");
  }
  
  /**
   * Does the class with the given name represent inexact numbers?
   * @param name the name of the class in question
   * @return true if this is double, float, or Double or Float
   */
  protected boolean isFloat(String name){
    return 
    name.equals("float") ||
    name.equals("java.lang.Float");
  }
  
  /**
   * Does the class with the given name represent a wrapper class for 
   * a primitive class?
   * @param name the name of the class in question
   * @return true if this is a class that represents a wrapper class
   */
  protected static boolean isWrapperClass(String name){
    return 
    name.equals("java.lang.Integer") ||
    name.equals("java.lang.Long") ||
    name.equals("java.lang.Short") ||
    name.equals("java.math.BigInteger") ||
    name.equals("java.math.BigDecimal") ||
    name.equals("java.lang.Float") ||
    name.equals("java.lang.Double") ||
    name.equals("java.lang.Byte") ||
    name.equals("java.lang.Boolean") ||
    name.equals("java.lang.Character");           
  }
  
  /**
   * Is this a name of one of our <code>Canvas</code> classes?
   * @param name the name of the class to consider
   * @return true if the given name is one of our Canvas classes
   */
  protected static boolean isOurCanvas(String name){
    return name.equals("draw.Canvas")  ||
           name.equals("idraw.Canvas") ||
           name.equals("adraw.Canvas");
  }
  
  /**
   * If <code>obj</code> is an instance of one of the <code>IColor</code>s
   * change it and its <code>Reflector</code> object to represent <code>
   * java.awt.Color</code> before doing the comparison
   * 
   * @param obj the object that could be an <code>IColor</code>
   */
  protected Object adjustIColors(Object obj){
    /** Convert IColor-s from the colors teachpack to java.awt.Color */
    if (obj.getClass().getName().equals("colors.Red"))
      return java.awt.Color.red;
    if (obj.getClass().getName().equals("colors.White"))
      return java.awt.Color.white;
    if (obj.getClass().getName().equals("colors.Blue"))
      return java.awt.Color.blue;
    if (obj.getClass().getName().equals("colors.Black"))
      return java.awt.Color.black;
    if (obj.getClass().getName().equals("colors.Green"))
      return java.awt.Color.green;
    if (obj.getClass().getName().equals("colors.Yellow"))
      return java.awt.Color.yellow;
    return obj;
  }
}