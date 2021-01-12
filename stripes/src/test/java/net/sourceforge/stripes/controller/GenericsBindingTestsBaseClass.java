package net.sourceforge.stripes.controller;

import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.testbeans.TestGenericBean;


/**
 * A simple base class that is littered with Type parameters at the class level. Contains
 * no tests in and of itself, but it is necessary to be a public class in order for
 * {@link GenericsBindingTests} to extend it and have the methods be accessible.
 *
 * @author Tim Fennell
 */
public class GenericsBindingTestsBaseClass<JB, N, E, K, V> {

   JB                                bean;
   TestGenericBean.GenericBean<N, E> genericBean;
   N                                 number;
   List<? extends E>                 list;
   Map<K, V>                         map;

   public JB getBean() { return bean; }

   public TestGenericBean.GenericBean<N, E> getGenericBean() { return genericBean; }

   public List<? extends E> getList() { return list; }

   public Map<K, V> getMap() { return map; }

   public N getNumber() { return number; }

   public void setBean( JB bean ) { this.bean = bean; }

   public void setGenericBean( TestGenericBean.GenericBean<N, E> genericBean ) { this.genericBean = genericBean; }

   public void setList( List<? extends E> list ) { this.list = list; }

   public void setMap( Map<K, V> map ) { this.map = map; }

   public void setNumber( N number ) { this.number = number; }
}
