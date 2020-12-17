package net.sourceforge.stripes.action;

public interface SingleBeanForm<T> extends Form {

   T getBean();

   void setBean( T initial );
}
