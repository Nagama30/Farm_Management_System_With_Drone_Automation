// FarmItem.java
package application;

public interface FarmItem {
    void setName(String name);
    String getName();

    void setPrice(double price);
    double getPrice();

    void setLocationX(double x);
    double getLocationX();

    void setLocationY(double y);
    double getLocationY();

    void setDimensions(double length, double width, double height);
    double getLength();
    double getWidth();
    double getHeight();

    void add(FarmItem item);
    void remove(FarmItem item);
    FarmItem getChild(int index);
}
