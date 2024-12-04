// Item.java
package application;

public class Item implements FarmItem {
    private String name;
    private double price;
    private double x, y;
    private double length, width, height;

    public Item(String name) {
        this.name = name;
    }

    @Override
    public void setName(String name) { this.name = name; }
    @Override
    public String getName() { return name; }

    @Override
    public void setPrice(double price) { this.price = price; }
    @Override
    public double getPrice() { return price; }

    @Override
    public void setLocationX(double x) { this.x = x; }
    @Override
    public double getLocationX() { return x; }

    @Override
    public void setLocationY(double y) { this.y = y; }
    @Override
    public double getLocationY() { return y; }

    @Override
    public void setDimensions(double length, double width, double height) {
        this.length = length;
        this.width = width;
        this.height = height;
    }
    @Override
    public double getLength() { return length; }
    @Override
    public double getWidth() { return width; }
    @Override
    public double getHeight() { return height; }

    @Override
    public void add(FarmItem item) { }
    @Override
    public void remove(FarmItem item) { }
    @Override
    public FarmItem getChild(int index) { return null; }
}
