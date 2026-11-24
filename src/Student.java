public class Student extends Member {
    private final int maxItems = 3;
    private final int loanPeriod = 14;

    public Student(String id, String name) {
        super(id, name);
    }

    @Override
    public int getMaxItems() { return maxItems; }

    @Override
    public int getLoanPeriod() { return loanPeriod; }
}
