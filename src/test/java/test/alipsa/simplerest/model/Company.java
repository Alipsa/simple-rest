package test.alipsa.simplerest.model;

public class Company {

  private String name;
  private int number;

  public Company() {
  }

  public Company(String name, int number) {
    this.name = name;
    this.number = number;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getNumber() {
    return number;
  }

  public void setNumber(int number) {
    this.number = number;
  }
}
