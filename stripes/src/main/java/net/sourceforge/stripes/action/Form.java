package net.sourceforge.stripes.action;

public interface Form<T> extends ActionBean {

   T getBean();

   void setBean( T initial );

}
