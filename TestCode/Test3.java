public class Test3 {
    private String name;

    public Test3(String name) {
        this.name = name;
    }

    public void sayHello() {
        if (name != null) {
            System.out.println("Hello " + name);
        }
    }
}
