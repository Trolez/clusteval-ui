package clusteval;

public class Run implements Comparable<Run> {
    private String name;
    boolean edited = false;

    public Run () {}

    public Run (String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public int compareTo(Run other) {
        return getName().compareTo(other.getName());
    }
}
