
interface Calculator{
    int operator(int a,int b);
}

class Add implements Calculator{

    @Override
    public int operator(int a, int b) {
        return a+b;
    }
}

public class Main{
    public static void main(String[] args) {
        Calculator add = (a,b)->a+b;
        System.out.println("sum " + add.operator(2,3));

        Calculator substract = (a, b)-> a-b;

        Calculator multiply = (a,b)->a*b;
        System.out.println("Multiply "+multiply.operator(2,3));
    }
}
